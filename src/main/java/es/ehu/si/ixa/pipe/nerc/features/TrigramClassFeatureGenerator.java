package es.ehu.si.ixa.pipe.nerc.features;

import java.util.List;

/**
 * Adds trigram features based on tokens and token class using
 * {@code TokenClassFeatureGenerator}.
 * 
 * @author ragerri
 * 
 */
public class TrigramClassFeatureGenerator extends FeatureGeneratorAdapter {

  public void createFeatures(List<String> features, String[] tokens, int index,
      String[] previousOutcomes) {
    String wc = TokenClassFeatureGenerator.tokenShapeFeature(tokens[index]);
    // trigram features
    if (index > 1) {
      features.add("ppw,pw,w=" + tokens[index - 2] + "," + tokens[index - 1] + "," + tokens[index]);
      String pwc = TokenClassFeatureGenerator.tokenShapeFeature(tokens[index - 1]);
      String ppwc = TokenClassFeatureGenerator.tokenShapeFeature(tokens[index - 2]);
      features.add("ppwc,pwc,wc=" + ppwc + "," + pwc + "," + wc);
    }
    if (index + 2 < tokens.length) {
      features.add("w,nw,nnw=" + tokens[index] + "," + tokens[index + 1] + "," + tokens[index + 2]);
      String nwc = TokenClassFeatureGenerator.tokenShapeFeature(tokens[index + 1]);
      String nnwc = TokenClassFeatureGenerator.tokenShapeFeature(tokens[index + 2]);
      features.add("wc,nwc,nnwc=" + wc + "," + nwc + "," + nnwc);
    }
  }
}
