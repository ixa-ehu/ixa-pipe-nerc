package es.ehu.si.ixa.ixa.pipe.nerc.features;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import opennlp.tools.util.InvalidFormatException;
import opennlp.tools.util.featuregen.ArtifactToSerializerMapper;
import opennlp.tools.util.featuregen.CustomFeatureGenerator;
import opennlp.tools.util.featuregen.FeatureGeneratorResourceProvider;
import opennlp.tools.util.model.ArtifactSerializer;
import es.ehu.si.ixa.ixa.pipe.nerc.dict.ClarkCluster;

public class ClarkFeatureGenerator extends CustomFeatureGenerator implements ArtifactToSerializerMapper {

  private ClarkCluster clarkCluster;
  private Map<String, String> attributes;
  public static String unknownClarkClass = "noclarkclass";

  
  public ClarkFeatureGenerator() {
  }
  
  public void createFeatures(List<String> features, String[] tokens, int index,
      String[] preds) {

    String wordClass = getWordClass(tokens[index].toLowerCase());
    features.add(attributes.get("dict") + "=" + wordClass);
  }

  public String getWordClass(String token) {
    String distSim = clarkCluster.lookupToken(token);
    if (distSim == null) {
      distSim = unknownClarkClass;
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
    Object dictResource = resourceProvider.getResource(properties.get("dict"));
    if (!(dictResource instanceof ClarkCluster)) {
      throw new InvalidFormatException("Not a ClarkCluster resource for key: " + properties.get("dict"));
    }
    this.clarkCluster = (ClarkCluster) dictResource;
    this.attributes = properties;
  }

  @Override
  public Map<String, ArtifactSerializer<?>> getArtifactSerializerMapping() {
    Map<String, ArtifactSerializer<?>> mapping = new HashMap<>();
    mapping.put("clarkserializer", new ClarkCluster.ClarkClusterSerializer());
    return Collections.unmodifiableMap(mapping);
  }
  
}
