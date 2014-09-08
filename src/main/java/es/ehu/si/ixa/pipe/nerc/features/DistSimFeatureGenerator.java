package es.ehu.si.ixa.pipe.nerc.features;

import es.ehu.si.ixa.pipe.nerc.dict.Dictionary;

import java.util.List;


public class DistSimFeatureGenerator extends FeatureGeneratorAdapter {

  
  private Dictionary distSimLexicon;
  public static String unknowndistSimClass = "JAR";

  public DistSimFeatureGenerator(Dictionary distSimLexicon) {
    this.distSimLexicon = distSimLexicon;
  }
  
  public void createFeatures(List<String> features, String[] tokens, int index,
      String[] preds) {
    
      String wordClass = getWordClass(tokens[index].toLowerCase());
      features.add("DISTSIM=" + wordClass);
    }
  
  private String getWordClass(String token) {
    
    String distSim = distSimLexicon.getDict().get(token);
    if (distSim == null) {
      distSim = unknowndistSimClass;
    }
    return distSim;
  }
  
}