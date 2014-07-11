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

import opennlp.tools.util.featuregen.AdaptiveFeatureGenerator;
import opennlp.tools.util.featuregen.CachedFeatureGenerator;
import opennlp.tools.util.featuregen.SuffixFeatureGenerator;
import es.ehu.si.ixa.pipe.nerc.features.Prefix34FeatureGenerator;

/**
 * Training NER based on Apache OpenNLP Machine Learning API.
 * This class implements baseline shape features on top of the {@link DefaultNameFinderTrainer}
 * features.
 *
 * @author ragerri 2014/06/25
 * @version 2014-07-11
 */
public class BaselineNameFinderTrainer extends AbstractNameFinderTrainer {

  /**
   * Construct a Baseline trainer.
   * @param trainData the training data
   * @param testData the test data
   * @param lang the language
   * @param beamsize the beamsize for decoding
   * @param corpusFormat the corpus format
   * @param netypes the NE classes
   * @throws IOException the data exception
   */
  public BaselineNameFinderTrainer(final String trainData, final String testData,
      final String lang, final int beamsize, final String corpusFormat, final String netypes)
      throws IOException {
    super(trainData, testData, lang, beamsize, corpusFormat, netypes);
    setFeatures(createFeatureGenerator());
  }

  /**
   * Construct a baseline trainer with only beamsize specified.
   * @param beamsize the beamsize
   */
  public BaselineNameFinderTrainer(final int beamsize) {
    super(beamsize);
    setFeatures(createFeatureGenerator());
  }

  /* (non-Javadoc)
   * @see es.ehu.si.ixa.pipe.nerc.train.NameFinderTrainer#createFeatureGenerator()
   */
  public final AdaptiveFeatureGenerator createFeatureGenerator() {
    List<AdaptiveFeatureGenerator> featureList = DefaultNameFinderTrainer
        .createFeatureList();
    addToFeatureList(featureList);
    AdaptiveFeatureGenerator[] featuresArray = featureList
        .toArray(new AdaptiveFeatureGenerator[featureList.size()]);
    return new CachedFeatureGenerator(featuresArray);
  }

  /**
   * Adds the Baseline features to the feature list.
   * @param featureList
   *          the feature list containing the baseline features
   */
  public static void addToFeatureList(final List<AdaptiveFeatureGenerator> featureList) {
    featureList.add(new Prefix34FeatureGenerator());
    featureList.add(new SuffixFeatureGenerator());

  }

}
