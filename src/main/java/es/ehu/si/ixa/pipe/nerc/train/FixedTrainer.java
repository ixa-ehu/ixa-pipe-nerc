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

import opennlp.tools.util.TrainingParameters;
import es.ehu.si.ixa.pipe.nerc.dict.BrownCluster;
import es.ehu.si.ixa.pipe.nerc.dict.ClarkCluster;
import es.ehu.si.ixa.pipe.nerc.dict.Dictionaries;
import es.ehu.si.ixa.pipe.nerc.dict.Dictionary;
import es.ehu.si.ixa.pipe.nerc.dict.Word2VecCluster;
import es.ehu.si.ixa.pipe.nerc.features.AdaptiveFeatureGenerator;
import es.ehu.si.ixa.pipe.nerc.features.BigramClassFeatureGenerator;
import es.ehu.si.ixa.pipe.nerc.features.BrownBigramFeatureGenerator;
import es.ehu.si.ixa.pipe.nerc.features.BrownTokenClassFeatureGenerator;
import es.ehu.si.ixa.pipe.nerc.features.BrownTokenFeatureGenerator;
import es.ehu.si.ixa.pipe.nerc.features.CachedFeatureGenerator;
import es.ehu.si.ixa.pipe.nerc.features.CharacterNgramFeatureGenerator;
import es.ehu.si.ixa.pipe.nerc.features.DictionaryFeatureGenerator;
import es.ehu.si.ixa.pipe.nerc.features.ClarkFeatureGenerator;
import es.ehu.si.ixa.pipe.nerc.features.FivegramClassFeatureGenerator;
import es.ehu.si.ixa.pipe.nerc.features.FourgramClassFeatureGenerator;
import es.ehu.si.ixa.pipe.nerc.features.OutcomePriorFeatureGenerator;
import es.ehu.si.ixa.pipe.nerc.features.Prefix34FeatureGenerator;
import es.ehu.si.ixa.pipe.nerc.features.Prev2MapFeatureGenerator;
import es.ehu.si.ixa.pipe.nerc.features.PreviousMapFeatureGenerator;
import es.ehu.si.ixa.pipe.nerc.features.PreviousMapTokenFeatureGenerator;
import es.ehu.si.ixa.pipe.nerc.features.SentenceFeatureGenerator;
import es.ehu.si.ixa.pipe.nerc.features.SuffixFeatureGenerator;
import es.ehu.si.ixa.pipe.nerc.features.TokenClassFeatureGenerator;
import es.ehu.si.ixa.pipe.nerc.features.TokenFeatureGenerator;
import es.ehu.si.ixa.pipe.nerc.features.TrigramClassFeatureGenerator;
import es.ehu.si.ixa.pipe.nerc.features.WindowFeatureGenerator;
import es.ehu.si.ixa.pipe.nerc.features.Word2VecClusterFeatureGenerator;

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
 * <li>ClarkClusterFeatures: use the clustering class of a token as a feature.
 * <li>BrownClusterFeatures: use brown clusters as features for each feature containing a token.
 * <li>Word2VecClusterFeatures: use the word2vec clustering class of a token as a feature.
 * <ol>
 * 
 * @author ragerri
 * @version 2014-09-24
 */
public class FixedTrainer extends AbstractTrainer {

  public static final String DEFAULT_FEATURE_FLAG = "no";
  public static final String CHAR_NGRAM_RANGE = "2:5";
  public static final String DEFAULT_WINDOW = "2:2";
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
   * The Clark clustering lexicon.
   */
  private static ClarkCluster clarkCluster;
  /**
   * The Clark clustering dictionary.
   */
  private static Dictionary clarkLexicon;
  /**
   * The word2vec clustering lexicon.
   */
  private static Word2VecCluster word2vecCluster;
  /**
   * The word2vec clustering dictionary.
   */
  private static Dictionary word2vecClusterLexicon;
  /**
   * The brown cluster.
   */
  private static BrownCluster brownCluster;
  /**
   * The Brown cluster lexicon.
   */
  private static Dictionary brownLexicon;

  /**
   * Construct a trainer based on features specified in the trainParams.txt
   * properties file.
   */
  public FixedTrainer(final String trainData,
      final String testData, final TrainingParameters params)
      throws IOException {
    super(trainData, testData, params);
    
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

  /**
   * Creates the list of features to be passed to the createFeatureGenerator(params)
   * method. Every programatic use of ixa-pipe-nerc will need to implement/use a function
   * like this to train a model.
   * @param params the training parameters
   * @return the list of {@code AdaptiveFeatureGenerator}s
   */
  private final List<AdaptiveFeatureGenerator> createFeatureList(
      TrainingParameters params) {
    
    List<AdaptiveFeatureGenerator> featureList = new ArrayList<AdaptiveFeatureGenerator>();
    int leftWindow = getWindowRange(params).get(0);
    int rightWindow = getWindowRange(params).get(1);
    
    String tokenParam = InputOutputUtils.getTokenFeatures(params);
    if (tokenParam.equalsIgnoreCase("yes")) {
      addWindowTokenFeatures(leftWindow, rightWindow, featureList);
      System.err.println("-> Token features added!: Window range " + leftWindow + ":" + rightWindow);
    }
    String tokenClassParam = InputOutputUtils.getTokenClassFeatures(params);
    if (tokenClassParam.equalsIgnoreCase("yes")) {
      addWindowTokenClassFeatures(leftWindow, rightWindow, featureList);
      System.err.println("-> Token Class features added!: Window range " + leftWindow + ":" + rightWindow);
    }
    String outcomePriorParam = InputOutputUtils.getOutcomePriorFeatures(params);
    if (outcomePriorParam.equalsIgnoreCase("yes")) {
      addOutcomePriorFeatures(featureList);
      System.err.println("-> Outcome prior features added!");
    }
    String previousMapParam = InputOutputUtils.getPreviousMapFeatures(params);
    if (previousMapParam.equalsIgnoreCase("yes")) {
      addPreviousMapFeatures(leftWindow, rightWindow, featureList);
      System.err.println("-> Previous map features added!");
    }
    String sentenceParam = InputOutputUtils.getSentenceFeatures(params);
    if (sentenceParam.equalsIgnoreCase("yes")) {
      addSentenceFeatures(featureList);
      System.err.println("-> Sentence features added!");
    }
    String preffixParam = InputOutputUtils.getPreffixFeatures(params);
    if (preffixParam
        .equalsIgnoreCase("yes")) {
      addPrefixFeatures(featureList);
      System.err.println("-> Prefix features added!");
    }
    String suffixParam = InputOutputUtils.getSuffixFeatures(params);
    if (suffixParam.equalsIgnoreCase("yes")) {
      addSuffixFeatures(featureList);
      System.err.println("-> Suffix features added!");
    }
    String bigramClassParam = InputOutputUtils.getBigramClassFeatures(params);
    if (bigramClassParam.equalsIgnoreCase("yes")) {
      addBigramClassFeatures(featureList);
      System.err.println("-> Bigram class features added!");
    }
    String trigramClassParam = InputOutputUtils.getTrigramClassFeatures(params);
    if (trigramClassParam.equalsIgnoreCase("yes")) {
      addTrigramClassFeatures(featureList);
      System.err.println("-> Trigram class features added!");
    }
    String fourgramClassParam = InputOutputUtils.getFourgramClassFeatures(params);
    if (fourgramClassParam.equalsIgnoreCase("yes")) {
      addFourgramClassFeatures(featureList);
      System.err.println("-> 4-gram class features added!");
    }
    String fivegramClassParam = InputOutputUtils.getFivegramClassFeatures(params);
    if (fivegramClassParam.equalsIgnoreCase("yes")) {
      addFivegramClassFeatures(featureList);
      System.err.println("-> 5-gram class features added!");
    }
    String charNgramParam = InputOutputUtils.getCharNgramFeatures(params);
    if (charNgramParam.equalsIgnoreCase("yes")) {
      int minLength = getNgramRange(params).get(0);
      int maxLength = getNgramRange(params).get(1);
      addCharNgramFeatures(minLength, maxLength, featureList);
      System.err.println("-> CharNgram features added!: Range " + minLength + ":" + maxLength);
    }
    String dictionaryParam = InputOutputUtils.getDictionaryFeatures(params);
    if (dictionaryParam.equalsIgnoreCase("yes")) {
      System.err.println("-> Dictionary features added!");
      String dictPath = InputOutputUtils.getDictPath(params);
      if (dictionaries == null) {
        dictionaries = new Dictionaries(dictPath);
      }
      addDictionaryFeatures(leftWindow, rightWindow, featureList);
    }
    String distSimParam = InputOutputUtils.getClarkFeatures(params);
    if (distSimParam.equalsIgnoreCase("yes")) {
      System.err.println("-> Clark clusters features added!");
      String distSimPath = InputOutputUtils.getClarkPath(params);
      if (clarkCluster == null) {
        clarkCluster = new ClarkCluster(distSimPath);
      }
      addClarkFeatures(leftWindow, rightWindow, featureList);
    }
    
    String brownParam = InputOutputUtils.getBrownFeatures(params);
    if (brownParam.equalsIgnoreCase("yes")) {
      System.err.println("-> Brown clusters features added!");
      brownLexicon = setBrownResources(params, brownParam);
      addBrownFeatures(leftWindow, rightWindow, featureList);
    }
    
    String word2vecClusterParam = InputOutputUtils.getWord2VecClusterFeatures(params);
    if (word2vecClusterParam.equalsIgnoreCase("yes")) {
      System.err.println("-> Word2vec clusters features added!");
      String word2vecClusterPath = InputOutputUtils.getWord2VecClusterPath(params);
      if (word2vecCluster == null) {
        word2vecCluster = new Word2VecCluster(word2vecClusterPath);
      }
      addWord2VecClusterFeatures(leftWindow, rightWindow, featureList);
    }
    return featureList;
  }

  /**
   * Adds window token features to the feature list.
   * @param leftWindow the leftwindow value
   * @param rightWindow the rightwindow value
   * @param featureList the feature list to which the feature generator is added
   */
  public static void addWindowTokenFeatures(int leftWindow, int rightWindow,
      List<AdaptiveFeatureGenerator> featureList) {
      featureList.add(new WindowFeatureGenerator(new TokenFeatureGenerator(),
          leftWindow, rightWindow)); 
  }
  
  /**
   * Add window token class/shape features to the feature list.
   * @param leftWindow the leftwindow value
   * @param rightWindow the rightwindow value
   * @param featureList the feature list to which the feature generator is added
   */
  public static void addWindowTokenClassFeatures(int leftWindow,
      int rightWindow, List<AdaptiveFeatureGenerator> featureList) {
    featureList.add(new WindowFeatureGenerator(new TokenClassFeatureGenerator(
        true), leftWindow, rightWindow));
  }

  /**
   * Adds a prior outcome to the feature list.
   * @param featureList the feature list to which the feature generator is added
   */
  public static void addOutcomePriorFeatures(
      List<AdaptiveFeatureGenerator> featureList) {
    featureList.add(new OutcomePriorFeatureGenerator());
  }

  /**
   * Adds the previous outcome for each token as a feature.
   * @param featureList the feature list to which the feature generator is added
   */
  public static void addPreviousMapFeatures(int leftWindow, int rightWindow,
      List<AdaptiveFeatureGenerator> featureList) {
    //TODO clarify this and other Ratinov and Roth baseline features
    //featureList.add(new PreviousMapFeatureGenerator());
    featureList.add(new Prev2MapFeatureGenerator());
    featureList.add(new WindowFeatureGenerator(leftWindow, rightWindow, new PreviousMapTokenFeatureGenerator()));
  }
  
  /**
   * Add sentence end and begin as features.
   * @param featureList the feature list
   */
  public static void addSentenceFeatures(
      List<AdaptiveFeatureGenerator> featureList) {
    featureList.add(new SentenceFeatureGenerator(true, false));
  }

  /**
   * Add the first 4 characters of the token as feature.
   * @param featureList the feature list
   */
  public static void addPrefixFeatures(
      List<AdaptiveFeatureGenerator> featureList) {
    featureList.add(new Prefix34FeatureGenerator());
  }

  /**
   * Add the last 4 characters of the token as a feature.
   * @param featureList the feature list
   */
  public static void addSuffixFeatures(
      List<AdaptiveFeatureGenerator> featureList) {
    featureList.add(new SuffixFeatureGenerator());
  }

  /**
   * Add token and token shape bigram features.
   * @param featureList the feature list
   */
  public static void addBigramClassFeatures(
      List<AdaptiveFeatureGenerator> featureList) {
    featureList.add(new BigramClassFeatureGenerator());
  }

  /**
   * Add token and token shape trigram features.
   * @param featureList the feature list
   */
  public static void addTrigramClassFeatures(
      List<AdaptiveFeatureGenerator> featureList) {
    featureList.add(new TrigramClassFeatureGenerator());
  }
  
  /**
   * Add the fourgram token and token shape features.
   * @param featureList the feature list
   */
  public static void addFourgramClassFeatures(
      List<AdaptiveFeatureGenerator> featureList) {
    featureList.add(new FourgramClassFeatureGenerator());
  }
  
  /**
   * Add the fivegram token and token shape features.
   * @param featureList the feature list
   */
  public static void addFivegramClassFeatures(
      List<AdaptiveFeatureGenerator> featureList) {
    featureList.add(new FivegramClassFeatureGenerator());
  }

  /**
   * Add the character ngram features of the current token.
   * @param minLength the minimun ngram
   * @param maxLength the maximum ngram 
   * @param featureList the feature list
   */
  public static void addCharNgramFeatures(int minLength, int maxLength,
      List<AdaptiveFeatureGenerator> featureList) {
    featureList.add(new CharacterNgramFeatureGenerator(minLength, maxLength));
  }

  /**
   * Adds the dictionary features of the curren token in a given window.
   * @param leftWindow the left window
   * @param rightWindow the right window
   * @param featureList the feature list
   */
  private static void addDictionaryFeatures(int leftWindow, int rightWindow,
      final List<AdaptiveFeatureGenerator> featureList) {
    for (int i = 0; i < dictionaries.getIgnoreCaseDictionaries().size(); i++) {
      prefix = dictionaries.getDictNames().get(i);
      dictionary = dictionaries.getIgnoreCaseDictionaries().get(i);
      featureList.add(new WindowFeatureGenerator(leftWindow, rightWindow, new DictionaryFeatureGenerator(prefix, dictionary)));
    }
  }
  
  /**
   * Add the Clark cluster class of the current token as a feature in a given window.
   * @param leftWindow the left window
   * @param rightWindow the right window
   * @param featureList the feature list
   */
  private static void addClarkFeatures(int leftWindow, int rightWindow, final List<AdaptiveFeatureGenerator> featureList) {
    clarkLexicon = clarkCluster.getIgnoreCaseDictionary();
    featureList.add(new WindowFeatureGenerator(leftWindow, rightWindow, new ClarkFeatureGenerator(clarkLexicon)));
  }
  
  /**
   * Adds Brown classes features for each token feature in a given window.
   * @param leftWindow the leftwindow value
   * @param rightWindow the rightwindow value
   * @param featureList the feature list to which the feature generator is added
   */
  private static void addBrownFeatures(int leftWindow, int rightWindow, final List<AdaptiveFeatureGenerator> featureList) {
    featureList.add(new WindowFeatureGenerator(new BrownTokenFeatureGenerator(brownLexicon), leftWindow, rightWindow));
    featureList.add(new WindowFeatureGenerator(new BrownTokenClassFeatureGenerator(brownLexicon), leftWindow, rightWindow));
    featureList.add(new BrownBigramFeatureGenerator(brownLexicon));
  }

  
  /**
   * Add the word2vec cluster class feature of the curren token in a given window.
   * @param leftWindow the left window
   * @param rightWindow the right window
   * @param featureList the feature list
   */
  private static void addWord2VecClusterFeatures(int leftWindow, int rightWindow, final List<AdaptiveFeatureGenerator> featureList) {
    word2vecClusterLexicon = word2vecCluster.getIgnoreCaseDictionary();
    featureList.add(new WindowFeatureGenerator(leftWindow, rightWindow, new Word2VecClusterFeatureGenerator(word2vecClusterLexicon)));
  }

  /**
   * Get the window range feature.
   * @param params the training parameters
   * @return the list containing the left and right window values
   */
  public static List<Integer> getWindowRange(TrainingParameters params) {
    List<Integer> windowRange = new ArrayList<Integer>();
    String windowParam = InputOutputUtils.getWindow(params);
    String[] windowArray = windowParam.split("[ :-]");
    if (windowArray.length == 2) {
      windowRange.add(Integer.parseInt(windowArray[0]));
      windowRange.add(Integer.parseInt(windowArray[1]));
    }
    return windowRange;
  }

  /**
   * Get the range of the character ngram of current token.
   * @param params the training parameters
   * @return a list containing the initial and maximum ngram values
   */
  public static List<Integer> getNgramRange(TrainingParameters params) {
    List<Integer> ngramRange = new ArrayList<Integer>();
      String charNgramParam = InputOutputUtils.getCharNgramFeaturesRange(params);
      String[] charngramArray = charNgramParam.split("[ :-]");
      if (charngramArray.length == 2) {
        ngramRange.add(Integer.parseInt(charngramArray[0]));
        ngramRange.add(Integer.parseInt(charngramArray[1]));

      }
    return ngramRange;
  }
  
  /**
   * Initialize Brown resources.
   * @param params the training parameters
   * @param brownParam the brown parameter
   * @return the {@code Dictionary} containing the {@code BrownCluster} lexicon
   */
  public static Dictionary setBrownResources(TrainingParameters params, String brownParam) {
    if (brownParam.equalsIgnoreCase("yes")) {
      String brownClusterPath = InputOutputUtils.getBrownClusterPath(params);
      if (brownCluster == null) {
        brownCluster = new BrownCluster(brownClusterPath);
      }
    }
    return brownCluster.getDictionary();
  }

}
