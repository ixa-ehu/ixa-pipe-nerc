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

import opennlp.tools.ml.BeamSearch;
import opennlp.tools.ml.EventModelSequenceTrainer;
import opennlp.tools.ml.EventTrainer;
import opennlp.tools.ml.SequenceTrainer;
import opennlp.tools.ml.TrainerFactory;
import opennlp.tools.ml.TrainerFactory.TrainerType;
import opennlp.tools.ml.model.Event;
import opennlp.tools.ml.model.MaxentModel;
import opennlp.tools.ml.model.SequenceClassificationModel;
import opennlp.tools.namefind.BioCodec;
import opennlp.tools.util.ObjectStream;
import opennlp.tools.util.Sequence;
import opennlp.tools.util.SequenceCodec;
import opennlp.tools.util.SequenceValidator;
import opennlp.tools.util.Span;
import opennlp.tools.util.TrainingParameters;
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


public class NameClassifier {
  
  private static String[][] EMPTY = new String[0][0];
  public static final int DEFAULT_BEAM_SIZE = 3;
  private static final Pattern typedOutcomePattern = Pattern.compile("(.+)-\\w+");



  public static final String START = "start";
  public static final String CONTINUE = "cont";
  public static final String OTHER = "other";
  
  private SequenceCodec<String> seqCodec = new BioCodec();
  protected SequenceClassificationModel<String> model;
  
  protected NameContextGenerator contextGenerator;
  private Sequence bestSequence;
  private AdditionalContextFeatureGenerator additionalContextFeatureGenerator =
      new AdditionalContextFeatureGenerator();
  private SequenceValidator<String> sequenceValidator;

  public NameClassifier(NameModel model) {
    NameClassifierFactory factory = model.getFactory();
    seqCodec = factory.createSequenceCodec();
    sequenceValidator = seqCodec.createSequenceValidator();
    this.model = model.getNameFinderSequenceModel();
    contextGenerator = factory.createContextGenerator();
    // TODO: We should deprecate this. And come up with a better solution!
    contextGenerator.addFeatureGenerator(
            new WindowFeatureGenerator(additionalContextFeatureGenerator, 8, 8));
  }
  
  static AdaptiveFeatureGenerator createFeatureGenerator() {
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
  * Generates name tags for the given sequence, typically a sentence, returning
  * token spans for any identified names.
  *
  * @param tokens an array of the tokens or words of the sequence, typically a
  * sentence.
  * @param additionalContext features which are based on context outside of the
  * sentence but which should also be used.
  *
  * @return an array of spans for each of the names identified.
  */
 public Span[] find(String[] tokens, String[][] additionalContext) {

   additionalContextFeatureGenerator.setCurrentContext(additionalContext);

   bestSequence = model.bestSequence(tokens, additionalContext, contextGenerator, sequenceValidator);

   List<String> c = bestSequence.getOutcomes();

   contextGenerator.updateAdaptiveData(tokens, c.toArray(new String[c.size()]));
   Span[] spans = seqCodec.decode(c);
   spans = setProbs(spans);
   return spans;
 }
 
 /**
  * sets the probs for the spans
  *
  * @param spans
  * @return
  */
 private Span[] setProbs(Span[] spans) {
    double[] probs = probs(spans);
    if (probs != null) {    
      
     for (int i = 0; i < probs.length; i++) {
       double prob = probs[i];
       spans[i]= new Span(spans[i], prob);
     }
   }
   return spans;
 }
 
 /**
  * Returns an array of probabilities for each of the specified spans which is
  * the arithmetic mean of the probabilities for each of the outcomes which
  * make up the span.
  *
  * @param spans The spans of the names for which probabilities are desired.
  *
  * @return an array of probabilities for each of the specified spans.
  */
 public double[] probs(Span[] spans) {

   double[] sprobs = new double[spans.length];
   double[] probs = bestSequence.getProbs();

   for (int si = 0; si < spans.length; si++) {

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

   public static NameModel train(String languageCode, String type,
           ObjectStream<CorpusSample> samples, TrainingParameters trainParams,
           NameClassifierFactory factory) throws IOException {
     String beamSizeString = trainParams.getSettings().get(BeamSearch.BEAM_SIZE_PARAMETER);

     int beamSize = NameClassifier.DEFAULT_BEAM_SIZE;
     if (beamSizeString != null) {
       beamSize = Integer.parseInt(beamSizeString);
     }

     Map<String, String> manifestInfoEntries = new HashMap<String, String>();

     MaxentModel nameFinderModel = null;

     SequenceClassificationModel<String> seqModel = null;

     TrainerType trainerType = TrainerFactory.getTrainerType(trainParams.getSettings());

     if (TrainerType.EVENT_MODEL_TRAINER.equals(trainerType)) {
       ObjectStream<Event> eventStream = new NameFinderEventStream(samples, type,
               factory.createContextGenerator(), factory.createSequenceCodec());

       EventTrainer trainer = TrainerFactory.getEventTrainer(trainParams.getSettings(), manifestInfoEntries);
       nameFinderModel = trainer.train(eventStream);
     } // TODO: Maybe it is not a good idea, that these two don't use the context generator ?!
     // These also don't use the sequence codec ?!
     else if (TrainerType.EVENT_MODEL_SEQUENCE_TRAINER.equals(trainerType)) {
       CorpusSampleSequenceStream ss = new CorpusSampleSequenceStream(samples, factory.createContextGenerator());

       EventModelSequenceTrainer trainer = TrainerFactory.getEventModelSequenceTrainer(
               trainParams.getSettings(), manifestInfoEntries);
       nameFinderModel = trainer.train(ss);
     } else if (TrainerType.SEQUENCE_TRAINER.equals(trainerType)) {
       SequenceTrainer trainer = TrainerFactory.getSequenceModelTrainer(
               trainParams.getSettings(), manifestInfoEntries);

       CorpusSampleSequenceStream ss = new CorpusSampleSequenceStream(samples, factory.createContextGenerator(), false);
       seqModel = trainer.train(ss);
     } else {
       throw new IllegalStateException("Unexpected trainer type!");
     }

     if (seqModel != null) {
       return new NameModel(languageCode, seqModel, null,
               factory.getResources(), manifestInfoEntries, factory.getSequenceCodec());
     } else {
       return new NameModel(languageCode, nameFinderModel, beamSize, null,
               factory.getResources(), manifestInfoEntries, factory.getSequenceCodec());
     }
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
