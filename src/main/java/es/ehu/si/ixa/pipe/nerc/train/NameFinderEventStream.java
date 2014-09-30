package es.ehu.si.ixa.pipe.nerc.train;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import opennlp.tools.ml.model.Event;
import opennlp.tools.namefind.BioCodec;
import opennlp.tools.util.ObjectStream;
import opennlp.tools.util.SequenceCodec;
import es.ehu.si.ixa.pipe.nerc.features.AdditionalContextFeatureGenerator;
import es.ehu.si.ixa.pipe.nerc.features.NameContextGenerator;
import es.ehu.si.ixa.pipe.nerc.features.WindowFeatureGenerator;
import es.ehu.si.ixa.pipe.nerc.formats.CorpusSample;

public class NameFinderEventStream extends opennlp.tools.util.AbstractEventStream<CorpusSample> {

  private NameContextGenerator contextGenerator;

  private AdditionalContextFeatureGenerator additionalContextFeatureGenerator = new AdditionalContextFeatureGenerator();

  private String type;

  private SequenceCodec<String> codec;

  /**
   * Creates a new name finder event stream using the specified data stream and context generator.
   * @param dataStream The data stream of events.
   * @param type null or overrides the type parameter in the provided samples
   * @param contextGenerator The context generator used to generate features for the event stream.
   */
  public NameFinderEventStream(ObjectStream<CorpusSample> dataStream, String type, NameContextGenerator contextGenerator, SequenceCodec codec) {
    super(dataStream);

    this.codec = codec;

    if (codec == null) {
      this.codec = new BioCodec();
    }

    this.contextGenerator = contextGenerator;
    this.contextGenerator.addFeatureGenerator(new WindowFeatureGenerator(additionalContextFeatureGenerator, 8, 8));

    if (type != null)
      this.type = type;
    else
      this.type = "default";
  }

  public static List<Event> generateEvents(String[] sentence, String[] outcomes, NameContextGenerator cg) {
    List<Event> events = new ArrayList<Event>(outcomes.length);
    for (int i = 0; i < outcomes.length; i++) {
      events.add(new Event(outcomes[i], cg.getContext(i, sentence, outcomes,null)));
    }

    cg.updateAdaptiveData(sentence, outcomes);

    return events;
  }

  @Override
  protected Iterator<Event> createEvents(CorpusSample sample) {

    if (sample.isClearAdaptiveDataSet()) {
      contextGenerator.clearAdaptiveData();
    }

    String outcomes[] = codec.encode(sample.getNames(), sample.getSentence().length);
//    String outcomes[] = generateOutcomes(sample.getNames(), type, sample.getSentence().length);
    additionalContextFeatureGenerator.setCurrentContext(sample.getAdditionalContext());
    String[] tokens = new String[sample.getSentence().length];

    for (int i = 0; i < sample.getSentence().length; i++) {
      tokens[i] = sample.getSentence()[i];
    }

    return generateEvents(tokens, outcomes, contextGenerator).iterator();
  }


  /**
   * Generated previous decision features for each token based on contents of the specified map.
   * @param tokens The token for which the context is generated.
   * @param prevMap A mapping of tokens to their previous decisions.
   * @return An additional context array with features for each token.
   */
  public static String[][] additionalContext(String[] tokens, Map<String, String> prevMap) {
    String[][] ac = new String[tokens.length][1];
    for (int ti=0;ti<tokens.length;ti++) {
      String pt = prevMap.get(tokens[ti]);
      ac[ti][0]="pd="+pt;
    }
    return ac;

  }
}
