package es.ehu.si.ixa.pipe.nerc.features;

import java.util.List;

import es.ehu.si.ixa.pipe.nerc.dict.BrownCluster;

/**
 * Generates a feature which contains the token itself.
 */
public class TokenFeatureGenerator extends FeatureGeneratorAdapter {

  private boolean lowercase;
  private boolean brownFeatures;
  private BrownCluster brownCluster;

  public TokenFeatureGenerator(boolean lowercase, boolean brownFeatures,
      BrownCluster brownCluster) {
    this.lowercase = lowercase;
    this.brownFeatures = brownFeatures;
    this.brownCluster = brownCluster;
  }

  public TokenFeatureGenerator(boolean lowercase) {
    this(lowercase, false, null);
  }

  public TokenFeatureGenerator() {
    this(true);
  }

  public void createFeatures(List<String> features, String[] tokens, int index,
      String[] preds) {

    if (brownFeatures) {
      String[] wordClasses = BrownTokenFeatures.getWordClasses(tokens[index],
          brownCluster);
      if (lowercase) {
        for (String wordClass : wordClasses) {
          features.add("w,brown=" + tokens[index].toLowerCase() + ","
              + wordClass);
        }
      } else {
        for (String wordClass : wordClasses) {
          features.add("w,brown=" + tokens[index] + "," + wordClass);
        }
      }
    }
    if (lowercase) {
      features.add("w=" + tokens[index].toLowerCase());
    } else {
      features.add("w=" + tokens[index]);
    }
  }
}
