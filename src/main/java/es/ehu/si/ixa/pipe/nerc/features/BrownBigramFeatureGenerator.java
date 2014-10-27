package es.ehu.si.ixa.pipe.nerc.features;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import opennlp.tools.util.InvalidFormatException;
import opennlp.tools.util.featuregen.ArtifactToSerializerMapper;
import opennlp.tools.util.featuregen.CustomFeatureGenerator;
import opennlp.tools.util.featuregen.FeatureGeneratorResourceProvider;
import opennlp.tools.util.model.ArtifactSerializer;
import es.ehu.si.ixa.pipe.nerc.dict.BrownCluster;

public class BrownBigramFeatureGenerator extends CustomFeatureGenerator implements ArtifactToSerializerMapper {
  
  private BrownCluster brownLexicon;
  private Map<String, String> attributes;
  
  public BrownBigramFeatureGenerator() {
  }

  public void createFeatures(List<String> features, String[] tokens, int index,
      String[] previousOutcomes) {
    
    List<String> wordClasses = BrownTokenClasses.getWordClasses(tokens[index], brownLexicon);
    if (index > 0) {
      List<String> prevWordClasses = BrownTokenClasses.getWordClasses(tokens[index - 1], brownLexicon);
      for (int i = 0; i < wordClasses.size() && i < prevWordClasses.size(); i++)
      features.add("p" + attributes.get("dict") + "," + attributes.get("dict")+ "=" + prevWordClasses.get(i) + "," + wordClasses.get(i));
    }
    if (index + 1 > tokens.length) {
      List<String> nextWordClasses = BrownTokenClasses.getWordClasses(tokens[index + 1], brownLexicon);
      for (int i = 0; i < wordClasses.size() && i < nextWordClasses.size(); i++) {
        features.add(attributes.get("dict") + "," + "n" + attributes.get("dict") + "=" + wordClasses.get(i) + "," + nextWordClasses.get(i));
      }
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
    Object dictResource = resourceProvider.getResource(properties.get("dict"));
    if (!(dictResource instanceof BrownCluster)) {
      throw new InvalidFormatException("Not a BrownCluster resource for key: " + properties.get("dict"));
    }
    this.brownLexicon = (BrownCluster) dictResource;
    this.attributes = properties;
    
  }

  @Override
  public Map<String, ArtifactSerializer<?>> getArtifactSerializerMapping() {
    Map<String, ArtifactSerializer<?>> mapping = new HashMap<>();
    mapping.put("brownserializer", new BrownCluster.BrownClusterSerializer());
    return Collections.unmodifiableMap(mapping);
  }
  
  

}
