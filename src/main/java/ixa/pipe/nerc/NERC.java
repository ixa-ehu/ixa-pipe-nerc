/*
 *Copyright 2013 Rodrigo Agerri

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

package ixa.pipe.nerc;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import opennlp.tools.namefind.NameFinderME;
import opennlp.tools.namefind.TokenNameFinderModel;
import opennlp.tools.util.Span;

/**
 * Named Entity Recognition module based on Apache OpenNLP ML API and Gazetteers.  
 *  
 * @author ragerri 2012/10/30
 * 
 */

public class NERC {

  private TokenNameFinderModel nercModel;
  private NameFinderME nercDetector;
  private NameFactory nameFactory;
  private final static boolean DEBUG = false;

  /**
   * It constructs an object NERC from the NERC class. First it loads a model,
   * then it initializes the nercModel and finally it creates a nercDetector
   * using such model.
   */
  public NERC(InputStream trainedModel) {

    try {
      nercModel = new TokenNameFinderModel(trainedModel);

    } catch (IOException e) {
      e.printStackTrace();
    } finally {
      if (trainedModel != null) {
        try {
          trainedModel.close();
        } catch (IOException e) {
        }
      }
    }

    nercDetector = new NameFinderME(nercModel);
  }
  
  /**
   * It constructs an object NERC from the NERC class. First it loads a model,
   * then it initializes the nercModel and finally it creates a nercDetector
   * using such model. This constructor also uses a NameFactory to create {@link Name}
   * objects
   */
  public NERC(InputStream trainedModel, NameFactory nameFactory) {
    this.nameFactory = nameFactory;
    try {
      nercModel = new TokenNameFinderModel(trainedModel);

    } catch (IOException e) {
      e.printStackTrace();
    } finally {
      if (trainedModel != null) {
        try {
          trainedModel.close();
        } catch (IOException e) {
        }
      }
    }

    nercDetector = new NameFinderME(nercModel);
  }


  /**
   * This method receives as an input an array of Apache OpenNLP tokenized text
   * and calls the NameFinderME.find(tokens) to recognize and classify Named
   * Entities.
   * 
   * From Apache OpenNLP documentation: "After every document clearAdaptiveData
   * must be called to clear the adaptive data in the feature generators. Not
   * calling clearAdaptiveData can lead to a sharp drop in the detection rate
   * after a few documents."
   * 
   * @param tokens
   *          an array of tokenized text
   * @return an array of OpenNLP Spans of annotated text
   */
  private Span[] nercToSpans(String[] tokens) {
    Span[] annotatedText = nercDetector.find(tokens);
    nercDetector.clearAdaptiveData();
    return annotatedText;

  }
  
  /**
   * Probablistic Named Entity Classifier
   * Takes an array of tokens and returns a List of {@link Name} objects
   * containing the nameString, the type and the {@link Span} for easy
   * access to Named Entity attributes 
   * 
   * @param tokens an array of tokenized text
   * @return a List of names
   */
  public List<Name> getNames(String[] tokens) {
    List<Name> names = new ArrayList<Name>();
    Span[] origSpans = nercToSpans(tokens);
    Span[] neSpans = NameFinderME.dropOverlappingSpans(origSpans);
    for (Span neSpan : neSpans) {
      String nameString = getStringFromSpan(neSpan,tokens);
      String neType = neSpan.getType();
      Name name = nameFactory.createName(nameString, neType, neSpan);
      names.add(name);
    }
    return names;
  }
  
  /**
   * 
   * It takes a NE span indexes and the tokens in a sentence and produces the
   * string to which the NE span corresponds to. This function is used to get
   * the Named Entity or Name textual representation from a Span.
   * 
   * @param Span
   *          reducedSpan
   * @param String
   *          [] tokens
   * @return named entity string
   */
  public static String getStringFromSpan(Span reducedSpan, String[] tokens) {
    StringBuilder sb = new StringBuilder();
    for (int si = reducedSpan.getStart(); si < reducedSpan.getEnd(); si++) {
      sb.append(tokens[si]).append(" ");
    }
    return sb.toString().trim();
  }
  
  /**
   * Gazetteer based Named Entity Tagger
   * 
   * @param wfList
   * @param dictionaries
   * @return
   */
  public List<Name> getNamesFromDictionaries(String[] tokens, Dictionaries dictionaries) {
    String sentence = StringUtils.getSentenceFromTokens(tokens);
    List<Integer> neIds = StringUtils.bndmStringFinder("obama","I know that barack obama is in the white house");
    //List<Integer> neIds = StringUtils.exactStringFinder("obama","I know that barack obama is in the white house");
    for (Integer neId : neIds) { 
      System.err.println(neId);
    }
    List<Name> names = new ArrayList<Name>();
    Iterator<String> dictIterator = dictionaries.person.iterator();
    while (dictIterator.hasNext()) {
      String neDict = (String) dictIterator.next();
      //List<Integer> neIds = StringUtils.exactStringFinder(neDict, sentence);      
      
      if (sentence.matches(neDict)) {
        String[] neTokens = neDict.split(" ");
        for (int i = 0; i < tokens.length; i++) { 
          if (neTokens[0].equals(tokens[i])) { 
            int startId = i;
            int endId = startId + neTokens.length;
            Span neSpan = new Span(startId, endId, "PERSON");
            String nameString = getStringFromSpan(neSpan,tokens);
            String nameType = neSpan.getType();
            Name name = nameFactory.createName(nameString, nameType, neSpan);
            names.add(name);
          }
        }
      }
    }
    return names;
  }
 
  public static void concatenateSpans(List<Span> listSpans, Span[] probSpans) {
    for (Span span : probSpans) {
      listSpans.add(span);
    }
  }
  
  public String gazetteerPostProcessing(Name name, Dictionaries dictionaries) { 
    String type;
    String neString = name.value().toLowerCase();
    if (DEBUG) {
      System.err.println("Checking " + neString + " for post-processing ...");
    }
    if (!name.getType().equalsIgnoreCase("PERSON") && dictionaries.knownPerson.contains(neString)) {
      if (DEBUG) { 
        System.err.println(neString + " to PERSON!");
      }
      type = "PERSON";
    }
    else if (!name.getType().equalsIgnoreCase("PERSON") && dictionaries.person.contains(neString)) { 
      if (DEBUG) { 
        System.err.println(neString + " to WikiPERSON!");
      }
      type = "PERSON";
    }
    else if (!name.getType().equalsIgnoreCase("ORGANIZATION") && dictionaries.knownOrganization.contains(neString)) { 
      if (DEBUG) { 
        System.err.println(neString + " to ORGANIZATION!");
      }
      type = "ORGANIZATION";
    }
    else if (!name.getType().equalsIgnoreCase("ORGANIZATION") && dictionaries.organization.contains(neString)) { 
      if (DEBUG) { 
        System.err.println(neString + " to WikiORGANIZATION!");
      }
      type = "ORGANIZATION";
    }
    else if (!name.getType().equalsIgnoreCase("LOCATION") && dictionaries.knownLocation.contains(neString)) { 
      if (DEBUG) { 
        System.err.println(neString + " to LOCATION!");
      }
      type = "LOCATION";
    }
    else if (!name.getType().equalsIgnoreCase("LOCATION") && dictionaries.location.contains(neString)) { 
      if (DEBUG) { 
        System.err.println(neString + " to WikiLOCATION!");
      }
      type = "LOCATION";
    }
    else {
      type = name.getType();
    }
    return type; 
  }


}
