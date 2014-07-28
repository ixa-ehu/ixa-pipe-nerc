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
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import opennlp.tools.util.TrainingParameters;
import es.ehu.si.ixa.pipe.nerc.dict.Dictionaries;
import es.ehu.si.ixa.pipe.nerc.dict.Dictionary;
import es.ehu.si.ixa.pipe.nerc.features.AdaptiveFeatureGenerator;
import es.ehu.si.ixa.pipe.nerc.features.BigramClassFeatureGenerator;
import es.ehu.si.ixa.pipe.nerc.features.CachedFeatureGenerator;
import es.ehu.si.ixa.pipe.nerc.features.CharacterNgramFeatureGenerator;
import es.ehu.si.ixa.pipe.nerc.features.DictionaryFeatureGenerator;
import es.ehu.si.ixa.pipe.nerc.features.FivegramClassFeatureGenerator;
import es.ehu.si.ixa.pipe.nerc.features.FourgramClassFeatureGenerator;
import es.ehu.si.ixa.pipe.nerc.features.OutcomePriorFeatureGenerator;
import es.ehu.si.ixa.pipe.nerc.features.Prefix34FeatureGenerator;
import es.ehu.si.ixa.pipe.nerc.features.PreviousMapFeatureGenerator;
import es.ehu.si.ixa.pipe.nerc.features.SentenceFeatureGenerator;
import es.ehu.si.ixa.pipe.nerc.features.SuffixFeatureGenerator;
import es.ehu.si.ixa.pipe.nerc.features.TokenClassFeatureGenerator;
import es.ehu.si.ixa.pipe.nerc.features.TokenFeatureGenerator;
import es.ehu.si.ixa.pipe.nerc.features.TrigramClassFeatureGenerator;
import es.ehu.si.ixa.pipe.nerc.features.WindowFeatureGenerator;

/**
 * Training NER based on Apache OpenNLP Machine Learning API for English. This
 * class creates a feature set based on the features activated in the
 * trainParams.txt properties file:
 * <ol>
 * <li>Window: specify left and right window lengths.
 * <li>TokenFeatures: tokens as features in a window length.
 * <li>TokenClassFeatures: token shape features in a window length.
 * <li>OutcomePriorFeatures: take into account previous outcomes.
 * <li>PreviousMapFeatures: add features based on tokens and previous decisions.
 * <li>SentenceFeatures: add beginning and end of sentence words.
 * <li>PrefixFeatures: first 4 characters in current token.
 * <li>SuffixFeatures: last 4 characters in current token.
 * <li>BigramClassFeatures: bigrams of tokens and token class.
 * <li>TrigramClassFeatures: trigrams of token and token class.
 * <li>FourgramClassFeatures: fourgrams of token and token class.
 * <li>FivegramClassFeatures: fivegrams of token and token class.
 * <li>CharNgramFeatures: character ngram features of current token.
 * <li>DictionaryFeatures: check if current token appears in some gazetteer.
 * <ol>
 * 
 * @author ragerri
 * @version 2014-07-28
 */
public class FixedTrainer extends AbstractTrainer {

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
   * Construct a trainer based on features specified in the trainParams.txt
   * properties file.
   */
  public FixedTrainer(final Properties props, final String trainData,
      final String testData, final TrainingParameters params)
      throws IOException {
    super(trainData, testData, params);
    String dictPath = props.getProperty("dictPath");
    if (dictionaries == null) {
      dictionaries = new Dictionaries(dictPath);
    }
    setFeatures(createFeatureGenerator(params));
  }

  /**
   * Construct a baseline trainer with external resources such as dictionaries
   * specified. Only for annotation.
   * 
   * @param beamsize
   *          the beamsize
   */
  public FixedTrainer(Properties props, TrainingParameters params) {
    super(params);
    String dictPath = props.getProperty("dictPath");
    if (dictionaries == null) {
      dictionaries = new Dictionaries(dictPath);
    }
    setFeatures(createFeatureGenerator(params));
  }

  /**
   * Construct a baseline trainer with only beamsize specified.
   * Only for annotation.
   * 
   * @param beamsize
   *          the beamsize
   */
  public FixedTrainer(TrainingParameters params) {
    super(params);
    setFeatures(createFeatureGenerator(params));
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * es.ehu.si.ixa.pipe.nerc.train.NameFinderTrainer#createFeatureGenerator()
   */
  public final AdaptiveFeatureGenerator createFeatureGenerator(
      TrainingParameters params) {
    List<AdaptiveFeatureGenerator> featureList = createFeatureList(params);
    AdaptiveFeatureGenerator[] featuresArray = featureList
        .toArray(new AdaptiveFeatureGenerator[featureList.size()]);
    return new CachedFeatureGenerator(featuresArray);
  }

  public static final List<AdaptiveFeatureGenerator> createFeatureList(
      TrainingParameters params) {
    List<AdaptiveFeatureGenerator> featureList = new ArrayList<AdaptiveFeatureGenerator>();
    int leftWindow = getWindowRange(params).get(0);
    int rightWindow = getWindowRange(params).get(1);
    int minLength = getNgramRange(params).get(0);
    int maxLength = getNgramRange(params).get(1);
    
    if (params.getSettings().get("TokenFeatures").equalsIgnoreCase("yes")) {
      addWindowTokenFeatures(leftWindow, rightWindow, featureList);
      System.err.println("-> Token features with added!: Window range " + leftWindow + ":" + rightWindow);
    } 
    if (params.getSettings().get("TokenClassFeatures")
        .equalsIgnoreCase("yes")) {
      addWindowTokenClassFeatures(leftWindow, rightWindow, featureList);
      System.err.println("-> Token Class features added!: Window range " + leftWindow + ":" + rightWindow);
    } 
    if (params.getSettings().get("OutcomePriorFeatures")
        .equalsIgnoreCase("yes")) {
      addOutcomePriorFeatures(featureList);
      System.err.println("-> Outcome prior features added!");
    } 
    if (params.getSettings().get("PreviousMapFeatures")
        .equalsIgnoreCase("yes")) {
      addPreviousMapFeatures(featureList);
      System.err.println("-> Previous map features added!");
    } 
    if (params.getSettings().get("SentenceFeatures")
        .equalsIgnoreCase("yes")) {
      addSentenceFeatures(featureList);
      System.err.println("-> Sentence features added!");
    }
    if (params.getSettings().get("PrefixFeatures")
        .equalsIgnoreCase("yes")) {
      addPrefixFeatures(featureList);
      System.err.println("-> Prefix features added!");
    } 
    if (params.getSettings().get("SuffixFeatures")
        .equalsIgnoreCase("yes")) {
      addSuffixFeatures(featureList);
      System.err.println("-> Suffix features added!");
    } 
    if (params.getSettings().get("BigramClassFeatures")
        .equalsIgnoreCase("yes")) {
      addBigramClassFeatures(featureList);
      System.err.println("-> Bigram class features added!");
    } 
    if (params.getSettings().get("TrigramClassFeatures")
        .equalsIgnoreCase("yes")) {
      addTrigramClassFeatures(featureList);
      System.err.println("-> Trigram class features added!");
    }
    if (params.getSettings().get("FourgramClassFeatures")
        .equalsIgnoreCase("yes")) {
      addFourgramClassFeatures(featureList);
      System.err.println("-> 4-gram class features added!");
    }
    if (params.getSettings().get("FivegramClassFeatures")
        .equalsIgnoreCase("yes")) {
      addFivegramClassFeatures(featureList);
      System.err.println("-> 5-gram class features added!");
    } 
    if (params.getSettings().get("CharNgramFeatures")
        .equalsIgnoreCase("yes")) {
      addCharNgramFeatures(minLength, maxLength, featureList);
      System.err.println("-> CharNgram features added!: Range " + minLength + ":" + maxLength);
    } 
    if (params.getSettings().get("DictionaryFeatures")
        .equalsIgnoreCase("yes")) {
      addDictionaryFeatures(featureList);
      System.err.println("-> Dictionary features added!");
    }
    return featureList;
  }

  public static void addWindowTokenFeatures(int leftWindow, int rightWindow,
      List<AdaptiveFeatureGenerator> featureList) {
    featureList.add(new WindowFeatureGenerator(new TokenFeatureGenerator(),
        leftWindow, rightWindow));
  }

  public static void addWindowTokenClassFeatures(int leftWindow,
      int rightWindow, List<AdaptiveFeatureGenerator> featureList) {
    featureList.add(new WindowFeatureGenerator(new TokenClassFeatureGenerator(
        true), leftWindow, rightWindow));
  }

  public static void addOutcomePriorFeatures(
      List<AdaptiveFeatureGenerator> featureList) {
    featureList.add(new OutcomePriorFeatureGenerator());
  }

  public static void addPreviousMapFeatures(
      List<AdaptiveFeatureGenerator> featureList) {
    featureList.add(new PreviousMapFeatureGenerator());
  }

  public static void addSentenceFeatures(
      List<AdaptiveFeatureGenerator> featureList) {
    featureList.add(new SentenceFeatureGenerator(true, false));
  }

  public static void addPrefixFeatures(
      List<AdaptiveFeatureGenerator> featureList) {
    featureList.add(new Prefix34FeatureGenerator());
  }

  public static void addSuffixFeatures(
      List<AdaptiveFeatureGenerator> featureList) {
    featureList.add(new SuffixFeatureGenerator());
  }

  public static void addBigramClassFeatures(
      List<AdaptiveFeatureGenerator> featureList) {
    featureList.add(new BigramClassFeatureGenerator());
  }

  public static void addTrigramClassFeatures(
      List<AdaptiveFeatureGenerator> featureList) {
    featureList.add(new TrigramClassFeatureGenerator());
  }
  
  public static void addFourgramClassFeatures(
      List<AdaptiveFeatureGenerator> featureList) {
    featureList.add(new FourgramClassFeatureGenerator());
  }
  
  public static void addFivegramClassFeatures(
      List<AdaptiveFeatureGenerator> featureList) {
    featureList.add(new FivegramClassFeatureGenerator());
  }

  public static void addCharNgramFeatures(int minLength, int maxLength,
      List<AdaptiveFeatureGenerator> featureList) {
    featureList.add(new CharacterNgramFeatureGenerator(minLength, maxLength));
  }

  /**
   * Adds the dictionary features to the feature list.
   * 
   * @param featureList
   *          the feature list containing the dictionary features
   */
  private static void addDictionaryFeatures(
      final List<AdaptiveFeatureGenerator> featureList) {
    for (int i = 0; i < dictionaries.getIgnoreCaseDictionaries().size(); i++) {
      prefix = dictionaries.getDictNames().get(i);
      dictionary = dictionaries.getIgnoreCaseDictionaries().get(i);
      featureList.add(new DictionaryFeatureGenerator(prefix, dictionary));
    }
  }

  public static List<Integer> getWindowRange(TrainingParameters params) {
    List<Integer> windowRange = new ArrayList<Integer>();
    String windowParam = params.getSettings().get("Window");
    String[] windowArray = windowParam.split("[ :-]");
    if (windowArray.length == 2) {
      windowRange.add(Integer.parseInt(windowArray[0]));
      windowRange.add(Integer.parseInt(windowArray[1]));

    }
    return windowRange;
  }

  public static List<Integer> getNgramRange(TrainingParameters params) {
    List<Integer> ngramRange = new ArrayList<Integer>();
    String charngramParam = params.getSettings().get("CharNgramFeaturesRange");
    String[] charngramArray = charngramParam.split("[ :-]");
    if (charngramArray.length == 2) {
      ngramRange.add(Integer.parseInt(charngramArray[0]));
      ngramRange.add(Integer.parseInt(charngramArray[1]));

    }
    return ngramRange;
  }

}
