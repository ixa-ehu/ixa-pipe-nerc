package es.ehu.si.ixa.pipe.nerc.features;

import es.ehu.si.ixa.pipe.nerc.Dictionary;
import es.ehu.si.ixa.pipe.nerc.DictionaryNameFinder;

import java.util.List;

import opennlp.tools.util.featuregen.FeatureGeneratorAdapter;


public class DictionaryFeatures extends FeatureGeneratorAdapter {

  private InSpanGenerator isg;
  
  public DictionaryFeatures(Dictionary dict) {
    this("",dict);
  }
  public DictionaryFeatures(String prefix, Dictionary dict) {
    setDictionary(prefix,dict);
  }
  
  public void setDictionary(Dictionary dict) {
    setDictionary("",dict);
  }
  //TODO this is too slow
  public void setDictionary(String name, Dictionary dict) {
    isg = new InSpanGenerator(name, new DictionaryNameFinder(dict));
  }
  
  public void createFeatures(List<String> features, String[] tokens, int index, String[] previousOutcomes) {
    isg.createFeatures(features, tokens, index, previousOutcomes);
  }
  
}
