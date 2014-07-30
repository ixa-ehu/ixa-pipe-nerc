/*
 *  Copyright 2014 Rodrigo Agerri

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
 */
package es.ehu.si.ixa.pipe.nerc.dict;

import java.util.HashMap;
import java.util.Map;

/**
 * It defines a Dictionary class consisting of a HashMap.
 * 
 * @author ragerri
 * @version 2014/06/25
 * 
 */
public class Dictionary {

  /**
   * The Map to store the dictionary.
   */
  private Map<String, String> dictMap;

  /**
   * Construct a Dictionary with a Map of Strings.
   * The key can be a Named Entity token and the value
   * its Named Entity class.
   * 
   * @param aMap
   *          the map of strings
   */
  public Dictionary() {
    this.dictMap = new HashMap<String, String>();
  }

  /**
   * Get the Map dictionary.
   * 
   * @return the dictionary as a map
   */
  public final Map<String, String> getDict() {
    return dictMap;
  }

  /**
   * Put a Named Entity token as key and its
   * Named Entity class as value.
   * 
   * @param name
   * @param neType
   */
  public void populate(String name, String neType) {
    dictMap.put(name, neType);
  }

  /**
   * Get the <key,value> size of the dictionary.
   * @return maximum token count in the dictionary
   */
  public int getMaxTokenCount() {
    return dictMap.size();
  }

}
