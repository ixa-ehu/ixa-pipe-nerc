/*
 *Copyright 2014 Rodrigo Agerri

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
import java.util.Arrays;
import java.util.List;

import opennlp.tools.namefind.NameFinderME;
import opennlp.tools.namefind.TokenNameFinderModel;
import opennlp.tools.util.Span;

 /**
 * Named Entity Recognition module based on Apache OpenNLP ML API and Gazetteers.  
 *  
 * @author ragerri 2014/03/14
 * 
 */

 public class NERC {

   private TokenNameFinderModel nercModel;
   private NameFinderME nercDetector;
   private NameFactory nameFactory;
   private final static boolean DEBUG = true;

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
   * Probabilistic Named Entity Classifier
   * 
   * Takes an array of tokens, calls nercToSpans function 
   * for probabilistic NERC and returns a List of {@link Name} objects
   * containing the nameString, the type and the {@link Span}
   * 
   * @param tokens an array of tokenized text
   * @return a List of names
   */
  public List<Name> getNamesProb(String[] tokens) {
    List<Name> names = new ArrayList<Name>();
    List<Span> origSpans = nercToSpans(tokens);
    Span[] neSpans = NameFinderME.dropOverlappingSpans(origSpans.toArray(new Span[origSpans.size()]));
    getNamesFromSpans(names,neSpans,tokens);
    return names;
  }
  
  /**
   * This method receives as an input an array of Apache OpenNLP tokenized text
   * and calls the NameFinderME.find(tokens) to recognize and classify Named
   * Entities. It outputs the spans of the detected and classified Named Entities
   * 
   * From Apache OpenNLP documentation: "After every document clearAdaptiveData
   * must be called to clear the adaptive data in the feature generators. Not
   * calling clearAdaptiveData can lead to a sharp drop in the detection rate
   * after a few documents."
   * 
   * @param tokens
   *          an array of tokenized text
   * @return an list of OpenNLP Spans of annotated text
   */
  public List<Span> nercToSpans(String[] tokens) {
    Span[] annotatedText = nercDetector.find(tokens);
    nercDetector.clearAdaptiveData();
    List<Span> probSpans = new ArrayList<Span>(Arrays.asList(annotatedText));
    return probSpans;
  }
  
  /**
   * Populates a list of {@link Name} objects from spans and tokens
   * 
   * @param names
   * @param neSpans
   * @param tokens
   */
  public void getNamesFromSpans(List<Name> names, Span[] neSpans, String[]tokens) {
    for (Span neSpan : neSpans) { 
      String nameString = StringUtils.getStringFromSpan(neSpan,tokens);
      String neType = neSpan.getType();
      Name name = nameFactory.createName(nameString, neType, neSpan);
      names.add(name);
    }
  }
  
  /**
   * Gazetteer based Named Entity Detector. Note that this method
   * does not classify the named entities, only assigns a "MISC" tag to every
   * Named Entity. Pass the result of this function to {@link gazetteerPostProcessing} for
   * gazetteer-based Named Entity classification.
   * 
   * @param tokens
   * @param dictionaries
   * @return a list of detected names
   */
  public List<Name> getNamesDict(String[] tokens, Dictionaries dictionaries) {
    
    List<Name> names = new ArrayList<Name>();
    List<Span> origSpans = nerFromDictToSpans(tokens,dictionaries);
    Span[] neSpans = NameFinderME.dropOverlappingSpans(origSpans.toArray(new Span[origSpans.size()]));
    getNamesFromSpans(names,neSpans,tokens);
    return names;
  }
  
  /**
  * Detects Named Entities in a gazetteer
  * 
  * @param tokens
  * @param dictionaries
  * @return spans of the Named Entities
  */
  public List<Span> nerFromDictToSpans(String[] tokens, Dictionaries dictionaries) {
    List<Span> neSpans = new ArrayList<Span>();
    for (String neDict : dictionaries.all) {
      List<Integer> neIds = StringUtils.exactTokenFinder(neDict,tokens);
      /*for (Integer neId : neIds) {
        System.err.println(neId);
      }*/
      if (!neIds.isEmpty()) {
        Span neSpan = new Span(neIds.get(0), neIds.get(1), "MISC");
        neSpans.add(neSpan); 
      }
    }
    return neSpans;
  }
 
  /**
   * Concatenates two span lists adding the spans of the second parameter
   * to the list in first parameter
   * 
   * @param allSpans
   * @param neSpans
   */
  public void concatenateSpans(List<Span> allSpans, List<Span> neSpans) {
    for (Span span : neSpans) {
      allSpans.add(span);
    }
  }
  
  /**
   * It classifies Named Entities according to its presence in one or more 
   * gazetteers. 
   * 
   * @param name
   * @param dictionaries
   * @return new Named Entity type according to one or more gazetteers
   */
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
