package es.ehu.si.ixa.pipe.nerc.features;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import opennlp.tools.util.featuregen.AdaptiveFeatureGenerator;

public class Prev2MapFeatureGenerator implements AdaptiveFeatureGenerator {
  
  private Map<String, String> previousMap = new HashMap<String, String>();

  public void createFeatures(List<String> features, String[] tokens, int index, String[] preds) {
    
    if (index > 0) {
      features.add("ppd=" + previousMap.get(tokens[index]) + "," + previousMap.get(tokens[index - 1]));
    }
  }

  /**
   * Generates previous decision features for the token based on contents of the previous map.
   */
  public void updateAdaptiveData(String[] tokens, String[] outcomes) {

    for (int i = 0; i < tokens.length; i++) {
      previousMap.put(tokens[i], outcomes[i]);
    }
  }

  /**
   * Clears the previous map.
   */
  public void clearAdaptiveData() {
    previousMap.clear();
  }

}
