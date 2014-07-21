package es.ehu.si.ixa.pipe.nerc.features;

import java.util.List;


public class SuffixFeatureGenerator extends FeatureGeneratorAdapter {

  private static final int SUFFIX_LENGTH = 4;
  
  public static String[] getSuffixes(String lex) {
    String[] suffs = new String[SUFFIX_LENGTH];
    for (int li = 0, ll = SUFFIX_LENGTH; li < ll; li++) {
      suffs[li] = lex.substring(Math.max(lex.length() - li - 1, 0));
    }
    return suffs;
  }
  
  public void createFeatures(List<String> features, String[] tokens, int index,
      String[] previousOutcomes) {
    String[] suffs = SuffixFeatureGenerator.getSuffixes(tokens[index]);
    for (String suff : suffs) {
      features.add("suf=" + suff);
    }
  }
}