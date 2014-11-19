package es.ehu.si.ixa.ixa.pipe.nerc.features;

import java.util.List;

import opennlp.tools.util.featuregen.FeatureGeneratorAdapter;

/**
 * Adds bigram features based on tokens and token class using {@code TokenClassFeatureGenerator}.
 * @author ragerri
 *
 */
public class BigramClassFeatureGenerator extends FeatureGeneratorAdapter {

  public void createFeatures(List<String> features, String[] tokens, int index, String[] previousOutcomes) {
    String wc = TokenClassFeatureGenerator.tokenShapeFeature(tokens[index]);
    //bi-gram features 
    if (index > 0) {
      features.add("pw,w=" + tokens[index-1] + "," + tokens[index]);
      String pwc = TokenClassFeatureGenerator.tokenShapeFeature(tokens[index - 1]);
      features.add("pwc,wc=" + pwc + "," + wc);
    }
    if (index + 1 < tokens.length) {
      features.add("w,nw=" + tokens[index] + "," + tokens[index + 1]);
      String nwc = TokenClassFeatureGenerator.tokenShapeFeature(tokens[index + 1]); 
      features.add("wc,nc=" + wc + "," + nwc);
    }
  } 
}
