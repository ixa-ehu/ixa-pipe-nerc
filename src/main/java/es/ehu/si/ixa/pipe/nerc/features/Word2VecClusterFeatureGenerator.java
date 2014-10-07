package es.ehu.si.ixa.pipe.nerc.features;

import java.util.List;

import es.ehu.si.ixa.pipe.nerc.dict.Dictionary;

public class Word2VecClusterFeatureGenerator extends FeatureGeneratorAdapter {
  
  private Dictionary word2vecLexicon;
  private static String unknownClass = "noWord2Vec";
  
  
  public Word2VecClusterFeatureGenerator(Dictionary aWord2vecLexicon) {
    this.word2vecLexicon = aWord2vecLexicon;
  }
  
  public void createFeatures(List<String> features, String[] tokens, int index,
      String[] previousOutcomes) {
    
    String wordClass = getWordClass(tokens[index].toLowerCase());
    features.add("Word2VecCluster=" + wordClass);
  }
  
  private String getWordClass(String token) {
    String wordClass = word2vecLexicon.getDict().get(token);
    if (wordClass == null) {
      wordClass = unknownClass;
    }
    return wordClass;
  }
  
}
