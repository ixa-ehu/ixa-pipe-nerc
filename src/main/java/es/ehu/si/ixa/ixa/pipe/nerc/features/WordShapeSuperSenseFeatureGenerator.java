/*
 *  Copyright 2015 Rodrigo Agerri

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
package es.ehu.si.ixa.ixa.pipe.nerc.features;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import opennlp.tools.util.InvalidFormatException;
import opennlp.tools.util.featuregen.CustomFeatureGenerator;
import opennlp.tools.util.featuregen.FeatureGeneratorResourceProvider;


/**
 * Ciaramita and Altun (2006) word shape features.
 * @author ragerri
 * @version 2015-03-17
 */
public class WordShapeSuperSenseFeatureGenerator extends CustomFeatureGenerator {
  
  private static Pattern duplicateCharacters = Pattern.compile("([a-z\\d])\\1", Pattern.CASE_INSENSITIVE);

  public WordShapeSuperSenseFeatureGenerator() {
    
  }

  public void createFeatures(List<String> features, String[] tokens, int index,
      String[] preds) {
    
    String normalizedToken = normalize(tokens[index]);
    features.add("sh=" + normalizedToken);
    
    if (Character.isLowerCase(tokens[index].charAt(0))) {
      features.add("sh=" + "low");
    } else if (Character.isUpperCase(tokens[index].charAt(0))) {
        if (index > 0 && (tokens[index - 1].equalsIgnoreCase("?") ||
            tokens[index -1].equalsIgnoreCase("?") || 
            tokens[index -1].equalsIgnoreCase("."))) {
          features.add("sh=" + "cap_brk");
        } else if (index == 0) {
          features.add("sh=" + "cap_brk");
        } else {
          features.add("sh=" + "cap_nobrk");
        }
      }
  }

  /**
   * Normalize upper case, lower case, digits and duplicate characters.
   * @param token the token to be normalized
   * @return the normalized token
   */
  public static String normalize(String token) {
    Matcher duplicateMatcher = duplicateCharacters.matcher(token);
    String normalizedToken = duplicateMatcher.replaceAll("#");
    normalizedToken = normalizedToken.replaceAll("[A-Z]", "X");
    normalizedToken = normalizedToken.replaceAll("[a-z]", "x");
    normalizedToken = normalizedToken.replaceAll("[0-9]", "d");
    return normalizedToken;
  }

  @Override
  public void clearAdaptiveData() {
    
  }

  @Override
  public void updateAdaptiveData(String[] tokens, String[] outcomes) {
    
  }

  @Override
  public void init(Map<String, String> properties,
      FeatureGeneratorResourceProvider resources) throws InvalidFormatException {
    
  }

}

