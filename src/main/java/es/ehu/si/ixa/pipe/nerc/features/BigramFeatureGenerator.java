package es.ehu.si.ixa.pipe.nerc.features;

import java.util.List;

public class BigramFeatureGenerator extends FeatureGeneratorAdapter {

  public void createFeatures(List<String> features, String[] tokens, int index, String[] previousOutcomes) {
    String wc = FeatureGeneratorUtil.tokenFeature(tokens[index]);
    //bi-gram features 
    if (index > 0) {
      features.add("pw,w="+tokens[index-1]+","+tokens[index]);
      String pwc = FeatureGeneratorUtil.tokenFeature(tokens[index-1]);
      features.add("pwc,wc="+pwc+","+wc);
    }
    if (index+1 < tokens.length) {
      features.add("w,nw="+tokens[index]+","+tokens[index+1]);
      String nwc = FeatureGeneratorUtil.tokenFeature(tokens[index+1]); 
      features.add("wc,nc="+wc+","+nwc);
    }
  } 
}
