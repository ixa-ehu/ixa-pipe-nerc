/*
 *  Copyright 2014 Rodrigo Agerri

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
 */
package es.ehu.si.ixa.pipe.nerc.features;

import es.ehu.si.ixa.pipe.nerc.DictionaryNameFinder;
import es.ehu.si.ixa.pipe.nerc.dict.Dictionary;

import java.util.List;


public class DictionaryFeatureGenerator extends FeatureGeneratorAdapter {

  private InSpanGenerator isg;
  
  public DictionaryFeatureGenerator(Dictionary dict) {
    this("",dict);
  }
  public DictionaryFeatureGenerator(String prefix, Dictionary dict) {
    setDictionary(prefix,dict);
  }
  
  public void setDictionary(Dictionary dict) {
    setDictionary("",dict);
  }
  
  public void setDictionary(String name, Dictionary dict) {
    isg = new InSpanGenerator(name, new DictionaryNameFinder(dict));
  }
  
  public void createFeatures(List<String> features, String[] tokens, int index, String[] previousOutcomes) {
    isg.createFeatures(features, tokens, index, previousOutcomes);
  }
  
}
