package ixa.pipe.nerc;

import java.util.ArrayList;
import java.util.List;

public class StringUtils {

  
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
  
  public static List<Integer> bndmStringFinder(String pattern, String source) {
    char[] x = pattern.toCharArray(), y = source.toCharArray();
    int i, j, s, d, last, m = x.length, n = y.length;
    List<Integer> result = new ArrayList<Integer>();
    
    int[] b = new int[65536];

    /* Pre processing */
    for (i = 0; i < b.length; i++)
        b[i] = 0;
    s = 1;
    for (i = m - 1; i >= 0; i--) {
        b[x[i]] |= s;
        s <<= 1;
    }

    /* Searching phase */
    j = 0;
    while (j <= n - m) {
        i = m - 1;
        last = m;
        d = ~0;
        while (i >= 0 && d != 0) {
            d &= b[y[j + i]];
            i--;
            if (d != 0) {
                if (i >= 0)
                    last = i + 1;
                else
                    result.add(j);
                    result.add(i + j);
            }
            d <<= 1;
        }
        j += last;
    }
    return result;
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
