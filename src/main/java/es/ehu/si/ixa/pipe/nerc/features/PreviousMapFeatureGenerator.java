package es.ehu.si.ixa.pipe.nerc.features;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import opennlp.tools.util.featuregen.AdaptiveFeatureGenerator;


public class PreviousMapFeatureGenerator implements AdaptiveFeatureGenerator {

 private Map<String, String> previousMap = new HashMap<String, String>();

 public void createFeatures(List<String> features, String[] tokens, int index, String[] preds) {
   features.add("pd=" + previousMap.get(tokens[index]));
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
