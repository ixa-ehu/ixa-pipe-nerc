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
import es.ehu.si.ixa.ixa.pipe.nerc.dict.LemmaResource;
import es.ehu.si.ixa.ixa.pipe.nerc.dict.MFSResource;
import es.ehu.si.ixa.ixa.pipe.nerc.dict.POSModelResource;
import es.ehu.si.ixa.ixa.pipe.nerc.train.Flags;

/**
 * Generate pos tag, pos tag class, lemma and most frequent sense as feature of
 * the current token. This feature generator can also be placed in a sliding
 * window.
 * 
 * @author ragerri
 * @version 2015-03-13
 */
public class MFSFeatureGenerator extends CustomFeatureGenerator implements
    ArtifactToSerializerMapper {

  private POSModelResource posModelResource;
  private LemmaResource lemmaDictResource;
  private MFSResource mfsDictResource;
  private String[] currentSentence;
  private String[] currentTags;
  private List<String> currentLemmas;
  private List<String> currentMFSList;
  private boolean isMFS;
  private boolean isMonosemic;

  public MFSFeatureGenerator() {
  }

  public void createFeatures(List<String> features, String[] tokens, int index,
      String[] previousOutcomes) {

    // cache results for each sentence
    if (currentSentence != tokens) {
      currentSentence = tokens;
      currentTags = posModelResource.posTag(tokens);
      currentLemmas = lemmaDictResource.lookUpLemmaArray(tokens, currentTags);
      currentMFSList = mfsDictResource.getLabeledMFS(currentLemmas, currentTags);
    }
    //word shapes with no window
    String firstCharacter = tokens[index].substring(0, 1);
    if (firstCharacter.toLowerCase().equals(firstCharacter)) {
      features.add("shlow");
    } else if (index == 0) {
      features.add("shcap_brk");
    } else {
      features.add("shcap_nobrk");
    }
    //postag features with no window
    String posTag = currentTags[index];
    if (posTag.startsWith("NNP")) {
      features.add("posprop");
    }
    if (posTag.equalsIgnoreCase("NNS") || posTag.equalsIgnoreCase("NN")) {
      features.add("poscomm");
    }
    //mfs
    if (isMFS) {
      String mfs = currentMFSList.get(index);
      //System.err.println("-> MFS " + mfs + " " + currentLemmas.get(index) + " " + tokens[index] +  " " + currentTags[index]);
      features.add("mfs=" + mfs);
      features.add("mfs,lemma=" + mfs + "," + currentLemmas.get(index));
      //previous label
      if (index > 0) {
        String previousLabel = previousOutcomes[index - 1];
        previousLabel = bilouToBio(previousLabel);
        features.add("prevLabel=" + previousLabel);
        //System.err.println("-> PrevLabel " + previousLabel);
      }
    }
    if (isMonosemic) {
    }
  }
  
  public static String bilouToBio(String label) {
    if (label.endsWith("-unit")) {
      label = "B-" + label.split("-unit")[0];
    } else if (label.endsWith("-start")) {
      label = "B-" + label.split("-start")[0];
    } else if (label.endsWith("-cont")) {
      label = "I-" + label.split("-cont")[0];
    } else if (label.endsWith("-last")) {
      label = "I-" + label.split("-last")[0];
    } else if (label.equalsIgnoreCase("other")) {
      label = "O";
    }
    return label;
  }

  @Override
  public void updateAdaptiveData(String[] tokens, String[] outcomes) {

  }

  @Override
  public void clearAdaptiveData() {

  }

  /**
   * Process the options of which kind of features are to be generated.
   * 
   * @param properties
   *          the properties map
   */
  private void processRangeOptions(Map<String, String> properties) {
    String featuresRange = properties.get("range");
    String[] rangeArray = Flags.processMFSFeaturesRange(featuresRange);
    // options
    if (rangeArray[0].equalsIgnoreCase("mfs")) {
      isMFS = true;
    }
    if (rangeArray[1].equalsIgnoreCase("monosemic")) {
      isMonosemic = true;
    }
  }
  
  @Override
  public void init(Map<String, String> properties,
      FeatureGeneratorResourceProvider resourceProvider)
      throws InvalidFormatException {
    Object posResource = resourceProvider.getResource(properties.get("model"));
    if (!(posResource instanceof POSModelResource)) {
      throw new InvalidFormatException("Not a POSModelResource for key: "
          + properties.get("model"));
    }
    this.posModelResource = (POSModelResource) posResource;
    Object lemmaResource = resourceProvider.getResource(properties.get("dict"));
    if (!(lemmaResource instanceof LemmaResource)) {
      throw new InvalidFormatException("Not a LemmaResource for key: "
          + properties.get("dict"));
    }
    this.lemmaDictResource = (LemmaResource) lemmaResource;
    Object mfsResource = resourceProvider.getResource(properties.get("mfs"));
    if (!(mfsResource instanceof MFSResource)) {
      throw new InvalidFormatException("Not a MFSResource for key: "
          + properties.get("mfs"));
    }
    this.mfsDictResource = (MFSResource) mfsResource;
    processRangeOptions(properties);
  }

  @Override
  public Map<String, ArtifactSerializer<?>> getArtifactSerializerMapping() {
    Map<String, ArtifactSerializer<?>> mapping = new HashMap<>();
    mapping.put("posmodelserializer",
        new POSModelResource.POSModelResourceSerializer());
    mapping.put("lemmadictserializer",
        new LemmaResource.LemmaResourceSerializer());
    mapping.put("mfsdictserializer", new MFSResource.MFSResourceSerializer());
    return Collections.unmodifiableMap(mapping);
  }
}
