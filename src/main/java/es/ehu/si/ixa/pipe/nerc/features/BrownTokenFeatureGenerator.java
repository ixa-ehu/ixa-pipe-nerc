package es.ehu.si.ixa.pipe.nerc.features;

import java.util.List;

import es.ehu.si.ixa.pipe.nerc.dict.Dictionary;

public class BrownTokenFeatureGenerator extends FeatureGeneratorAdapter {

  private Dictionary brownLexicon;
  private static boolean DEBUG = false;

  public BrownTokenFeatureGenerator(Dictionary aBrownLexicon) {
    this.brownLexicon = aBrownLexicon;
  }
  
  public void createFeatures(List<String> features, String[] tokens, int index,
      String[] previousOutcomes) {
    
    List<String> wordClasses = BrownTokenClasses.getWordClasses(tokens[index], brownLexicon);
    if (DEBUG) {
      BrownTokenClasses.printList(wordClasses);
    }
    for (int i = 0; i < wordClasses.size(); i++) {
      features.add("brown=" + wordClasses.get(i));
    }
  }

  

}
