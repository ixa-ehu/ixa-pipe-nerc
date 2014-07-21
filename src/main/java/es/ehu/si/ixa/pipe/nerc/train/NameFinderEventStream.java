package es.ehu.si.ixa.pipe.nerc.train;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import opennlp.model.Event;
import opennlp.tools.util.ObjectStream;
import opennlp.tools.util.Span;
import es.ehu.si.ixa.pipe.nerc.features.AdditionalContextFeatureGenerator;
import es.ehu.si.ixa.pipe.nerc.features.NameContextGenerator;
import es.ehu.si.ixa.pipe.nerc.features.WindowFeatureGenerator;

public class NameFinderEventStream extends opennlp.tools.util.AbstractEventStream<CorpusSample> {

 private NameContextGenerator contextGenerator;

 private AdditionalContextFeatureGenerator additionalContextFeatureGenerator = new AdditionalContextFeatureGenerator();

 private String type;
 
 /**
  * Creates a new name finder event stream using the specified data stream and context generator.
  * @param dataStream The data stream of events.
  * @param type null or overrides the type parameter in the provided samples
  * @param contextGenerator The context generator used to generate features for the event stream.
  */
 public NameFinderEventStream(ObjectStream<CorpusSample> dataStream, String type, NameContextGenerator contextGenerator) {
   super(dataStream);
   
   this.contextGenerator = contextGenerator;
   this.contextGenerator.addFeatureGenerator(new WindowFeatureGenerator(additionalContextFeatureGenerator, 8, 8));
   
   if (type != null)
     this.type = type;
   else
     this.type = "default";
 }

 public NameFinderEventStream(ObjectStream<CorpusSample> dataStream) {
   this(dataStream, null, new DefaultNameContextGenerator());
 }

 /**
  * Generates the name tag outcomes (start, continue, other) for each token in a sentence
  * with the specified length using the specified name spans.
  * @param names Token spans for each of the names.
  * @param type null or overrides the type parameter in the provided samples
  * @param length The length of the sentence.
  * @return An array of start, continue, other outcomes based on the specified names and sentence length.
  */
 public static String[] generateOutcomes(Span[] names, String type, int length) {
   String[] outcomes = new String[length];
   for (int i = 0; i < outcomes.length; i++) {
     outcomes[i] = NameClassifier.OTHER;
   }
   for (Span name : names) {
     if (name.getType() == null) {
       outcomes[name.getStart()] = type + "-" + NameClassifier.START;
     }
     else {
       outcomes[name.getStart()] = name.getType() + "-" + NameClassifier.START;
     }
     // now iterate from begin + 1 till end
     for (int i = name.getStart() + 1; i < name.getEnd(); i++) {
       if (name.getType() == null) {
         outcomes[i] = type + "-" + NameClassifier.CONTINUE;
       }
       else {
         outcomes[i] = name.getType() + "-" + NameClassifier.CONTINUE;
       }
     }
   }
   return outcomes;
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
   
   String outcomes[] = generateOutcomes(sample.getNames(), type, sample.getSentence().length);
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
