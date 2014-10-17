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
import es.ehu.si.ixa.pipe.nerc.dict.BrownCluster;

public class BrownTokenFeatureGenerator extends CustomFeatureGenerator implements ArtifactToSerializerMapper {

  private BrownCluster brownLexicon;
  private Map<String, String> attributes;
  private static boolean DEBUG = false;

  public BrownTokenFeatureGenerator() {
  }
  
  public void createFeatures(List<String> features, String[] tokens, int index,
      String[] previousOutcomes) {
    
    List<String> wordClasses = BrownTokenClasses.getWordClasses(tokens[index], brownLexicon);
    if (DEBUG) {
      BrownTokenClasses.printList(wordClasses);
    }
    for (int i = 0; i < wordClasses.size(); i++) {
      features.add(attributes.get("dict") + "=" + wordClasses.get(i));
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
    this.attributes = properties;
    InputStream inputStream = CmdLineUtil.openInFile(new File(properties.get("dict")));
    try {
      this.brownLexicon = new BrownCluster.BrownClusterSerializer().create(inputStream);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  @Override
  public Map<String, ArtifactSerializer<?>> getArtifactSerializerMapping() {
    Map<String, ArtifactSerializer<?>> mapping = new HashMap<>();
    mapping.put("browncluster", new BrownCluster.BrownClusterSerializer());
    return Collections.unmodifiableMap(mapping);
  }

  

}
