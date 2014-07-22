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

package es.ehu.si.ixa.pipe.nerc.train;

import java.io.IOException;
import java.util.List;

import es.ehu.si.ixa.pipe.nerc.dict.Dictionaries;
import es.ehu.si.ixa.pipe.nerc.dict.Dictionary;
import es.ehu.si.ixa.pipe.nerc.features.AdaptiveFeatureGenerator;
import es.ehu.si.ixa.pipe.nerc.features.CachedFeatureGenerator;
import es.ehu.si.ixa.pipe.nerc.features.DictionaryFeatureGenerator;
import es.ehu.si.ixa.pipe.nerc.train.AbstractNameFinderTrainer;

/**
 * Training NER based on Apache OpenNLP Machine Learning API. This class
 * implements Gazetteer features.
 *
 * @author ragerri 2014/06/25
 *
 */

public class DictNameFinderTrainer extends AbstractNameFinderTrainer {

  /**
   * The {@link Dictionaries} contained in the given directory.
   */
  private static Dictionaries dictionaries;
  /**
   * A {@link Dictionary} object.
   */
  private static Dictionary dictionary;
  /**
   * The prefix to be used in the {@link DictionaryFeatureGenerator}.
   */
  private static String prefix;

  /**
   * Construct a dictionary name finder trainer.
   * @param aDictionaries the dictionaries
   * @param trainData the training data
   * @param testData the test data
   * @param lang the language
   * @param beamsize the beamsize for decoding
   * @param corpusFormat the corpus format
   * @param netypes the NE classes for filtering
   * @throws IOException throw if input error
   */
  public DictNameFinderTrainer(final Dictionaries aDictionaries, final String trainData,
      final String testData, final String lang, final int beamsize, final String corpusFormat,
      final String netypes) throws IOException {
    super(trainData, testData, lang, beamsize, corpusFormat, netypes);

    if (dictionaries == null) {
      dictionaries = aDictionaries;
    }
    setFeatures(createFeatureGenerator());

  }

  /**
   * Construct a DictionaryNameFinderTrainer for evaluation only.
   *
   * @param aDictionaries the dictionaries
   * @param beamsize the beamsize for decoding; it defaults to 3
   */
  public DictNameFinderTrainer(final Dictionaries aDictionaries, final int beamsize) {
    super(beamsize);

    if (dictionaries == null) {
      dictionaries = aDictionaries;
    }
    setFeatures(createFeatureGenerator());

  }

  /*
   * Creates a feature list to which the dictionary features are added.
   *  (non-Javadoc)
   * @see es.ehu.si.ixa.pipe.nerc.train.NameFinderTrainer#createFeatureGenerator()
   */
  public final AdaptiveFeatureGenerator createFeatureGenerator() {
    List<AdaptiveFeatureGenerator> featureList = BaselineNameFinderTrainer.createWindowFeatureList();
    BaselineNameFinderTrainer.addTokenFeatures(featureList);
    BaselineNameFinderTrainer.addCharNgramFeatures(featureList);
    addDictionariesToFeatureList(featureList);
    AdaptiveFeatureGenerator[] featuresArray = featureList
        .toArray(new AdaptiveFeatureGenerator[featureList.size()]);
    return new CachedFeatureGenerator(featuresArray);
  }

  /**
   * Adds the dictionary features to the feature list.
   *
   * @param featureList the feature list containing the dictionary features
   */
  private static void addDictionariesToFeatureList(
      final List<AdaptiveFeatureGenerator> featureList) {
    for (int i = 0; i < dictionaries.getIgnoreCaseDictionaries().size(); i++) {
      prefix = dictionaries.getDictNames().get(i);
      dictionary = dictionaries.getIgnoreCaseDictionaries().get(i);
      featureList.add(new DictionaryFeatureGenerator(prefix, dictionary));
    }
  }
  
}
