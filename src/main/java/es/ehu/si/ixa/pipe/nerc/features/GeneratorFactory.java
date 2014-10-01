package es.ehu.si.ixa.pipe.nerc.features;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import opennlp.tools.util.InvalidFormatException;
import opennlp.tools.util.TrainingParameters;
import opennlp.tools.util.featuregen.FeatureGeneratorResourceProvider;
import es.ehu.si.ixa.pipe.nerc.dict.BrownCluster;
import es.ehu.si.ixa.pipe.nerc.dict.ClarkCluster;
import es.ehu.si.ixa.pipe.nerc.dict.Dictionaries;
import es.ehu.si.ixa.pipe.nerc.dict.Dictionary;
import es.ehu.si.ixa.pipe.nerc.dict.Word2VecCluster;
import es.ehu.si.ixa.pipe.nerc.train.FixedTrainer;
import es.ehu.si.ixa.pipe.nerc.train.InputOutputUtils;

/**
 * @author ragerri
 *
 */
public class GeneratorFactory {
  
  private static int DEFAULT_WINDOW_SIZE = 2;
  private static int minLength = 2;
  private static int maxLength = 5;
  /**
   * The {@link Dictionaries} contained in the given directory.
   */
  private static Dictionaries gazDictionaries;
  /**
   * A {@link Dictionary} object.
   */
  private static Dictionary gazDictionary;
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
  
  static interface ParamsFeatureGeneratorFactory {

    /**
     * Creates an {@link AdaptiveFeatureGenerator} from a the describing
     * XML element.
     *
     * @param generatorElement the element which contains the configuration
     * @param resourceManager the resource manager which could be used
     *     to access referenced resources
     *
     * @return the configured {@link AdaptiveFeatureGenerator}
     */
    AdaptiveFeatureGenerator create(String featureName, 
        FeatureGeneratorResourceProvider resourceManager) throws InvalidFormatException;
  }
  /** 
   * Window token generator factory.
   * @author ragerri
   *
   */
  static class WindowTokenFeatureGeneratorFactory implements ParamsFeatureGeneratorFactory {

    public AdaptiveFeatureGenerator create(String featureName,
        FeatureGeneratorResourceProvider resourceManager) {
      return new WindowFeatureGenerator(new TokenFeatureGenerator(), DEFAULT_WINDOW_SIZE, DEFAULT_WINDOW_SIZE);
    }
    static void register(Map<String, ParamsFeatureGeneratorFactory> factoryMap) {
      factoryMap.put("TokenFeatures", new WindowTokenFeatureGeneratorFactory());
    }
  }
  /**
   * Window token class generator factory.
   * @author ragerri
   *
   */
  static class WindowTokenClassFeatureGeneratorFactory implements ParamsFeatureGeneratorFactory {

    public AdaptiveFeatureGenerator create(String featureName,
        FeatureGeneratorResourceProvider resourceManager) {
      return new WindowFeatureGenerator(new TokenClassFeatureGenerator(true), DEFAULT_WINDOW_SIZE, DEFAULT_WINDOW_SIZE);
    }
    static void register(Map<String, ParamsFeatureGeneratorFactory> factoryMap) {
      factoryMap.put("TokenClassFeatures", new WindowTokenFeatureGeneratorFactory());
    }
  }
  /**
   * Outcome prior generator factory.
   * @author ragerri
   *
   */
  static class OutcomePriorFeatureGeneratorFactory implements ParamsFeatureGeneratorFactory {

    public AdaptiveFeatureGenerator create(String featureName,
        FeatureGeneratorResourceProvider resourceManager) {
      return new OutcomePriorFeatureGenerator();
    }
    static void register(Map<String, ParamsFeatureGeneratorFactory> factoryMap) {
      factoryMap.put("OutcomePriorFeatures", new OutcomePriorFeatureGeneratorFactory());
    }
  }
  /**
   * Previous map generator factory.
   * @author ragerri
   *
   */
  static class PreviousMapFeatureGeneratorFactory implements ParamsFeatureGeneratorFactory {

    public AdaptiveFeatureGenerator create(String featureName,
        FeatureGeneratorResourceProvider resourceManager) {
      return new WindowFeatureGenerator(new PreviousMapFeatureGenerator(), DEFAULT_WINDOW_SIZE, DEFAULT_WINDOW_SIZE);
    }
    static void register(Map<String, ParamsFeatureGeneratorFactory> factoryMap) {
      factoryMap.put("PreviousMapFeatures", new PreviousMapFeatureGeneratorFactory());
    }
  }
  /**
   * Sentence feature generator factory.
   * @author ragerri
   *
   */
  static class SentenceFeatureGeneratorFactory implements ParamsFeatureGeneratorFactory {

    public AdaptiveFeatureGenerator create(String featureName,
        FeatureGeneratorResourceProvider resourceManager) {
      return new SentenceFeatureGenerator(true, false);
    }
    static void register(Map<String, ParamsFeatureGeneratorFactory> factoryMap) {
      factoryMap.put("SentenceFeatures", new SentenceFeatureGeneratorFactory());
    }
  }
  /**
   * Prefix feature generator factory.
   * @author ragerri
   *
   */
  static class PrefixFeatureGeneratorFactory implements ParamsFeatureGeneratorFactory {

    public AdaptiveFeatureGenerator create(String featureName,
        FeatureGeneratorResourceProvider resourceManager) {
      return new Prefix34FeatureGenerator();
    }
    static void register(Map<String, ParamsFeatureGeneratorFactory> factoryMap) {
      factoryMap.put("PrefixFeatures", new PrefixFeatureGeneratorFactory());
    }
  }
  /**
   * Suffix feature generator factory.
   * @author ragerri
   *
   */
  static class SuffixFeatureGeneratorFactory implements ParamsFeatureGeneratorFactory {

    public AdaptiveFeatureGenerator create(String featureName,
        FeatureGeneratorResourceProvider resourceManager) {
      return new SuffixFeatureGenerator();
    }
    static void register(Map<String, ParamsFeatureGeneratorFactory> factoryMap) {
      factoryMap.put("SuffixFeatures", new SuffixFeatureGeneratorFactory());
    }
  }
  /**
   * Bigram class feature generator factory.
   * @author ragerri
   *
   */
  static class BigramClassFeatureGeneratorFactory implements ParamsFeatureGeneratorFactory {

    public AdaptiveFeatureGenerator create(String featureName,
        FeatureGeneratorResourceProvider resourceManager) {
      return new BigramClassFeatureGenerator();
    }
    static void register(Map<String, ParamsFeatureGeneratorFactory> factoryMap) {
      factoryMap.put("BigramClassFeatures", new BigramClassFeatureGeneratorFactory());
    }
  }
  
  
  
  /**
   * Map containing the feature generator factories.
   */
  private static Map<String, ParamsFeatureGeneratorFactory> factories =
      new HashMap<String, ParamsFeatureGeneratorFactory>();

  static {
    WindowTokenFeatureGeneratorFactory.register(factories);
    WindowTokenClassFeatureGeneratorFactory.register(factories);
    OutcomePriorFeatureGeneratorFactory.register(factories);
    PreviousMapFeatureGeneratorFactory.register(factories);
    SentenceFeatureGeneratorFactory.register(factories);
    PrefixFeatureGeneratorFactory.register(factories);
    SuffixFeatureGeneratorFactory.register(factories);
    BigramClassFeatureGeneratorFactory.register(factories);
  }

  /**
   * Creates a {@link AdaptiveFeatureGenerator} for the provided element.
   * To accomplish this it looks up the corresponding factory by the
   * element tag name. The factory is then responsible for the creation
   * of the generator from the element.
   *
   * @param generatorElement
   * @param resourceManager
   *
   * @return
   */
  static AdaptiveFeatureGenerator createGenerator(String featureName,
      FeatureGeneratorResourceProvider resourceManager) throws InvalidFormatException {

    ParamsFeatureGeneratorFactory generatorFactory = factories.get(featureName);
    if (generatorFactory == null) {
      throw new InvalidFormatException("Unexpected element: " + featureName);
    }
    return generatorFactory.create(featureName, resourceManager);
  }
  
  
  /**
  * Creates an {@link AdaptiveFeatureGenerator} from an provided XML descriptor.
  *
  * Usually this XML descriptor contains a set of nested feature generators
  * which are then used to generate the features by one of the opennlp
  * components.
  *
  * @param xmlDescriptorIn the {@link InputStream} from which the descriptor
  * is read, the stream remains open and must be closed by the caller.
  *
  * @param resourceManager the resource manager which is used to resolve resources
  * referenced by a key in the descriptor
  *
  * @return created feature generators
  *
  * @throws IOException if an error occurs during reading from the descriptor
  *     {@link InputStream}
  */
 public static AdaptiveFeatureGenerator create(TrainingParameters params,
     FeatureGeneratorResourceProvider resourceManager) throws IOException, InvalidFormatException {

   List<String> featureList = getFeatureList(params);
   AdaptiveFeatureGenerator featureGenerator = null;
   for (String featureName : featureList) {
     featureGenerator = createGenerator(featureName, resourceManager);
   }
   return new CachedFeatureGenerator(featureGenerator);
 }
  
 
  /**
   * Creates the list of features to be passed to the createFeatureGenerator(params)
   * method. Every programatic use of ixa-pipe-nerc will need to implement/use a function
   * like this to train a model.
   * @param params the training parameters
   * @return the list of {@code AdaptiveFeatureGenerator}s
   */
  private final static List<String> getFeatureList(
      TrainingParameters params) {
    
    List<String> featureList = new ArrayList<String>();
  
    String tokenParam = InputOutputUtils.getTokenFeatures(params);
    if (tokenParam.equalsIgnoreCase("yes")) {
      featureList.add("TokenFeatures");
      System.err.println("-> Token features added!: Window range " + DEFAULT_WINDOW_SIZE + ":" + DEFAULT_WINDOW_SIZE);
    }
    String tokenClassParam = InputOutputUtils.getTokenClassFeatures(params);
    if (tokenClassParam.equalsIgnoreCase("yes")) {
      featureList.add("TokenClassFeatures");
      System.err.println("-> Token Class features added!: Window range " + DEFAULT_WINDOW_SIZE + ":" + DEFAULT_WINDOW_SIZE);
    }
    String outcomePriorParam = InputOutputUtils.getOutcomePriorFeatures(params);
    if (outcomePriorParam.equalsIgnoreCase("yes")) {
      featureList.add("OutcomePriorFeatures");
      System.err.println("-> Outcome prior features added!");
    }
    String previousMapParam = InputOutputUtils.getPreviousMapFeatures(params);
    if (previousMapParam.equalsIgnoreCase("yes")) {
      featureList.add("PreviousMapFeatures");
      System.err.println("-> Previous map features added!");
    }
    String sentenceParam = InputOutputUtils.getSentenceFeatures(params);
    if (sentenceParam.equalsIgnoreCase("yes")) {
      featureList.add("SentenceFeatures");
      System.err.println("-> Sentence features added!");
    }
    String preffixParam = InputOutputUtils.getPreffixFeatures(params);
    if (preffixParam
        .equalsIgnoreCase("yes")) {
      featureList.add("PrefixFeatures");
      System.err.println("-> Prefix features added!");
    }
    String suffixParam = InputOutputUtils.getSuffixFeatures(params);
    if (suffixParam.equalsIgnoreCase("yes")) {
      featureList.add("SuffixFeatures");
      System.err.println("-> Suffix features added!");
    }
    String bigramClassParam = InputOutputUtils.getBigramClassFeatures(params);
    if (bigramClassParam.equalsIgnoreCase("yes")) {
      featureList.add("BigramClassFeatures");
      System.err.println("-> Bigram class features added!");
    }
    String trigramClassParam = InputOutputUtils.getTrigramClassFeatures(params);
    if (trigramClassParam.equalsIgnoreCase("yes")) {
      featureList.add("TrigramClassFeatures");
      System.err.println("-> Trigram class features added!");
    }
    String fourgramClassParam = InputOutputUtils.getFourgramClassFeatures(params);
    if (fourgramClassParam.equalsIgnoreCase("yes")) {
      featureList.add("FourgramClassFeatures");
      System.err.println("-> 4-gram class features added!");
    }
    String fivegramClassParam = InputOutputUtils.getFivegramClassFeatures(params);
    if (fivegramClassParam.equalsIgnoreCase("yes")) {
      featureList.add("FivegramClassFeatures");
      System.err.println("-> 5-gram class features added!");
    }
    String charNgramParam = InputOutputUtils.getCharNgramFeatures(params);
    if (charNgramParam.equalsIgnoreCase("yes")) {
      featureList.add("CharNgramFeatures");
      System.err.println("-> CharNgram features added!: Range " + minLength + ":" + maxLength);
    }
    String dictionaryParam = InputOutputUtils.getDictionaryFeatures(params);
    if (!dictionaryParam.equalsIgnoreCase(FixedTrainer.DEFAULT_FEATURE_FLAG)) {
      featureList.add("DictionaryFeatures");
      System.err.println("-> Dictionary features added!");
    }
    String brownParam = InputOutputUtils.getBrownFeatures(params);
    if (!brownParam.equalsIgnoreCase(FixedTrainer.DEFAULT_FEATURE_FLAG)) {
      featureList.add("BrownClusterFeatures");
      System.err.println("-> Brown clusters features added!");
      
    }
    String clarkParam = InputOutputUtils.getClarkFeatures(params);
    if (!clarkParam.equalsIgnoreCase(FixedTrainer.DEFAULT_FEATURE_FLAG)) {
      featureList.add("ClarkClusterFeatures");
      System.err.println("-> Clark clusters features added!");
    }
    String word2vecClusterParam = InputOutputUtils.getWord2VecClusterFeatures(params);
    if (!word2vecClusterParam.equalsIgnoreCase(FixedTrainer.DEFAULT_FEATURE_FLAG)) {
      featureList.add("Word2VecClusterFeatures");
      System.err.println("-> Word2vec clusters features added!");
    }
    return featureList;
  }

  /**
   * Adds the dictionary features of the curren token in a given window.
   * @param leftWindow the left window
   * @param rightWindow the right window
   * @param featureList the feature list
   */
  private static void addDictionaryFeatures(int leftWindow, int rightWindow,
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
  private static void addBrownFeatures(int leftWindow, int rightWindow, final List<AdaptiveFeatureGenerator> featureList) {
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
  private static void addClarkFeatures(int leftWindow, int rightWindow, final List<AdaptiveFeatureGenerator> featureList) {
    clarkLexicon = clarkCluster.getIgnoreCaseDictionary();
    featureList.add(new WindowFeatureGenerator(new ClarkFeatureGenerator(clarkLexicon), leftWindow, rightWindow));
  }
  
  /**
   * Add the word2vec cluster class feature of the curren token in a given window.
   * @param leftWindow the left window
   * @param rightWindow the right window
   * @param featureList the feature list
   */
  private static void addWord2VecClusterFeatures(int leftWindow, int rightWindow, final List<AdaptiveFeatureGenerator> featureList) {
    word2vecClusterLexicon = word2vecCluster.getIgnoreCaseDictionary();
    featureList.add(new WindowFeatureGenerator(new Word2VecClusterFeatureGenerator(word2vecClusterLexicon), leftWindow, rightWindow));
  }

  /**
   * Add the word2vec cluster class feature of the curren token in a given window.
   * @param leftWindow the left window
   * @param rightWindow the right window
   * @param featureList the feature list
   */
  private static void addWord2VecClusterFeatures(final List<AdaptiveFeatureGenerator> featureList) {
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
