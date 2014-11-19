package es.ehu.si.ixa.ixa.pipe.nerc.features;

import java.util.List;

import opennlp.tools.util.featuregen.FeatureGeneratorAdapter;

/**
 * Adds fourgram features based on tokens and token class using
 * {@code TokenClassFeatureGenerator}.
 * 
 * @author ragerri
 * 
 */
public class FourgramClassFeatureGenerator extends FeatureGeneratorAdapter {

  public void createFeatures(List<String> features, String[] tokens, int index,
      String[] previousOutcomes) {
    String wc = TokenClassFeatureGenerator.tokenShapeFeature(tokens[index]);
    // fourgram features
    if (index > 2) {
      features.add("pppw,ppw,pw,w=" + tokens[index - 3] + "," + tokens[index - 2] + "," + tokens[index - 1] + "," + tokens[index]);
      String pwc = TokenClassFeatureGenerator.tokenShapeFeature(tokens[index - 1]);
      String ppwc = TokenClassFeatureGenerator.tokenShapeFeature(tokens[index - 2]);
      String pppwc = TokenClassFeatureGenerator.tokenShapeFeature(tokens[index - 3]);
      features.add("pppwc,ppwc,pwc,wc=" + pppwc + "," + ppwc + "," + pwc + "," + wc);
    }
    if (index + 3 < tokens.length) {
      features.add("w,nw,nnw,nnnw=" + tokens[index] + "," + tokens[index + 1] + "," + tokens[index + 2] + "," + tokens[index + 3]);
      String nwc = TokenClassFeatureGenerator.tokenShapeFeature(tokens[index + 1]);
      String nnwc = TokenClassFeatureGenerator.tokenShapeFeature(tokens[index + 2]);
      String nnnwc = TokenClassFeatureGenerator.tokenShapeFeature(tokens[index + 3]);
      features.add("wc,nwc,nnwc,nnnwc=" + wc + "," + nwc + "," + nnwc + "," + nnnwc);
    }
  }
}
