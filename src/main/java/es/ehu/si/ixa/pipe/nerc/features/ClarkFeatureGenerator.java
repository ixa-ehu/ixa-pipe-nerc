package es.ehu.si.ixa.pipe.nerc.features;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import opennlp.tools.cmdline.CmdLineUtil;
import opennlp.tools.util.InvalidFormatException;
import opennlp.tools.util.featuregen.ArtifactToSerializerMapper;
import opennlp.tools.util.featuregen.CustomFeatureGenerator;
import opennlp.tools.util.featuregen.FeatureGeneratorResourceProvider;
import opennlp.tools.util.model.ArtifactSerializer;
import es.ehu.si.ixa.pipe.nerc.dict.ClarkCluster;


public class ClarkFeatureGenerator extends CustomFeatureGenerator implements ArtifactToSerializerMapper {

  
  private ClarkCluster clarkCluster;
  public static String unknownClarkClass = "noclarkclass";
  private Map<String, String> attributes;

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
    this.attributes = properties;
    InputStream inputStream = CmdLineUtil.openInFile(new File(properties.get("dict")));
    try {
      this.clarkCluster = new ClarkCluster.ClarkClusterSerializer().create(inputStream);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  @Override
  public Map<String, ArtifactSerializer<?>> getArtifactSerializerMapping() {
    Map<String, ArtifactSerializer<?>> mapping = new HashMap<>();
    mapping.put("clarkcluster", new ClarkCluster.ClarkClusterSerializer());
    return Collections.unmodifiableMap(mapping);
  }
  
}
