package es.ehu.si.ixa.pipe.nerc.features;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import opennlp.tools.util.TrainingParameters;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;

import es.ehu.si.ixa.pipe.nerc.dict.Dictionaries;
import es.ehu.si.ixa.pipe.nerc.train.FixedTrainer;
import es.ehu.si.ixa.pipe.nerc.train.Flags;

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
  public static Dictionaries dictionaries;
  /**
   * The prefix to be used in the {@link DictionaryFeatureGenerator}.
   */
  public static String prefix;
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
    if (FixedTrainer.isTokenFeature(params)) {
      setWindow(params);
      Element tokenFeature = new Element("custom");
      tokenFeature.setAttribute("class", TokenFeatureGenerator.class.getName());
      Element tokenWindow = new Element("window");
      tokenWindow.setAttribute("prevLength", Integer.toString(leftWindow));
      tokenWindow.setAttribute("nextLength", Integer.toString(rightWindow));
      tokenWindow.addContent(tokenFeature);
      generators.addContent(tokenWindow);
      System.err.println("-> Token features added!: Window range " + leftWindow + ":" + rightWindow);
    }
    if (FixedTrainer.isTokenClassFeature(params)) {
      setWindow(params);
      Element tokenClassFeature = new Element("custom");
      tokenClassFeature.setAttribute("class", TokenClassFeatureGenerator.class.getName());
      Element tokenClassWindow = new Element("window");
      tokenClassWindow.setAttribute("prevLength", Integer.toString(leftWindow));
      tokenClassWindow.setAttribute("nextLength", Integer.toString(rightWindow));
      tokenClassWindow.addContent(tokenClassFeature);
      generators.addContent(tokenClassWindow);
      System.err.println("-> Token Class Features added!: Window range " + leftWindow + ":" + rightWindow);
    }
    if (FixedTrainer.isOutcomePriorFeature(params)) {
      Element outcomePriorFeature = new Element("custom");
      outcomePriorFeature.setAttribute("class", OutcomePriorFeatureGenerator.class.getName());
      generators.addContent(outcomePriorFeature);
      System.err.println("-> Outcome Prior Features added!");
    }
    if (FixedTrainer.isPreviousMapFeature(params)) {
      Element previousMapFeature = new Element("custom");
      previousMapFeature.setAttribute("class", PreviousMapFeatureGenerator.class.getName());
      generators.addContent(previousMapFeature);
      System.err.println("-> Previous Map Features added!");
    }
    if (FixedTrainer.isSentenceFeature(params)) {
      Element sentenceFeature = new Element("custom");
      sentenceFeature.setAttribute("class", SentenceFeatureGenerator.class.getName());
      sentenceFeature.setAttribute("begin", "true");
      sentenceFeature.setAttribute("end", "false");
      generators.addContent(sentenceFeature);
      System.err.println("-> Sentence Features added!");
    }
    if (FixedTrainer.isPrefixFeature(params)) {
      Element prefixFeature = new Element("custom");
      prefixFeature.setAttribute("class", Prefix34FeatureGenerator.class.getName());
      generators.addContent(prefixFeature);
      System.err.println("-> Prefix Features added!");
    }
    if (FixedTrainer.isSuffixFeature(params)) {
      Element suffixFeature = new Element("custom");
      suffixFeature.setAttribute("class", SuffixFeatureGenerator.class.getName());
      generators.addContent(suffixFeature);
      System.err.println("-> Suffix Features added!");
    }
    if (FixedTrainer.isBigramClassFeature(params)) {
      Element bigramFeature = new Element("custom");
      bigramFeature.setAttribute("class", BigramClassFeatureGenerator.class.getName());
      generators.addContent(bigramFeature);
      System.err.println("-> Bigram Class Features added!");
    }
    if (FixedTrainer.isTrigramClassFeature(params)) {
      Element trigramFeature = new Element("custom");
      trigramFeature.setAttribute("class", TrigramClassFeatureGenerator.class.getName());
      generators.addContent(trigramFeature);
      System.err.println("-> Trigram Class Features added!");
    }
    if (FixedTrainer.isFourgramClassFeature(params)) {
      Element fourgramFeature = new Element("custom");
      fourgramFeature.setAttribute("class", FourgramClassFeatureGenerator.class.getName());
      generators.addContent(fourgramFeature);
      System.err.println("-> Fourgram Class Features added!");
    }
    if (FixedTrainer.isFivegramClassFeature(params)) {
      Element fivegramFeature = new Element("custom");
      fivegramFeature.setAttribute("class", FivegramClassFeatureGenerator.class.getName());
      generators.addContent(fivegramFeature);
      System.err.println("-> Fivegram Class Features added!");
    }
    if (FixedTrainer.isCharNgramClassFeature(params)) {
      Element charngramFeature = new Element("custom");
      charngramFeature.setAttribute("class", CharacterNgramFeatureGenerator.class.getName());
      generators.addContent(charngramFeature);
      System.err.println("-> CharNgram Class Features added!");
    }
    if (FixedTrainer.isDictionaryFeatures(params)) {
      setWindow(params);
      String dictPath = Flags.getDictionaryFeatures(params);
      if (dictionaries == null) {
        dictionaries = new Dictionaries(dictPath);
      }
      for (int i = 0; i < dictionaries.getIgnoreCaseDictionaries().size(); i++) {
        prefix = dictionaries.getDictNames().get(i);
        Element dictFeatures = new Element("custom");
        dictFeatures.setAttribute("class", DictionaryFeatureGenerator.class.getName());
        dictFeatures.setAttribute("prefix", dictionaries.getDictNames().get(i));
        Element dictWindow = new Element("window");
        dictWindow.setAttribute("prevLength", Integer.toString(leftWindow));
        dictWindow.setAttribute("nextLength", Integer.toString(rightWindow));
        dictWindow.addContent(dictFeatures);
        generators.addContent(dictWindow);
      }
      System.err.println("-> Dictionary Features added!");
    }
    
    if (FixedTrainer.isBrownFeatures(params)) {
      String brownClusterPath = Flags.getBrownFeatures(params);
      setWindow(params);
      //previous 2 maps features
      Element prev2MapFeature = new Element("custom");
      prev2MapFeature.setAttribute("class", Prev2MapFeatureGenerator.class.getName());
      generators.addContent(prev2MapFeature);
      //previous map and token feature (in window)
      Element prevMapTokenFeature = new Element("custom");
      prevMapTokenFeature.setAttribute("class", PreviousMapTokenFeatureGenerator.class.getName());
      Element prevMapTokenWindow = new Element("window");
      prevMapTokenWindow.setAttribute("prevLength", Integer.toString(leftWindow));
      prevMapTokenWindow.setAttribute("nextLength", Integer.toString(rightWindow));
      prevMapTokenWindow.addContent(prevMapTokenFeature);
      generators.addContent(prevMapTokenWindow);
      //brown bigram class features
      Element brownBigramFeatures = new Element("custom");
      brownBigramFeatures.setAttribute("class", BrownBigramFeatureGenerator.class.getName());
      brownBigramFeatures.setAttribute("dict", brownClusterPath);
      generators.addContent(brownBigramFeatures);
      //brown token feature
      Element brownTokenFeature = new Element("custom");
      brownTokenFeature.setAttribute("class", BrownTokenFeatureGenerator.class.getName());
      brownTokenFeature.setAttribute("dict", brownClusterPath);
      Element brownTokenWindow = new Element("window");
      brownTokenWindow.setAttribute("prevLength", Integer.toString(leftWindow));
      brownTokenWindow.setAttribute("nextLength", Integer.toString(rightWindow));
      brownTokenWindow.addContent(brownTokenFeature);
      generators.addContent(brownTokenWindow);
      //brown token class feature
      Element brownTokenClassFeature = new Element("custom");
      brownTokenClassFeature.setAttribute("class", BrownTokenClassFeatureGenerator.class.getName());
      brownTokenClassFeature.setAttribute("dict", brownClusterPath);
      Element brownTokenClassWindow = new Element("window");
      brownTokenClassWindow.setAttribute("prevLength", Integer.toString(leftWindow));
      brownTokenClassWindow.setAttribute("nextLength", Integer.toString(rightWindow));
      brownTokenClassWindow.addContent(brownTokenClassFeature);
      generators.addContent(brownTokenClassWindow);
      System.err.println("-> Brown cluster features added!");
      
    }
    
    if (FixedTrainer.isClarkFeatures(params)) {
      String clarkClusterPath = Flags.getClarkFeatures(params);
      setWindow(params);
      Element clarkFeatures = new Element("custom");
      clarkFeatures.setAttribute("class", ClarkFeatureGenerator.class.getName());
      clarkFeatures.setAttribute("dict", clarkClusterPath);
      Element clarkWindow = new Element("window");
      clarkWindow.setAttribute("prevLength", Integer.toString(leftWindow));
      clarkWindow.setAttribute("nextLength", Integer.toString(rightWindow));
      clarkWindow.addContent(clarkFeatures);
      generators.addContent(clarkWindow);
      System.err.println("-> Clark cluster features added!");
    }
    
    if (FixedTrainer.isWord2VecClusterFeatures(params)) {
      String word2vecClusterPath = Flags.getWord2VecClusterFeatures(params);
      setWindow(params);
      Element word2vecClusterFeatures = new Element("custom");
      word2vecClusterFeatures.setAttribute("class", Word2VecClusterFeatureGenerator.class.getName());
      word2vecClusterFeatures.setAttribute("dict", word2vecClusterPath);
      Element word2vecClusterWindow = new Element("window");
      word2vecClusterWindow.setAttribute("prevLength", Integer.toString(leftWindow));
      word2vecClusterWindow.setAttribute("nextLength", Integer.toString(rightWindow));
      word2vecClusterWindow.addContent(word2vecClusterFeatures);
      generators.addContent(word2vecClusterWindow);
      System.err.println("-> Word2Vec clusters features added!");
    }
    
    aggGenerators.addContent(cached);
    cached.addContent(generators);
    
    XMLOutputter xmlOutput = new XMLOutputter();
    Format format = Format.getPrettyFormat();
    xmlOutput.setFormat(format);
    return xmlOutput.outputString(doc);
    
  }
  
  
  /**
   * @param params
   */
  public static void setWindow(TrainingParameters params) {
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
    String windowParam = Flags.getWindow(params);
    String[] windowArray = windowParam.split("[ :-]");
    if (windowArray.length == 2) {
      windowRange.add(Integer.parseInt(windowArray[0]));
      windowRange.add(Integer.parseInt(windowArray[1]));
    }
    return windowRange;
  }
  
  public static void setNgramRange(TrainingParameters params) {
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
      String charNgramParam = Flags.getCharNgramFeaturesRange(params);
      String[] charngramArray = charNgramParam.split("[ :-]");
      if (charngramArray.length == 2) {
        ngramRange.add(Integer.parseInt(charngramArray[0]));
        ngramRange.add(Integer.parseInt(charngramArray[1]));

      }
    return ngramRange;
  }
  
 
}
