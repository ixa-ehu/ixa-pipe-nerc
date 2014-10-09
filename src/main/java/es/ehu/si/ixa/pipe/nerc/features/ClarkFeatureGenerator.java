package es.ehu.si.ixa.pipe.nerc.features;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import opennlp.tools.util.InvalidFormatException;
import opennlp.tools.util.featuregen.ArtifactToSerializerMapper;
import opennlp.tools.util.featuregen.CustomFeatureGenerator;
import opennlp.tools.util.featuregen.FeatureGeneratorResourceProvider;
import opennlp.tools.util.model.ArtifactSerializer;
import es.ehu.si.ixa.pipe.nerc.dict.ClarkCluster;


public class ClarkFeatureGenerator extends CustomFeatureGenerator implements ArtifactToSerializerMapper {

  
  private ClarkCluster clarkCluster;
  public static String unknowndistSimClass = "JAR";
  private Map<String, ArtifactSerializer<?>> mapping;
  private Map<String, String> attributes;

  public ClarkFeatureGenerator() {
  }
  
  public void createFeatures(List<String> features, String[] tokens, int index,
      String[] preds) {
    
      String wordClass = getWordClass(tokens[index].toLowerCase());
      features.add("clark=" + wordClass);
    }
  
  public String getWordClass(String token) {
    
    String distSim = clarkCluster.lookupToken(token);
    if (distSim == null) {
      distSim = unknowndistSimClass;
    }
    return distSim;
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
    this.attributes = properties;
  }

  @Override
  public Map<String, ArtifactSerializer<?>> getArtifactSerializerMapping() {
    mapping.put("clarklexicon", new ClarkCluster.ClarkClusterSerializer());
    return Collections.unmodifiableMap(mapping);
  }
  
}
