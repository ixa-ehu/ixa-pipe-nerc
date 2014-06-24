package es.ehu.si.ixa.pipe.nerc;

import java.util.HashMap;
import java.util.Map;

/**
 * It defines a Dictionary class consisting of a HashMap.
 *
 * @author ragerri
 *
 */
public class Dictionary {

  /**
   * The list to store the dictionary.
   */
  private Map<String, String> dictMap;

  /**
   * Construct a Dictionary with a Map of Strings.
   *
   * @param aMap the map of strings
   */
  public Dictionary() {
      this.dictMap = new HashMap<String, String>();
  }

  /**
   * Get the dictionary.
   *
   * @return the dictionary as a map
   */
  public final Map<String, String> getDict() {
    return dictMap;
  }
  
  public void populate(String name, String neType) {
    dictMap.put(name, neType);
  }
  
  
  /**
   * 
   * @return maximum token count in the dictionary
   */
  public int getMaxTokenCount() {
      return dictMap.size();
  }
  

}
