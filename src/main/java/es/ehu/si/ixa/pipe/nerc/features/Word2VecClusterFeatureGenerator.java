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
import es.ehu.si.ixa.pipe.nerc.dict.Word2VecCluster;

public class Word2VecClusterFeatureGenerator extends CustomFeatureGenerator implements ArtifactToSerializerMapper {
  
  private Word2VecCluster word2vecCluster;
  private static String unknownClass = "noWord2Vec";
  private Map<String, String> attributes;
  
  
  public Word2VecClusterFeatureGenerator() {
  }
  
  public void createFeatures(List<String> features, String[] tokens, int index,
      String[] previousOutcomes) {
    
    String wordClass = getWordClass(tokens[index].toLowerCase());
    features.add(attributes.get("dict") + "=" + wordClass);
  }
  
  private String getWordClass(String token) {
    String wordClass = word2vecCluster.lookupToken(token);
    if (wordClass == null) {
      wordClass = unknownClass;
    }
    return wordClass;
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
    if (!(dictResource instanceof Word2VecCluster)) {
      throw new InvalidFormatException("Not a Word2VecCluster resource for key: " + properties.get("dict"));
    }
    this.word2vecCluster = (Word2VecCluster) dictResource;
    this.attributes = properties;
  }
  
  @Override
  public Map<String, ArtifactSerializer<?>> getArtifactSerializerMapping() {
    Map<String, ArtifactSerializer<?>> mapping = new HashMap<>();
    mapping.put("word2vecserializer", new Word2VecCluster.Word2VecClusterSerializer());
    return Collections.unmodifiableMap(mapping);
  }
}
