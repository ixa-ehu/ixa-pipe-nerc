package es.ehu.si.ixa.pipe.nerc.features;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import opennlp.tools.util.TrainingParameters;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;

import es.ehu.si.ixa.pipe.nerc.dict.BrownCluster;
import es.ehu.si.ixa.pipe.nerc.dict.ClarkCluster;
import es.ehu.si.ixa.pipe.nerc.dict.Dictionaries;
import es.ehu.si.ixa.pipe.nerc.dict.Dictionary;
import es.ehu.si.ixa.pipe.nerc.dict.Word2VecCluster;
import es.ehu.si.ixa.pipe.nerc.train.InputOutputUtils;

public class XMLFeatureDescriptor {


  public static final String DEFAULT_FEATURE_FLAG = "no";
  public static final String CHAR_NGRAM_RANGE = "2:5";
  public static final String DEFAULT_WINDOW = "2:2";
  public static int leftWindow = -1;
  public static int rightWindow = -1;
  public static int minCharNgram = -1;
  public static int maxCharNgram = -1;
  /**
   * The {@link Dictionaries} contained in the given directory.
   */
  private static Dictionaries dictionaries;
  /**
   * A {@link Dictionary} object.
   */
  public static Dictionary dictionary;
  /**
   * The prefix to be used in the {@link DictionaryFeatureGenerator}.
   */
  public static String prefix;
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
   * This class is not to be instantiated.
   */
  private XMLFeatureDescriptor() {
  }
  
  public static String createXMLFeatureDescriptor(TrainingParameters params) throws IOException {
    
    Element aggGenerators = new Element("generators");
    Document doc = new Document(aggGenerators);
    
    //<generators>
    //  <cache>
    //    <generators>
    Element cached = new Element("cache");
    Element generators = new Element("generators");
    //<window prevLength="2" nextLength="2">
    //  <token />
    //</window>
    if (isTokenFeature(params)) {
      Element tokenFeature = new Element("custom");
      tokenFeature.setAttribute("class", TokenFeatureGenerator.class.getName());
      Element tokenWindow = new Element("window");
      tokenWindow.setAttribute("prevLength", Integer.toString(leftWindow));
      tokenWindow.setAttribute("nextLength", Integer.toString(rightWindow));
      tokenWindow.addContent(tokenFeature);
      generators.addContent(tokenWindow);
      System.err.println("-> Token features added!: Window range " + leftWindow + ":" + rightWindow);
    }
    if (isTokenClassFeature(params)) {
      Element tokenClassFeature = new Element("custom");
      tokenClassFeature.setAttribute("class", TokenClassFeatureGenerator.class.getName());
      Element tokenClassWindow = new Element("window");
      tokenClassWindow.setAttribute("prevLength", Integer.toString(leftWindow));
      tokenClassWindow.setAttribute("nextLength", Integer.toString(rightWindow));
      tokenClassWindow.addContent(tokenClassFeature);
      generators.addContent(tokenClassWindow);
      System.err.println("-> Token Class Features added!: Window range " + leftWindow + ":" + rightWindow);
    }
    if (isOutcomePriorFeature(params)) {
      Element outcomePriorFeature = new Element("custom");
      outcomePriorFeature.setAttribute("class", OutcomePriorFeatureGenerator.class.getName());
      generators.addContent(outcomePriorFeature);
      System.err.println("-> Outcome Prior Features added!");
    }
    if (isPreviousMapFeature(params)) {
      Element previousMapFeature = new Element("custom");
      previousMapFeature.setAttribute("class", PreviousMapFeatureGenerator.class.getName());
      generators.addContent(previousMapFeature);
      System.err.println("-> Previous Map Features added!");
    }
    if (isSentenceFeature(params)) {
      Element sentenceFeature = new Element("custom");
      sentenceFeature.setAttribute("class", SentenceFeatureGenerator.class.getName());
      sentenceFeature.setAttribute("begin", "true");
      sentenceFeature.setAttribute("end", "false");
      generators.addContent(sentenceFeature);
      System.err.println("-> Sentence Features added!");
    }
    if (isPrefixFeature(params)) {
      Element prefixFeature = new Element("custom");
      prefixFeature.setAttribute("class", Prefix34FeatureGenerator.class.getName());
      generators.addContent(prefixFeature);
      System.err.println("-> Prefix Features added!");
    }
    if (isSuffixFeature(params)) {
      Element suffixFeature = new Element("custom");
      suffixFeature.setAttribute("class", SuffixFeatureGenerator.class.getName());
      generators.addContent(suffixFeature);
      System.err.println("-> Suffix Features added!");
    }
    if (isBigramClassFeature(params)) {
      Element bigramFeature = new Element("custom");
      bigramFeature.setAttribute("class", BigramClassFeatureGenerator.class.getName());
      generators.addContent(bigramFeature);
      System.err.println("-> Bigram Class Features added!");
    }
    if (isTrigramClassFeature(params)) {
      Element trigramFeature = new Element("custom");
      trigramFeature.setAttribute("class", TrigramClassFeatureGenerator.class.getName());
      generators.addContent(trigramFeature);
      System.err.println("-> Trigram Class Features added!");
    }
    if (isFourgramClassFeature(params)) {
      Element fourgramFeature = new Element("custom");
      fourgramFeature.setAttribute("class", FourgramClassFeatureGenerator.class.getName());
      generators.addContent(fourgramFeature);
      System.err.println("-> Fourgram Class Features added!");
    }
    if (isFivegramClassFeature(params)) {
      Element fivegramFeature = new Element("custom");
      fivegramFeature.setAttribute("class", FivegramClassFeatureGenerator.class.getName());
      generators.addContent(fivegramFeature);
      System.err.println("-> Fivegram Class Features added!");
    }
    if (isCharNgramClassFeature(params)) {
      Element charngramFeature = new Element("custom");
      charngramFeature.setAttribute("class", CharacterNgramFeatureGenerator.class.getName());
      generators.addContent(charngramFeature);
      System.err.println("-> CharNgram Class Features added!");
    }
    if (isDictionaryFeatures(params)) {
      String dictPath = InputOutputUtils.getDictionaryFeatures(params);
      System.err.println("-> Loading dictionaries from " + dictPath);
      if (dictionaries == null) {
        dictionaries = new Dictionaries(dictPath);
      }
      for (int i = 0; i < dictionaries.getIgnoreCaseDictionaries().size(); i++) {
        prefix = dictionaries.getDictNames().get(i);
        dictionary = dictionaries.getIgnoreCaseDictionaries().get(i);
        Element dictFeatures = new Element("custom");
        dictFeatures.setAttribute("class", DictionaryFeatureGenerator.class.getName());
        dictFeatures.setAttribute("dict", prefix);
        Element dictWindow = new Element("window");
        dictWindow.setAttribute("prevLength", Integer.toString(leftWindow));
        dictWindow.setAttribute("nextLength", Integer.toString(rightWindow));
        dictWindow.addContent(dictFeatures);
        generators.addContent(dictWindow);
      }
      System.err.println("-> Dictionary Features added!");
    }
    
    aggGenerators.addContent(cached);
    cached.addContent(generators);
    
    XMLOutputter xmlOutput = new XMLOutputter();
    Format format = Format.getPrettyFormat();
    xmlOutput.setFormat(format);
    return xmlOutput.outputString(doc);
    
  }
  
  
  private static boolean isTokenFeature(TrainingParameters params) {
    setWindow(params);
    String tokenParam = InputOutputUtils.getTokenFeatures(params);
    return !tokenParam.equalsIgnoreCase(XMLFeatureDescriptor.DEFAULT_FEATURE_FLAG);
  }
  private static boolean isTokenClassFeature(TrainingParameters params) {
    setWindow(params);
    String tokenParam = InputOutputUtils.getTokenClassFeatures(params);
    return !tokenParam.equalsIgnoreCase(XMLFeatureDescriptor.DEFAULT_FEATURE_FLAG);
  }
  private static boolean isOutcomePriorFeature(TrainingParameters params) {
    String outcomePriorParam = InputOutputUtils.getOutcomePriorFeatures(params);
    return !outcomePriorParam.equalsIgnoreCase(XMLFeatureDescriptor.DEFAULT_FEATURE_FLAG);
  }
  private static boolean isPreviousMapFeature(TrainingParameters params) {
    String previousMapParam = InputOutputUtils.getPreviousMapFeatures(params);
    return !previousMapParam.equalsIgnoreCase(XMLFeatureDescriptor.DEFAULT_FEATURE_FLAG);
  }
  private static boolean isSentenceFeature(TrainingParameters params) {
    String sentenceParam = InputOutputUtils.getSentenceFeatures(params);
    return !sentenceParam.equalsIgnoreCase(XMLFeatureDescriptor.DEFAULT_FEATURE_FLAG);
  }
  private static boolean isPrefixFeature(TrainingParameters params) {
    String prefixParam = InputOutputUtils.getPreffixFeatures(params);
    return !prefixParam.equalsIgnoreCase(XMLFeatureDescriptor.DEFAULT_FEATURE_FLAG);
  }
  private static boolean isSuffixFeature(TrainingParameters params) {
    String suffixParam = InputOutputUtils.getSuffixFeatures(params);
    return !suffixParam.equalsIgnoreCase(XMLFeatureDescriptor.DEFAULT_FEATURE_FLAG);
  }
  private static boolean isBigramClassFeature(TrainingParameters params) {
    String bigramParam = InputOutputUtils.getBigramClassFeatures(params);
    return !bigramParam.equalsIgnoreCase(XMLFeatureDescriptor.DEFAULT_FEATURE_FLAG);
  }
  private static boolean isTrigramClassFeature(TrainingParameters params) {
    String trigramParam = InputOutputUtils.getTrigramClassFeatures(params);
    return !trigramParam.equalsIgnoreCase(XMLFeatureDescriptor.DEFAULT_FEATURE_FLAG);
  }
  private static boolean isFourgramClassFeature(TrainingParameters params) {
    String fourgramParam = InputOutputUtils.getFourgramClassFeatures(params);
    return !fourgramParam.equalsIgnoreCase(XMLFeatureDescriptor.DEFAULT_FEATURE_FLAG);
  }
  private static boolean isFivegramClassFeature(TrainingParameters params) {
    String fivegramParam = InputOutputUtils.getFivegramClassFeatures(params);
    return !fivegramParam.equalsIgnoreCase(XMLFeatureDescriptor.DEFAULT_FEATURE_FLAG);
  }
  private static boolean isCharNgramClassFeature(TrainingParameters params) {
    setNgramRange(params);
    String charngramParam = InputOutputUtils.getCharNgramFeatures(params);
    return !charngramParam.equalsIgnoreCase(XMLFeatureDescriptor.DEFAULT_FEATURE_FLAG);
  }
  private static boolean isDictionaryFeatures(TrainingParameters params) {
    setWindow(params);
    String dictFeatures = InputOutputUtils.getDictionaryFeatures(params);
    return !dictFeatures.equalsIgnoreCase(DEFAULT_FEATURE_FLAG);
  }
  private static boolean isBrownFeatures(TrainingParameters params) {
    String brownFeatures = InputOutputUtils.getBrownFeatures(params);
    return !brownFeatures.equalsIgnoreCase(DEFAULT_FEATURE_FLAG);
  }
  private static boolean isClarkFeatures(TrainingParameters params) {
    String clarkFeatures = InputOutputUtils.getClarkFeatures(params);
    return !clarkFeatures.equalsIgnoreCase(DEFAULT_FEATURE_FLAG);
  }
  private static boolean isWordVecClusterFeatures(TrainingParameters params) {
    String word2vecClusterFeatures = InputOutputUtils.getWord2VecClusterFeatures(params);
    return !word2vecClusterFeatures.equalsIgnoreCase(DEFAULT_FEATURE_FLAG);
  }
  
  /**
   * @param params
   */
  private static void setWindow(TrainingParameters params) {
    if (leftWindow == -1 || rightWindow == -1) {
      leftWindow = getWindowRange(params).get(0);
      rightWindow = getWindowRange(params).get(1);
    }
  }
  
  /**
   * Get the window range feature.
   * @param params the training parameters
   * @return the list containing the left and right window values
   */
  private static List<Integer> getWindowRange(TrainingParameters params) {
    List<Integer> windowRange = new ArrayList<Integer>();
    String windowParam = InputOutputUtils.getWindow(params);
    String[] windowArray = windowParam.split("[ :-]");
    if (windowArray.length == 2) {
      windowRange.add(Integer.parseInt(windowArray[0]));
      windowRange.add(Integer.parseInt(windowArray[1]));
    }
    return windowRange;
  }
  
  private static void setNgramRange(TrainingParameters params) {
    if (minCharNgram == -1 || maxCharNgram == -1) {
      minCharNgram = getNgramRange(params).get(0);
      maxCharNgram = getNgramRange(params).get(1);
    }
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
