package es.ehu.si.ixa.pipe.nerc.features;

import java.util.List;
import java.util.Map;

import opennlp.tools.util.InvalidFormatException;
import opennlp.tools.util.featuregen.CustomFeatureGenerator;
import opennlp.tools.util.featuregen.FeatureGeneratorResourceProvider;
import es.ehu.si.ixa.pipe.nerc.dict.Dictionary;

public class BrownTokenClassFeatureGenerator extends CustomFeatureGenerator {

  private Dictionary brownLexicon;
  
  public BrownTokenClassFeatureGenerator() {
  }

  public void createFeatures(List<String> features, String[] tokens, int index,
      String[] previousOutcomes) {
    
    String tokenShape = TokenClassFeatureGenerator.tokenShapeFeature(tokens[index]);
    List<String> wordClasses = BrownTokenClasses.getWordClasses(tokens[index], brownLexicon);
    
    for (int i = 0; i < wordClasses.size(); i++) {
      features.add("c,brown=" + tokenShape + "," + wordClasses.get(i));
    }
    
  }

  @Override
  public void updateAdaptiveData(String[] tokens, String[] outcomes) {
    
  }

  @Override
  public void clearAdaptiveData() {
    
  }

  @Override
  public void init(Map<String, String> properties,
      FeatureGeneratorResourceProvider resourceProvider)
      throws InvalidFormatException {
    this.brownLexicon = XMLFeatureDescriptor.brownCluster.getDictionary();
    
  }
  
}
