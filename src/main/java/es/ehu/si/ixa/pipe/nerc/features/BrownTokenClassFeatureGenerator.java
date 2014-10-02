package es.ehu.si.ixa.pipe.nerc.features;

import java.util.List;

import opennlp.tools.util.featuregen.FeatureGeneratorAdapter;

import es.ehu.si.ixa.pipe.nerc.dict.Dictionary;

public class BrownTokenClassFeatureGenerator extends FeatureGeneratorAdapter {

  private Dictionary brownLexicon;
  
  public BrownTokenClassFeatureGenerator(Dictionary aBrownLexicon) {
    this.brownLexicon = aBrownLexicon;
  }

  public void createFeatures(List<String> features, String[] tokens, int index,
      String[] previousOutcomes) {
    
    String tokenShape = TokenClassFeatureGenerator.tokenShapeFeature(tokens[index]);
    List<String> wordClasses = BrownTokenClasses.getWordClasses(tokens[index], brownLexicon);
    
    for (int i = 0; i < wordClasses.size(); i++) {
      features.add("c,brown=" + tokenShape + "," + wordClasses.get(i));
    }
    
  }
  
}
