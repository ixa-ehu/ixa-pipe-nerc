package es.ehu.si.ixa.pipe.nerc.features;

import java.util.List;

import es.ehu.si.ixa.pipe.nerc.dict.Dictionary;

public class BrownBigramFeatureGenerator extends FeatureGeneratorAdapter {
  
  private Dictionary brownLexicon;
  
  public BrownBigramFeatureGenerator(Dictionary aBrownLexicon) {
    this.brownLexicon = aBrownLexicon;
  }

  public void createFeatures(List<String> features, String[] tokens, int index,
      String[] previousOutcomes) {
    
    List<String> wordClasses = BrownTokenClasses.getWordClasses(tokens[index], brownLexicon);
    if (index > 0) {
      List<String> prevWordClasses = BrownTokenClasses.getWordClasses(tokens[index - 1], brownLexicon);
      for (int i = 0; i < wordClasses.size() && i < prevWordClasses.size(); i++)
      features.add("pbrown,brown=" + prevWordClasses.get(i) + "," + wordClasses.get(i));
    }
    if (index + 1 > tokens.length) {
      List<String> nextWordClasses = BrownTokenClasses.getWordClasses(tokens[index + 1], brownLexicon);
      for (int i = 0; i < wordClasses.size() && i < nextWordClasses.size(); i++) {
        features.add("brown,nbrown=" + wordClasses.get(i) + "," + nextWordClasses.get(i));
      }
    }
  }
  
  

}
