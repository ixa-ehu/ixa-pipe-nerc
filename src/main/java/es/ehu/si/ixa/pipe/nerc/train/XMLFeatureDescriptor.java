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
    
    Element aggregatedGenerators = new Element("generators");
    Document doc = new Document(aggregatedGenerators);
    
    //<generators>
    //  <cache>
    //    <generators>
    Element cached = new Element("cache");
    doc.addContent(cached);
    Element generators = new Element("generators");
    doc.addContent(generators);
    
    //<window prevLength="2" nextLength="2">
    //  <token />
    //</window>
    if (isTokenFeature(params)) {
      Element tokenFeature = new Element("token");
      Element tokenWindow = new Element("window");
      tokenWindow.setAttribute("prevLength", Integer.toString(leftWindow));
      tokenWindow.setAttribute("nextLegth", Integer.toString(rightWindow));
      tokenWindow.addContent(tokenFeature);
      doc.addContent(tokenWindow);
      System.err.println("-> Token features added!: Window range " + leftWindow + ":" + rightWindow);
    }
    if (isTokenClassFeature(params)) {
      Element tokenClassFeature = new Element("tokenclass");
      Element tokenClassWindow = new Element("window");
      tokenClassWindow.setAttribute("prevLength", Integer.toString(leftWindow));
      tokenClassWindow.setAttribute("nextLegth", Integer.toString(rightWindow));
      tokenClassWindow.addContent(tokenClassFeature);
      doc.addContent(tokenClassWindow);
      System.err.println("-> Token Class Features added!: Window range " + leftWindow + ":" + rightWindow);
    }
    if (isOutcomePriorFeature(params)) {
      Element outcomePriorFeature = new Element("outcomeprior");
      doc.addContent(outcomePriorFeature);
      System.err.println("-> Outcome Prior Features added!");
    }
    if (isPreviousMapFeature(params)) {
      Element previousMapFeature = new Element("previousmap");
      Element previousMapWindow = new Element("window");
      previousMapWindow.setAttribute("prevLength", Integer.toString(leftWindow));
      previousMapWindow.setAttribute("nextLegth", Integer.toString(rightWindow));
      previousMapWindow.addContent(previousMapFeature);
      doc.addContent(previousMapWindow);
      System.err.println("-> Previous Map Features added!: Window range " + leftWindow + ":" + rightWindow);
    }
    if (isSentenceFeature(params)) {
      Element sentenceFeature = new Element("sentence");
      sentenceFeature.setAttribute("begin", "true");
      sentenceFeature.setAttribute("end", "false");
      doc.addContent(sentenceFeature);
      System.err.println("-> Sentence Features added!");
    }
    if (isPrefixFeature(params)) {
      Element prefixFeature = new Element("prefix");
      doc.addContent(prefixFeature);
      System.err.println("-> Prefix Features added!");
    }
    if (isSuffixFeature(params)) {
      Element prefixFeature = new Element("suffix");
      doc.addContent(prefixFeature);
      System.err.println("-> Suffix Features added!");
    }
    if (isBigramClassFeature(params)) {
      Element bigramFeature = new Element("bigram");
      doc.addContent(bigramFeature);
      System.err.println("-> Bigram Class Features added!");
    }
    
    XMLOutputter xmlOutput = new XMLOutputter();
    xmlOutput.setFormat(Format.getPrettyFormat());
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
    setWindow(params);
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
