package es.ehu.si.ixa.pipe.nerc.features;

import es.ehu.si.ixa.pipe.nerc.dict.Dictionary;

import java.util.List;


public class ClarkFeatureGenerator extends FeatureGeneratorAdapter {

  
  private Dictionary clarkLexicon;
  public static String unknowndistSimClass = "JAR";

  public ClarkFeatureGenerator(Dictionary distSimLexicon) {
    this.clarkLexicon = distSimLexicon;
  }
  
  public void createFeatures(List<String> features, String[] tokens, int index,
      String[] preds) {
    
      String wordClass = getWordClass(tokens[index].toLowerCase());
      features.add("clark=" + wordClass);
    }
  
  public String getWordClass(String token) {
    
    String distSim = clarkLexicon.getDict().get(token);
    if (distSim == null) {
      distSim = unknowndistSimClass;
    }
    return distSim;
  }
  
}
