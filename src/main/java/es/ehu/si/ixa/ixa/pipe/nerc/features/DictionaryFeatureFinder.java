/*
 *Copyright 2014 Rodrigo Agerri

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

import java.util.LinkedList;
import java.util.List;

import opennlp.tools.util.Span;
import es.ehu.si.ixa.ixa.pipe.nerc.StringUtils;
import es.ehu.si.ixa.ixa.pipe.nerc.dict.Dictionary;

/**
 * It detects named entities by dictionary look-up for
 * DictionaryFeatureGenerator. The dictionary has the form: named entity\tclass
 * 
 * @author ragerri
 * 
 */
public class DictionaryFeatureFinder {

  /**
   * The dictionary to find the names.
   */
  private Dictionary dictionary;
  /**
   * Debugging switch.
   */
  private final boolean debug = false;

  public DictionaryFeatureFinder(final Dictionary aDictionary) {
    this.dictionary = aDictionary;
  }

  /**
   * Detects Named Entities against a {@link Dictionary} ignoring case.
   * 
   * @param tokens
   *          the tokenized sentence
   * @return spans of the Named Entities
   */
  public final Span[] nercToSpans(final String[] tokens) {
    List<Span> namesFound = new LinkedList<Span>();

    for (int offsetFrom = 0; offsetFrom < tokens.length; offsetFrom++) {
      Span nameFound = null;
      String tokensSearching[] = new String[] {};
      
      for (int offsetTo = offsetFrom; offsetTo < tokens.length; offsetTo++) {

        int lengthSearching = offsetTo - offsetFrom + 1;
        if (lengthSearching > dictionary.getMaxTokenCount()) {
          break;
        } else {
          tokensSearching = new String[lengthSearching];
          System.arraycopy(tokens, offsetFrom, tokensSearching, 0,
              lengthSearching);

          String entryForSearch = StringUtils.getStringFromTokens(
              tokensSearching);
          String entryValue = dictionary.lookup(entryForSearch.toLowerCase());
          if (entryValue != null) {
            nameFound = new Span(offsetFrom, offsetTo + 1, entryValue);
          }
        }
      }
      if (nameFound != null) {
        namesFound.add(nameFound);
        // skip over the found tokens for the next search
        offsetFrom += (nameFound.length() - 1);
      }
    }
    return namesFound.toArray(new Span[namesFound.size()]);
  }

  /**
   * Detects Named Entities in a {@link Dictionary} taking case into account.
   * 
   * @param tokens
   *          the tokenized sentence
   * @return spans of the Named Entities
   */
  public final Span[] nercToSpansExact(final String[] tokens) {
    List<Span> namesFound = new LinkedList<Span>();

    for (int offsetFrom = 0; offsetFrom < tokens.length; offsetFrom++) {
      Span nameFound = null;
      String tokensSearching[] = new String[] {};

      for (int offsetTo = offsetFrom; offsetTo < tokens.length; offsetTo++) {
        int lengthSearching = offsetTo - offsetFrom + 1;

        if (lengthSearching > dictionary.getMaxTokenCount()) {
          break;
        } else {
          tokensSearching = new String[lengthSearching];
          System.arraycopy(tokens, offsetFrom, tokensSearching, 0,
              lengthSearching);

          String entryForSearch = StringUtils
              .getStringFromTokens(tokensSearching);
          String entryValue = dictionary.lookup(entryForSearch);
          if (entryValue != null) {
            nameFound = new Span(offsetFrom, offsetTo + 1, entryValue);
          }
        }
      }
      if (nameFound != null) {
        namesFound.add(nameFound);
        // skip over the found tokens for the next search
        offsetFrom += (nameFound.length() - 1);
      }
    }
    return namesFound.toArray(new Span[namesFound.size()]);
  }

}
