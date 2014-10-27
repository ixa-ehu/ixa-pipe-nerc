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

public class BrownTokenClassFeatureGenerator extends CustomFeatureGenerator implements ArtifactToSerializerMapper {

  private BrownCluster brownLexicon;
  private Map<String, String> attributes;
  
  public BrownTokenClassFeatureGenerator() {
  }

  public void createFeatures(List<String> features, String[] tokens, int index,
      String[] previousOutcomes) {
    
    String tokenShape = TokenClassFeatureGenerator.tokenShapeFeature(tokens[index]);
    List<String> wordClasses = BrownTokenClasses.getWordClasses(tokens[index], brownLexicon);
    
    for (int i = 0; i < wordClasses.size(); i++) {
      features.add("c," + attributes.get("dict") + "=" + tokenShape + "," + wordClasses.get(i));
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
