package es.ehu.si.ixa.ixa.pipe.nerc.features;

import java.util.List;

import opennlp.tools.util.featuregen.FeatureGeneratorAdapter;

/**
 * Adds fivegram features based on tokens and token class using
 * {@code TokenClassFeatureGenerator}.
 * 
 * @author ragerri
 * 
 */
public class FivegramClassFeatureGenerator extends FeatureGeneratorAdapter {

  public void createFeatures(List<String> features, String[] tokens, int index,
      String[] previousOutcomes) {
    String wc = TokenClassFeatureGenerator.tokenShapeFeature(tokens[index]);
    // fivegram features
    if (index > 3) {
      features.add("ppppw,pppw,ppw,pw,w=" + tokens[index - 4] + "," + tokens[index - 3] + "," + tokens[index - 2] + "," + tokens[index - 1] + "," + tokens[index]);
      String pwc = TokenClassFeatureGenerator.tokenShapeFeature(tokens[index - 1]);
      String ppwc = TokenClassFeatureGenerator.tokenShapeFeature(tokens[index - 2]);
      String pppwc = TokenClassFeatureGenerator.tokenShapeFeature(tokens[index - 3]);
      String ppppwc = TokenClassFeatureGenerator.tokenShapeFeature(tokens[index - 4]);
      features.add("pppwc,ppwc,pwc,wc=" + ppppwc + "," + pppwc + "," + ppwc + "," + pwc + "," + wc);
    }
    if (index + 4 < tokens.length) {
      features.add("w,nw,nnw,nnnw,nnnnw=" + tokens[index] + "," + tokens[index + 1] + "," + tokens[index + 2] + "," + tokens[index + 3] + "," + tokens[index + 4]);
      String nwc = TokenClassFeatureGenerator.tokenShapeFeature(tokens[index + 1]);
      String nnwc = TokenClassFeatureGenerator.tokenShapeFeature(tokens[index + 2]);
      String nnnwc = TokenClassFeatureGenerator.tokenShapeFeature(tokens[index + 3]);
      String nnnnwc = TokenClassFeatureGenerator.tokenShapeFeature(tokens[index + 4]);
      features.add("wc,nwc,nnwc,nnnwc=" + wc + "," + nwc + "," + nnwc + "," + nnnwc + "," + nnnnwc);
    }
  }
}
