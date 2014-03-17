package ixa.pipe.nerc.train;

import ixa.pipe.nerc.Gazetteer;
import ixa.pipe.nerc.GazetteerNameFinder;

import java.util.List;

import opennlp.tools.util.featuregen.FeatureGeneratorAdapter;


public class DictionaryFeatures extends FeatureGeneratorAdapter {

  private InSpanGenerator isg;
  
  public DictionaryFeatures(Gazetteer dict) {
    this("",dict);
  }
  public DictionaryFeatures(String prefix, Gazetteer dict) {
    setDictionary(prefix,dict);
  }
  
  public void setDictionary(Gazetteer dict) {
    setDictionary("",dict);
  }
  
  public void setDictionary(String name, Gazetteer dict) {
    isg = new InSpanGenerator(name, new GazetteerNameFinder(dict));
  }
  
  public void createFeatures(List<String> features, String[] tokens, int index, String[] previousOutcomes) {
    isg.createFeatures(features, tokens, index, previousOutcomes);
  }
  
}
