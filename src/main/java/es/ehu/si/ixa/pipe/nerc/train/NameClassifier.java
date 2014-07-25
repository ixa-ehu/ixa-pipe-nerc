package es.ehu.si.ixa.pipe.nerc.train;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import es.ehu.si.ixa.pipe.nerc.features.AdaptiveFeatureGenerator;
import es.ehu.si.ixa.pipe.nerc.features.AdditionalContextFeatureGenerator;
import es.ehu.si.ixa.pipe.nerc.features.BigramClassFeatureGenerator;
import es.ehu.si.ixa.pipe.nerc.features.CachedFeatureGenerator;
import es.ehu.si.ixa.pipe.nerc.features.NameContextGenerator;
import es.ehu.si.ixa.pipe.nerc.features.OutcomePriorFeatureGenerator;
import es.ehu.si.ixa.pipe.nerc.features.PreviousMapFeatureGenerator;
import es.ehu.si.ixa.pipe.nerc.features.SentenceFeatureGenerator;
import es.ehu.si.ixa.pipe.nerc.features.TokenClassFeatureGenerator;
import es.ehu.si.ixa.pipe.nerc.features.TokenFeatureGenerator;
import es.ehu.si.ixa.pipe.nerc.features.WindowFeatureGenerator;
import es.ehu.si.ixa.pipe.nerc.formats.CorpusSample;
import es.ehu.si.ixa.pipe.nerc.formats.CorpusSampleSequenceStream;

import opennlp.model.AbstractModel;
import opennlp.model.EventStream;
import opennlp.model.MaxentModel;
import opennlp.model.TrainUtil;
import opennlp.tools.util.BeamSearch;
import opennlp.tools.util.ObjectStream;
import opennlp.tools.util.Sequence;
import opennlp.tools.util.SequenceValidator;
import opennlp.tools.util.Span;
import opennlp.tools.util.TrainingParameters;
import opennlp.tools.util.model.ModelUtil;

public class NameClassifier {
  
  private static String[][] EMPTY = new String[0][0];
  public static final int DEFAULT_BEAM_SIZE = 3;
  private static final Pattern typedOutcomePattern = Pattern.compile("(.+)-\\w+");



  public static final String START = "start";
  public static final String CONTINUE = "cont";
  public static final String OTHER = "other";

  protected MaxentModel model;
  protected NameContextGenerator contextGenerator;
  private Sequence bestSequence;
  private BeamSearch<String> beam;
  
  private AdditionalContextFeatureGenerator additionalContextFeatureGenerator =
      new AdditionalContextFeatureGenerator();

  public NameClassifier(NameModel model) {
    this(model, DEFAULT_BEAM_SIZE);
  }

  /**
   * Initializes the name finder with the specified model.
   *
   * @param model
   * @param beamSize
   */
  public NameClassifier(NameModel model, AdaptiveFeatureGenerator generator, int beamSize,
      SequenceValidator<String> sequenceValidator) {
    this.model = model.getNameFinderModel();

    // If generator is provided always use that one
    if (generator != null) {
      contextGenerator = new DefaultNameContextGenerator(generator);
    }
    else {
      // If model has a generator use that one, otherwise create default
      AdaptiveFeatureGenerator featureGenerator = createFeatureGenerator();

      contextGenerator = new DefaultNameContextGenerator(featureGenerator);
    }

    contextGenerator.addFeatureGenerator(
          new WindowFeatureGenerator(additionalContextFeatureGenerator, 8, 8));

    if (sequenceValidator == null)
      sequenceValidator = new NameFinderSequenceValidator();

    beam = new BeamSearch<String>(beamSize, contextGenerator, this.model,
        sequenceValidator, beamSize);
  }

  public NameClassifier(NameModel model, AdaptiveFeatureGenerator generator, int beamSize) {
    this(model, generator, beamSize, null);
  }

  public NameClassifier(NameModel model, int beamSize) {
    this(model, null, beamSize);
  }
  
  private static AdaptiveFeatureGenerator createFeatureGenerator() {
    return new CachedFeatureGenerator(
          new AdaptiveFeatureGenerator[]{
            new WindowFeatureGenerator(new TokenFeatureGenerator(), 2, 2),
            new WindowFeatureGenerator(new TokenClassFeatureGenerator(true), 2, 2),
            new OutcomePriorFeatureGenerator(),
            new PreviousMapFeatureGenerator(),
            new BigramClassFeatureGenerator(),
            new SentenceFeatureGenerator(true, false)
            });
   }


  public Span[] find(String[] tokens) {
    return find(tokens, EMPTY);
  }

  /**
   * Generates name tags for the given sequence, typically a sentence,
   * returning token spans for any identified names.
   *
   * @param tokens an array of the tokens or words of the sequence,
   *     typically a sentence.
   * @param additionalContext features which are based on context outside
   *     of the sentence but which should also be used.
   *
   * @return an array of spans for each of the names identified.
   */
  public Span[] find(String[] tokens, String[][] additionalContext) {
    additionalContextFeatureGenerator.setCurrentContext(additionalContext);
    bestSequence = beam.bestSequence(tokens, additionalContext);

    List<String> c = bestSequence.getOutcomes();

    contextGenerator.updateAdaptiveData(tokens, c.toArray(new String[c.size()]));

    int start = -1;
    int end = -1;
    List<Span> spans = new ArrayList<Span>(tokens.length);
    for (int li = 0; li < c.size(); li++) {
      String chunkTag = c.get(li);
      if (chunkTag.endsWith(NameClassifier.START)) {
        if (start != -1) {
          spans.add(new Span(start, end, extractNameType(c.get(li - 1))));
        }
        start = li;
        end = li + 1;
      }
      else if (chunkTag.endsWith(NameClassifier.CONTINUE)) {
        end = li + 1;
      }
      else if (chunkTag.endsWith(NameClassifier.OTHER)) {
        if (start != -1) {
          spans.add(new Span(start, end, extractNameType(c.get(li - 1))));
          start = -1;
          end = -1;
        }
      }
    }
    if (start != -1) {
      spans.add(new Span(start, end, extractNameType(c.get(c.size() - 1))));
    }
    return spans.toArray(new Span[spans.size()]);
  }

  /**
   * Forgets all adaptive data which was collected during previous
   * calls to one of the find methods.
   *
   * This method is typical called at the end of a document.
   */
  public void clearAdaptiveData() {
   contextGenerator.clearAdaptiveData();
  }

  /**
   * Populates the specified array with the probabilities of the last decoded
   * sequence. The sequence was determined based on the previous call to
   * <code>chunk</code>. The specified array should be at least as large as
   * the number of tokens in the previous call to <code>chunk</code>.
   *
   * @param probs
   *          An array used to hold the probabilities of the last decoded
   *          sequence.
   */
   public void probs(double[] probs) {
     bestSequence.getProbs(probs);
   }

  /**
    * Returns an array with the probabilities of the last decoded sequence.  The
    * sequence was determined based on the previous call to <code>chunk</code>.
    *
    * @return An array with the same number of probabilities as tokens were sent to <code>chunk</code>
    * when it was last called.
    */
   public double[] probs() {
     return bestSequence.getProbs();
   }

   /**
    * Returns an array of probabilities for each of the specified spans which is the arithmetic mean
    * of the probabilities for each of the outcomes which make up the span.
    *
    * @param spans The spans of the names for which probabilities are desired.
    *
    * @return an array of probabilities for each of the specified spans.
    */
   public double[] probs(Span[] spans) {

     double[] sprobs = new double[spans.length];
     double[] probs = bestSequence.getProbs();

     for (int si=0; si<spans.length; si++) {

       double p = 0;

       for (int oi = spans[si].getStart(); oi < spans[si].getEnd(); oi++) {
         p += probs[oi];
       }

       p /= spans[si].length();

       sprobs[si] = p;
     }

     return sprobs;
   }

   /**
    * Trains a name finder model.
    *
    * @param languageCode
    *          the language of the training data
    * @param type
    *          null or an override type for all types in the training data
    * @param samples
    *          the training data
    * @param trainParams
    *          machine learning train parameters
    * @param generator
    *          null or the feature generator
    * @param resources
    *          the resources for the name finder or null if none
    *
    * @return the newly trained model
    *
    * @throws IOException
    */
   public static NameModel train(String languageCode, String type, ObjectStream<CorpusSample> samples,
       TrainingParameters trainParams, AdaptiveFeatureGenerator generator, final Map<String, Object> resources) throws IOException {

     if (languageCode == null) {
       throw new IllegalArgumentException("languageCode must not be null!");
     }
     
     Map<String, String> manifestInfoEntries = new HashMap<String, String>();

     AdaptiveFeatureGenerator featureGenerator;

     if (generator != null)
       featureGenerator = generator;
     else
       featureGenerator = createFeatureGenerator();

     AbstractModel nameFinderModel;

     if (!TrainUtil.isSequenceTraining(trainParams.getSettings())) {
       EventStream eventStream = new NameFinderEventStream(samples, type,
           new DefaultNameContextGenerator(featureGenerator));

       nameFinderModel = TrainUtil.train(eventStream, trainParams.getSettings(), manifestInfoEntries);
     }
     else {
       CorpusSampleSequenceStream ss = new CorpusSampleSequenceStream(samples, featureGenerator);

       nameFinderModel = TrainUtil.train(ss, trainParams.getSettings(), manifestInfoEntries);
     }

     return new NameModel(languageCode, nameFinderModel,
         resources, manifestInfoEntries);
   }

   /**
    * Trains a name finder model.
    *
    * @param languageCode the language of the training data
    * @param type null or an override type for all types in the training data
    * @param samples the training data
    * @param iterations the number of iterations
    * @param cutoff
    * @param resources the resources for the name finder or null if none
    *
    * @return the newly trained model
    *
    * @throws IOException
    * @throws ObjectStreamException
    */
   public static NameModel train(String languageCode, String type, ObjectStream<CorpusSample> samples,
       AdaptiveFeatureGenerator generator, final Map<String, Object> resources,
       int iterations, int cutoff) throws IOException {
     return train(languageCode, type, samples, ModelUtil.createTrainingParameters(iterations, cutoff),
         generator, resources);
   }

  /**
   * Gets the name type from the outcome
   * @param outcome the outcome
   * @return the name type, or null if not set
   */
  static final String extractNameType(String outcome) {
    Matcher matcher = typedOutcomePattern.matcher(outcome);
    if(matcher.matches()) {
      String nameType = matcher.group(1);
      return nameType;
    }

    return null;
  }

  /**
   * Removes spans with are intersecting or crossing in anyway.
   *
   * <p>
   * The following rules are used to remove the spans:<br>
   * Identical spans: The first span in the array after sorting it remains<br>
   * Intersecting spans: The first span after sorting remains<br>
   * Contained spans: All spans which are contained by another are removed<br>
   *
   * @param spans
   *
   * @return non-overlapping spans
   */
  public static Span[] dropOverlappingSpans(Span spans[]) {

    List<Span> sortedSpans = new ArrayList<Span>(spans.length);
    Collections.addAll(sortedSpans, spans);
    Collections.sort(sortedSpans);

    Iterator<Span> it = sortedSpans.iterator();


    Span lastSpan = null;

    while (it.hasNext()) {
      Span span = it.next();

      if (lastSpan != null) {
        if (lastSpan.intersects(span)) {
          it.remove();
          span = lastSpan;
        }
      }

      lastSpan = span;
    }

    return sortedSpans.toArray(new Span[sortedSpans.size()]);
  }

}
