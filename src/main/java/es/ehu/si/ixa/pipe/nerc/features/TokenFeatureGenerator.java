package es.ehu.si.ixa.pipe.nerc.features;

import java.util.List;

/**
 * Generates a feature which contains the token itself.
 */
public class TokenFeatureGenerator extends FeatureGeneratorAdapter {

  private static final String WORD_PREFIX = "w";
  private boolean lowercase;

  public TokenFeatureGenerator(boolean lowercase) {
    this.lowercase = lowercase;
  }

  public TokenFeatureGenerator() {
    this(true);
  }

  public void createFeatures(List<String> features, String[] tokens, int index, String[] preds) {
    if (lowercase) {
      features.add(WORD_PREFIX + "=" + tokens[index].toLowerCase());
    }
    else {
      features.add(WORD_PREFIX + "=" + tokens[index]);
    }
  }
}
