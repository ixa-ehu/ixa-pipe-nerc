package es.ehu.si.ixa.pipe.nerc.features;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import es.ehu.si.ixa.pipe.nerc.dict.Dictionary;

public class BrownPreviousMapFeatureGenerator implements AdaptiveFeatureGenerator {
  
  private Dictionary brownLexicon;
  private Map<List<String>, String> previousMap = new HashMap<List<String>, String>();
  List<String> wordClass = new ArrayList<String>();
  
  public BrownPreviousMapFeatureGenerator(Dictionary aBrownLexicon) {
    this.brownLexicon = aBrownLexicon;
  }

  public void createFeatures(List<String> features, String[] tokens, int index,
      String[] previousOutcomes) {
    
    for (int i = 0; i < wordClass.size(); i++) {
      features.add("pd,brown=" + previousMap.get(wordClass));
    }
  }
  
  public void updateAdaptiveData(String[] tokens, String[] outcomes) {
    for (int i = 0; i < tokens.length; i++) {
      wordClass = BrownTokenClasses.getWordClasses(tokens[i], brownLexicon);
      previousMap.put(wordClass, outcomes[i]);
    }
  }
  /**
   * Clears the previous map.
   */
  public void clearAdaptiveData() {
    previousMap.clear();
    wordClass.clear();
  }

}
