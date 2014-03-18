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

package ixa.pipe.nerc;

import java.util.ArrayList;
import java.util.List;

import opennlp.tools.util.Span;

public class StringUtils {
  
  /**
   * Finds a pattern (typically a named entity string) in a tokenized sentence. 
   * It outputs the {@link Span} indexes of the named entity found, if any
   * 
   * @param pattern a string to find 
   * @param tokens an array of tokens
   * @return token spans of the pattern (e.g. a named entity)
   */
  public static List<Integer> exactTokenFinder(String pattern, String[] tokens) {
    String[] patternTokens = pattern.split(" ");
    int i, j; 
    int patternLength = patternTokens.length;
    int sentenceLength = tokens.length;
    List<Integer> neTokens = new ArrayList<Integer>();
    for (j = 0; j <= sentenceLength - patternLength; ++j) {
      for (i = 0; i < patternLength && patternTokens[i].equalsIgnoreCase(tokens[i + j]); ++i);
      if (i >= patternLength) {
        neTokens.add(j);
        neTokens.add(i+j);
      }
    }
    return neTokens;
  }
  
  /**
   * Finds a pattern (typically a named entity string) in a sentence string. 
   * It outputs the offsets for the start and end characters named entity found, if any
   * 
   * @param pattern
   * @param sentence
   * @return
   */
  public static List<Integer> exactStringFinder(String pattern, String sentence) {
    char[] patternArray = pattern.toCharArray(), sentenceArray = sentence.toCharArray();
    int i, j; 
    int patternLength = patternArray.length;
    int sentenceLength = sentenceArray.length;
    List<Integer> neChars = new ArrayList<Integer>();
    for (j = 0; j <= sentenceLength - patternLength; ++j) {
      for (i = 0; i < patternLength && patternArray[i] == sentenceArray[i + j]; ++i);
      if (i >= patternLength) {
        neChars.add(j);
        neChars.add(i+j);
      }
    }
    return neChars;
  }
  
  /**
   * 
   * It takes a NE span indexes and the tokens in a sentence and produces the
   * string to which the NE span corresponds to. This function is used to get
   * the Named Entity or Name textual representation from a {@link Span}
   * 
   * @param reducedSpan a {@link Span}
   * @param tokens an array of tokens
   * @return named entity string
   */
  public static String getStringFromSpan(Span reducedSpan, String[] tokens) {
    StringBuilder sb = new StringBuilder();
    for (int si = reducedSpan.getStart(); si < reducedSpan.getEnd(); si++) {
      sb.append(tokens[si]).append(" ");
    }
    return sb.toString().trim();
  }

  /**
   * Gets the String joined by a space of an array of tokens
   * @param tokens an array of tokens representing a tokenized sentence
   * @return sentence 
   */
  public static String getSentenceFromTokens(String[] tokens) {
    StringBuilder sb = new StringBuilder();
    for (String tok : tokens) {
      sb.append(tok).append(" ");
    }
    return sb.toString().trim();
  }
    

}
