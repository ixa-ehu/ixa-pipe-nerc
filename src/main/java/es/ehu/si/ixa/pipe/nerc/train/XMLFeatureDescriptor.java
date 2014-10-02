package es.ehu.si.ixa.pipe.nerc.train;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import opennlp.tools.util.TrainingParameters;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;

public class XMLFeatureDescriptor {


  public static final String DEFAULT_FEATURE_FLAG = "no";
  public static final String CHAR_NGRAM_RANGE = "2:5";
  public static final String DEFAULT_WINDOW = "2:2";
  private static int leftWindow = -1;
  private static int rightWindow = -1;
  
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
      Element tokenFeature = new Element("token");
      Element tokenWindow = new Element("window");
      tokenWindow.setAttribute("prevLength", Integer.toString(leftWindow));
      tokenWindow.setAttribute("nextLength", Integer.toString(rightWindow));
      tokenWindow.addContent(tokenFeature);
      generators.addContent(tokenWindow);
      System.err.println("-> Token features added!: Window range " + leftWindow + ":" + rightWindow);
    }
    if (isTokenClassFeature(params)) {
      Element tokenClassFeature = new Element("tokenclass");
      Element tokenClassWindow = new Element("window");
      tokenClassWindow.setAttribute("prevLength", Integer.toString(leftWindow));
      tokenClassWindow.setAttribute("nextLength", Integer.toString(rightWindow));
      tokenClassWindow.addContent(tokenClassFeature);
      generators.addContent(tokenClassWindow);
      System.err.println("-> Token Class Features added!: Window range " + leftWindow + ":" + rightWindow);
    }
    if (isOutcomePriorFeature(params)) {
      Element outcomePriorFeature = new Element("definition");
      generators.addContent(outcomePriorFeature);
      System.err.println("-> Outcome Prior Features added!");
    }
    if (isPreviousMapFeature(params)) {
      Element previousMapFeature = new Element("prevmap");
      generators.addContent(previousMapFeature);
      System.err.println("-> Previous Map Features added!");
    }
    if (isSentenceFeature(params)) {
      Element sentenceFeature = new Element("sentence");
      sentenceFeature.setAttribute("begin", "true");
      sentenceFeature.setAttribute("end", "false");
      generators.addContent(sentenceFeature);
      System.err.println("-> Sentence Features added!");
    }
    /*if (isPrefixFeature(params)) {
      Element prefixFeature = new Element("prefix");
      generators.addContent(prefixFeature);
      System.err.println("-> Prefix Features added!");
    }
    if (isSuffixFeature(params)) {
      Element prefixFeature = new Element("suffix");
      generators.addContent(prefixFeature);
      System.err.println("-> Suffix Features added!");
    }*/
    if (isBigramClassFeature(params)) {
      Element bigramFeature = new Element("bigram");
      generators.addContent(bigramFeature);
      System.err.println("-> Bigram Class Features added!");
    }
    
    aggGenerators.addContent(cached);
    cached.addContent(generators);
    
    XMLOutputter xmlOutput = new XMLOutputter();
    Format format = Format.getPrettyFormat();
    //format.setOmitDeclaration(true);
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
