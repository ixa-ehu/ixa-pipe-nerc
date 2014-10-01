/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package es.ehu.si.ixa.pipe.nerc.train;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import opennlp.tools.namefind.BioCodec;
import opennlp.tools.util.BaseToolFactory;
import opennlp.tools.util.InvalidFormatException;
import opennlp.tools.util.SequenceCodec;
import opennlp.tools.util.TrainingParameters;
import opennlp.tools.util.ext.ExtensionLoader;
import es.ehu.si.ixa.pipe.nerc.dict.BrownCluster;
import es.ehu.si.ixa.pipe.nerc.dict.ClarkCluster;
import es.ehu.si.ixa.pipe.nerc.dict.Dictionaries;
import es.ehu.si.ixa.pipe.nerc.dict.Dictionary;
import es.ehu.si.ixa.pipe.nerc.dict.Word2VecCluster;
import es.ehu.si.ixa.pipe.nerc.features.AdaptiveFeatureGenerator;
import es.ehu.si.ixa.pipe.nerc.features.AggregatedFeatureGenerator;
import es.ehu.si.ixa.pipe.nerc.features.BigramClassFeatureGenerator;
import es.ehu.si.ixa.pipe.nerc.features.BrownBigramFeatureGenerator;
import es.ehu.si.ixa.pipe.nerc.features.BrownTokenClassFeatureGenerator;
import es.ehu.si.ixa.pipe.nerc.features.BrownTokenFeatureGenerator;
import es.ehu.si.ixa.pipe.nerc.features.CachedFeatureGenerator;
import es.ehu.si.ixa.pipe.nerc.features.CharacterNgramFeatureGenerator;
import es.ehu.si.ixa.pipe.nerc.features.ClarkFeatureGenerator;
import es.ehu.si.ixa.pipe.nerc.features.DictionaryFeatureGenerator;
import es.ehu.si.ixa.pipe.nerc.features.FivegramClassFeatureGenerator;
import es.ehu.si.ixa.pipe.nerc.features.FourgramClassFeatureGenerator;
import es.ehu.si.ixa.pipe.nerc.features.NameContextGenerator;
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

public class NameClassifierFactory extends BaseToolFactory {

  private static final String GAZ_DICTIONARIES = "gaz.dictionaries";
  private static final String BROWN_CLUSTER_ENTRY_NAME = "brown.cluster";
  private static final String CLARK_CLUSTER_ENTRY_NAME = "clark.cluster";
  private static final String WORD2VEC_CLUSTER_ENTRY_NAME = "word2vec.cluster";
  private static final String WORD2VEC_EMBEDDINGS_ENTRY_NAME = "word2vec.embedding";

  private SequenceCodec<String> seqCodec;

  /**
   * The {@link Dictionaries} contained in the given directory.
   */
  private Dictionaries gazDictionaries;
  /**
   * A {@link Dictionary} object.
   */
  private Dictionary gazDictionary;
  /**
   * The prefix to be used in the {@link DictionaryFeatureGenerator}.
   */
  private static String prefix;
  /**
   * The Clark clustering lexicon.
   */
  private ClarkCluster clarkCluster;
  /**
   * The Clark clustering dictionary.
   */
  private Dictionary clarkLexicon;
  /**
   * The word2vec clustering lexicon.
   */
  private Word2VecCluster word2vecCluster;
  /**
   * The word2vec clustering dictionary.
   */
  private Dictionary word2vecClusterLexicon;
  /**
   * The brown cluster.
   */
  private BrownCluster brownCluster;
  /**
   * The Brown cluster lexicon.
   */
  private Dictionary brownLexicon;

  /**
   * Creates a {@link NameClassifierFactory} that provides the default
   * implementation of the resources.
   */
  public NameClassifierFactory() {
    this.seqCodec = new BioCodec();
  }

  /**
   * Construct a NameClassifierFactory with parameters and external resources.
   * @param params
   * @param aGazDictionaries
   * @param aBrownCluster
   * @param aClarkCluster
   * @param aWord2VecCluster
   * @param seqCodec
   */
  public NameClassifierFactory(final Dictionaries aGazDictionaries,
      BrownCluster aBrownCluster, ClarkCluster aClarkCluster,
      Word2VecCluster aWord2VecCluster, SequenceCodec<String> seqCodec) {
    init(aGazDictionaries, aBrownCluster, aClarkCluster, aWord2VecCluster,
        seqCodec);
  }

  void init(final Dictionaries aGazDictionaries, BrownCluster aBrownCluster,
      ClarkCluster aClarkCluster, Word2VecCluster aWord2VecCluster,
      SequenceCodec<String> seqCodec) {
    this.gazDictionaries = aGazDictionaries;
    this.brownCluster = aBrownCluster;
    this.clarkCluster = aClarkCluster;
    this.word2vecCluster = aWord2VecCluster;
    this.seqCodec = seqCodec;
  }

  protected SequenceCodec<String> getSequenceCodec() {
    return seqCodec;
  }

  public Map<String, Object> createArtifactMap() {
    Map<String, Object> artifactMap = super.createArtifactMap();
    
    if (gazDictionaries != null) {
      artifactMap.put(GAZ_DICTIONARIES, gazDictionary);
    }
    if (brownCluster != null) {
      artifactMap.put(BROWN_CLUSTER_ENTRY_NAME, brownCluster);
    }
    if (clarkCluster != null) {
      artifactMap.put(CLARK_CLUSTER_ENTRY_NAME, clarkCluster);
    }
    if (word2vecCluster != null) {
      artifactMap.put(WORD2VEC_CLUSTER_ENTRY_NAME, word2vecCluster);
    }
    return artifactMap;
  }

  public Dictionaries createGazetteers(String dictPath) {
    return new Dictionaries(dictPath);
  }

  public BrownCluster createBrownCluster(String dictPath) {
    return new BrownCluster(dictPath);
  }

  public ClarkCluster createClarkCluster(String dictPath) {
    return new ClarkCluster(dictPath);
  }

  public Word2VecCluster createWord2VecCluster(String dictPath) {
    return new Word2VecCluster(dictPath);
  }

  public void setGazetteers(Dictionaries dictionaries) {
    if (artifactProvider != null) {
      throw new IllegalStateException(
          "Can not set tag dictionary while using artifact provider.");
    }
    this.gazDictionaries = dictionaries;
  }

  public void setBrownCluster(BrownCluster aBrownCluster) {
    if (artifactProvider != null) {
      throw new IllegalStateException(
          "Can not set a Brown cluster while using artifact provider.");
    }
    this.brownCluster = aBrownCluster;
  }

  public void setClarkCluster(ClarkCluster aClarkCluster) {
    if (artifactProvider != null) {
      throw new IllegalStateException(
          "Can not set a Clark cluster while using artifact provider.");
    }
    this.clarkCluster = aClarkCluster;
  }

  public void setWord2VecCluster(Word2VecCluster aWord2vecCluster) {
    if (artifactProvider != null) {
      throw new IllegalStateException(
          "Can not set a word2vec cluster while using artifact provider.");
    }
    this.word2vecCluster = aWord2vecCluster;
  }

  public Dictionaries getGazetteers() {
    if (this.gazDictionaries == null && artifactProvider != null) {
      this.gazDictionaries = artifactProvider.getArtifact(GAZ_DICTIONARIES);
    }
    return this.gazDictionaries;
  }

  public BrownCluster getBrownCluster() {
    if (this.brownCluster == null && artifactProvider != null) {
      this.brownCluster = artifactProvider
          .getArtifact(BROWN_CLUSTER_ENTRY_NAME);
    }
    return this.brownCluster;
  }
  
  public ClarkCluster getClarkCluster() {
    if (this.clarkCluster == null && artifactProvider != null) {
      this.clarkCluster = artifactProvider
          .getArtifact(CLARK_CLUSTER_ENTRY_NAME);
    }
    return this.clarkCluster;
  }
  
  public Word2VecCluster getWord2VecCluster() {
    if (this.word2vecCluster == null && artifactProvider != null) {
      this.word2vecCluster = artifactProvider
          .getArtifact(WORD2VEC_CLUSTER_ENTRY_NAME);
    }
    return this.word2vecCluster;
  }
  
  public SequenceCodec<String> createSequenceCodec() {
    if (artifactProvider != null) {
      String sequenceCodecImplName = artifactProvider
          .getManifestProperty(NameModel.SEQUENCE_CODEC_CLASS_NAME_PARAMETER);
      return instantiateSequenceCodec(sequenceCodecImplName);
    } else {
      return seqCodec;
    }
  }

  @SuppressWarnings("unchecked")
  public static SequenceCodec<String> instantiateSequenceCodec(
      String sequenceCodecImplName) {
    if (sequenceCodecImplName != null) {
      return ExtensionLoader.instantiateExtension(SequenceCodec.class,
          sequenceCodecImplName);
    } else {
      // If nothing is specified return old default!
      return new BioCodec();
    }
  }
  
  public static NameClassifierFactory create(String subclassName, Dictionaries aDictionaries,
      BrownCluster aBrownCluster, ClarkCluster aClarkCluster, Word2VecCluster aWord2vecCluster,
      SequenceCodec<String> seqCodec)
      throws InvalidFormatException {
    if (subclassName == null) {
      // will create the default factory
      return new NameClassifierFactory();
    }
    try {
      NameClassifierFactory theFactory = ExtensionLoader.instantiateExtension(
          NameClassifierFactory.class, subclassName);
      theFactory.init(aDictionaries, aBrownCluster, aClarkCluster, aWord2vecCluster, seqCodec);
      return theFactory;
    } catch (Exception e) {
      String msg = "Could not instantiate the " + subclassName
          + ". The initialization throw an exception.";
      System.err.println(msg);
      e.printStackTrace();
      throw new InvalidFormatException(msg, e);
    }
  }

  @Override
  public void validateArtifactMap() throws InvalidFormatException {
    // no additional artifacts
  }
  

  public NameContextGenerator createContextGenerator() {

    AdaptiveFeatureGenerator featureGenerator = createFeatureGenerators();
    if (featureGenerator == null) {
      featureGenerator = NameClassifier.createFeatureGenerator();
    }
    return new DefaultNameContextGenerator(featureGenerator);
  }
  
  /**
   * Creates the {@link AdaptiveFeatureGenerator}. Usually this is a set of
   * generators contained in the {@link AggregatedFeatureGenerator}.
   * 
   * Note: The generators are created on every call to this method.
   * 
   * @return the feature generator or null if there is no descriptor in the
   *         model
   */
  // TODO: During training time the resources need to be loaded from the
  // resources map!
  public AdaptiveFeatureGenerator createFeatureGenerators() {

    TrainingParameters params = FixedTrainer.getTrainingParameters();
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
    if (!dictionaryParam.equalsIgnoreCase(FixedTrainer.DEFAULT_FEATURE_FLAG)) {
      System.err.println("-> Dictionary features added!");
      addDictionaryFeatures(leftWindow, rightWindow, featureList);
    }
    String brownParam = InputOutputUtils.getBrownFeatures(params);
    if (!brownParam.equalsIgnoreCase(FixedTrainer.DEFAULT_FEATURE_FLAG)) {
      System.err.println("-> Brown clusters features added!");
      addBrownFeatures(leftWindow, rightWindow, featureList);
    }
    String clarkParam = InputOutputUtils.getClarkFeatures(params);
    if (!clarkParam.equalsIgnoreCase(FixedTrainer.DEFAULT_FEATURE_FLAG)) {
      System.err.println("-> Clark clusters features added!");
      addClarkFeatures(leftWindow, rightWindow, featureList);
    }
    String word2vecClusterParam = InputOutputUtils.getWord2VecClusterFeatures(params);
    if (!word2vecClusterParam.equalsIgnoreCase(FixedTrainer.DEFAULT_FEATURE_FLAG)) {
      System.err.println("-> Word2vec clusters features added!");
      addWord2VecClusterFeatures(leftWindow, rightWindow, featureList);
      //addWord2VecClusterFeatures(featureList);
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
    featureList.add(new PreviousMapFeatureGenerator());
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
  private void addDictionaryFeatures(int leftWindow, int rightWindow,
      final List<AdaptiveFeatureGenerator> featureList) {
    for (int i = 0; i < gazDictionaries.getIgnoreCaseDictionaries().size(); i++) {
      prefix = gazDictionaries.getDictNames().get(i);
      gazDictionary = gazDictionaries.getIgnoreCaseDictionaries().get(i);
      featureList.add(new WindowFeatureGenerator(new DictionaryFeatureGenerator(prefix, gazDictionary), leftWindow, rightWindow));
    }
  }
  
  /**
   * Adds Brown classes features for each token feature in a given window.
   * @param leftWindow the leftwindow value
   * @param rightWindow the rightwindow value
   * @param featureList the feature list to which the feature generator is added
   */
  private void addBrownFeatures(int leftWindow, int rightWindow, final List<AdaptiveFeatureGenerator> featureList) {
    featureList.add(new WindowFeatureGenerator(new BrownTokenFeatureGenerator(brownLexicon), leftWindow, rightWindow));
    featureList.add(new WindowFeatureGenerator(new BrownTokenClassFeatureGenerator(brownLexicon), leftWindow, rightWindow));
    featureList.add(new BrownBigramFeatureGenerator(brownLexicon));
    featureList.add(new Prev2MapFeatureGenerator());
    featureList.add(new WindowFeatureGenerator(new PreviousMapTokenFeatureGenerator(), leftWindow, rightWindow));
  }

  /**
   * Add the Clark cluster class of the current token as a feature in a given window.
   * @param leftWindow the left window
   * @param rightWindow the right window
   * @param featureList the feature list
   */
  private void addClarkFeatures(int leftWindow, int rightWindow, final List<AdaptiveFeatureGenerator> featureList) {
    clarkLexicon = clarkCluster.getIgnoreCaseDictionary();
    featureList.add(new WindowFeatureGenerator(new ClarkFeatureGenerator(clarkLexicon), leftWindow, rightWindow));
  }
  
  /**
   * Add the word2vec cluster class feature of the curren token in a given window.
   * @param leftWindow the left window
   * @param rightWindow the right window
   * @param featureList the feature list
   */
  private void addWord2VecClusterFeatures(int leftWindow, int rightWindow, final List<AdaptiveFeatureGenerator> featureList) {
    word2vecClusterLexicon = word2vecCluster.getIgnoreCaseDictionary();
    featureList.add(new WindowFeatureGenerator(new Word2VecClusterFeatureGenerator(word2vecClusterLexicon), leftWindow, rightWindow));
  }

  /**
   * Add the word2vec cluster class feature of the curren token in a given window.
   * @param leftWindow the left window
   * @param rightWindow the right window
   * @param featureList the feature list
   */
  private void addWord2VecClusterFeatures(final List<AdaptiveFeatureGenerator> featureList) {
    word2vecClusterLexicon = word2vecCluster.getIgnoreCaseDictionary();
    featureList.add(new Word2VecClusterFeatureGenerator(word2vecClusterLexicon));
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

  
}
