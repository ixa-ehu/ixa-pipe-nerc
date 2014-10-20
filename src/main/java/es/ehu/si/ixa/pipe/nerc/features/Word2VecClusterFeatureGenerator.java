package es.ehu.si.ixa.pipe.nerc.features;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

import opennlp.tools.util.InvalidFormatException;
import opennlp.tools.util.featuregen.ArtifactToSerializerMapper;
import opennlp.tools.util.featuregen.CustomFeatureGenerator;
import opennlp.tools.util.featuregen.FeatureGeneratorResourceProvider;
import opennlp.tools.util.model.ArtifactSerializer;
import es.ehu.si.ixa.pipe.nerc.dict.Word2VecCluster;
import es.ehu.si.ixa.pipe.nerc.train.InputOutputUtils;

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
  public Map<String, ArtifactSerializer<?>> getArtifactSerializerMapping() {
    return null;
  }

  @Override
  public void init(Map<String, String> properties,
      FeatureGeneratorResourceProvider resourceProvider)
      throws InvalidFormatException {
    this.attributes = properties;
    InputStream inputStream = InputOutputUtils.getDictionaryResource(properties.get("dict"));
    try {
      this.word2vecCluster = new Word2VecCluster.Word2VecClusterSerializer().create(inputStream);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
  
}
