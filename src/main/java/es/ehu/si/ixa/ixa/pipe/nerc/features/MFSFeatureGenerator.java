/*
 * Copyright 2015 Rodrigo Agerri

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

package es.ehu.si.ixa.ixa.pipe.nerc.features;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import opennlp.tools.util.InvalidFormatException;
import opennlp.tools.util.featuregen.ArtifactToSerializerMapper;
import opennlp.tools.util.featuregen.CustomFeatureGenerator;
import opennlp.tools.util.featuregen.FeatureGeneratorResourceProvider;
import opennlp.tools.util.model.ArtifactSerializer;

import com.google.common.collect.TreeMultimap;

import es.ehu.si.ixa.ixa.pipe.nerc.dict.LemmaResource;
import es.ehu.si.ixa.ixa.pipe.nerc.dict.MFSResource;
import es.ehu.si.ixa.ixa.pipe.nerc.dict.POSModelResource;
import es.ehu.si.ixa.ixa.pipe.nerc.train.Flags;

/**
 * Generate pos tag, pos tag class, lemma and most frequent sense
 * as feature of the current token.
 * This feature generator can also be placed in a sliding window.
 * @author ragerri
 * @version 2015-03-13
 */
public class MFSFeatureGenerator extends CustomFeatureGenerator implements ArtifactToSerializerMapper {
  
  private POSModelResource posModelResource;
  private LemmaResource lemmaDictResource;
  private MFSResource mfsDictResource;
  private String[] currentSentence;
  private String[] currentTags;
  private boolean isPos;
  private boolean isPosClass;
  private boolean isLemma;
  private boolean isMFS;
  private boolean isMonosemic;
  
  public MFSFeatureGenerator() {
  }
  
  public void createFeatures(List<String> features, String[] tokens, int index,
      String[] previousOutcomes) {
    
    //cache pos tagger results for each sentence
    if (currentSentence != tokens) {
      currentSentence = tokens;
      currentTags = posModelResource.posTag(tokens);
    }
    String posTag = currentTags[index];
    //options
    if (isPos) {
      features.add("posTag=" + posTag);
    }
    if (isPosClass) {
      String posTagClass = posTag.substring(0, 1);
      features.add("posTagClass=" + posTagClass);
    }
    if (isLemma) {
      String lemma = lemmaDictResource.lookUpLemma(tokens[index], posTag);
      features.add("lemma=" + lemma);
    }
    String lemmaPOSClass = null;
    if (isMFS) {
      //TODO use DictionaryFeatureFinder to find multiword spans and build the
      //feature as for DictionaryFeatureGenerator
      if (posTag.startsWith("J") || posTag.startsWith("N") || posTag.startsWith("R") || posTag.startsWith("V")) {
        String lemma = lemmaDictResource.lookUpLemma(tokens[index], posTag);
        lemmaPOSClass = lemma + "#" + posTag.substring(0, 1).toLowerCase();
        TreeMultimap<Integer, String> mfsMap = mfsDictResource.getOrderedMap(lemmaPOSClass);
        if (!mfsMap.isEmpty()) {
          String mfs = mfsDictResource.getMFS(mfsMap);
          features.add("mfs=" + mfs);
          features.add("mfs,w=" + mfs + "," + tokens[index]);
        } else {
          features.add("mfs=" + "unknownMFS");
          features.add("mfs,w=" + "unknownMFS" + "," + tokens[index]);
        }
      }
    }
    if (isMonosemic) {
      if (isMFS && lemmaPOSClass != null) {
        TreeMultimap<Integer, String> mfsMap = mfsDictResource.getOrderedMap(lemmaPOSClass);
        if (mfsMap.size() == 1) {
          String monosemic = mfsMap.get(mfsMap.keySet().first()).first();
          features.add("monosemic=" + monosemic);
          features.add("monosemic,w=" + monosemic + "," + tokens[index]);
        } else {
          features.add("monosemic=" + "unknownMonosemic");
          features.add("monosemic,w=" + "uknownMonosemic" + "," + tokens[index]);
        }
      } else {
        //TODO use DictionaryFeatureFinder to find multiword spans and build the
        //feature as for DictionaryFeatureGenerator
        if (posTag.startsWith("J") || posTag.startsWith("N") || posTag.startsWith("R") || posTag.startsWith("V")) {
          String lemma = lemmaDictResource.lookUpLemma(tokens[index], posTag);
          lemmaPOSClass = lemma + "#" + posTag.substring(0, 1).toLowerCase();
          TreeMultimap<Integer, String> mfsMap = mfsDictResource.getOrderedMap(lemmaPOSClass);
          if (mfsMap.size() == 1) {
            String monosemic = mfsMap.get(mfsMap.keySet().first()).first();
            features.add("monosemic=" + monosemic);
            features.add("monosemic,w=" + monosemic + "," + tokens[index]);
          } else {
            features.add("monosemic=" + "unknownMonosemic");
	    features.add("monosemic,w=" + "uknownMonosemic" + "," + tokens[index]);
          }
        }
      }
    }
  }
  
  @Override
  public void updateAdaptiveData(String[] tokens, String[] outcomes) {
    
  }

  @Override
  public void clearAdaptiveData() {
    
  }

  @Override
  public void init(Map<String, String> properties,
      FeatureGeneratorResourceProvider resourceProvider)
      throws InvalidFormatException {
    Object posResource = resourceProvider.getResource(properties.get("model"));
    if (!(posResource instanceof POSModelResource)) {
      throw new InvalidFormatException("Not a POSModelResource for key: " + properties.get("model"));
    }
    this.posModelResource = (POSModelResource) posResource;
    Object lemmaResource = resourceProvider.getResource(properties.get("dict"));
    if (!(lemmaResource instanceof LemmaResource)) {
      throw new InvalidFormatException("Not a LemmaResource for key: " + properties.get("dict"));
    }
    this.lemmaDictResource = (LemmaResource) lemmaResource;
    Object mfsResource = resourceProvider.getResource(properties.get("mfs"));
    if (!(mfsResource instanceof MFSResource)) {
      throw new InvalidFormatException("Not a MFSResource for key: " + properties.get("mfs"));
    }
    this.mfsDictResource = (MFSResource) mfsResource;
    processRangeOptions(properties);
  }
  
  /**
   * Process the options of which kind of features are to be generated.
   * @param properties the properties map
   */
  private void processRangeOptions(Map<String, String> properties) {
    String featuresRange = properties.get("range");
    String[] rangeArray = Flags.processMFSFeaturesRange(featuresRange);
    //options
    if (rangeArray[0].equalsIgnoreCase("pos")) {
      isPos = true;
    }
    if (rangeArray[1].equalsIgnoreCase("posclass")) {
      isPosClass = true;
    }
    if (rangeArray[2].equalsIgnoreCase("lemma")) {
      isLemma = true;
    }
    if (rangeArray[3].equalsIgnoreCase("mfs")) {
      isMFS = true;
    }
    if (rangeArray[4].equalsIgnoreCase("monosemic")) {
      isMonosemic = true;
    }
  }
  
  @Override
  public Map<String, ArtifactSerializer<?>> getArtifactSerializerMapping() {
    Map<String, ArtifactSerializer<?>> mapping = new HashMap<>();
    mapping.put("posmodelserializer", new POSModelResource.POSModelResourceSerializer());
    mapping.put("lemmadictserializer", new LemmaResource.LemmaResourceSerializer());
    mapping.put("mfsdictserializer", new MFSResource.MFSResourceSerializer());
    return Collections.unmodifiableMap(mapping);
  }
}



