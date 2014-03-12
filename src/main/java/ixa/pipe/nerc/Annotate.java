/*
 *  Copyright 2013 Rodrigo Agerri

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

import ixa.kaflib.KAFDocument;
import ixa.kaflib.Term;
import ixa.kaflib.WF;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import opennlp.tools.namefind.NameFinderME;
import opennlp.tools.util.Span;

/**
 * @author ragerri
 * 
 */
public class Annotate {

  private final static boolean DEBUG = false;
  private NERC nameFinder;

  public Annotate(String cmdOption) {
    Models modelRetriever = new Models();
    InputStream nerModel = modelRetriever.getNERModel(cmdOption);
    nameFinder = new NERC(nerModel);
  }
  
  /**
   * 
   * It takes a NE span indexes and the tokens in a sentence and produces the
   * string to which the NE span corresponds to. This function is used to get
   * the NE textual representation from a Span.
   * 
   * @param Span
   *          reducedSpan
   * @param String
   *          [] tokens
   * @return named entity string
   */
  private String getSentenceFromWFs(List<WF> sentence) {
    StringBuilder sb = new StringBuilder();
    for (WF wf : sentence) {
      sb.append(wf.getForm()).append(" ");
    }
    return sb.toString().trim();
  }

  private static List<Integer> findName(String sentence, String name) {
    
    List<Integer> neChars = new ArrayList<Integer>();
    int foundChar = sentence.indexOf(name);
    if (foundChar != -1) {
      System.out.println(foundChar);
      for (int i = foundChar + 1; i < foundChar - sentence.length(); i++) {
        System.out.println(i);
        int found = sentence.indexOf(name, i);
        if (found != -1) { 
          //System.err.println(found);
        }
      }
    }
    return neChars;
  }
  
  private List<Span> getNEsFromDictionaries(List<WF> wfList, Dictionaries dictionaries) {
    String sentence = getSentenceFromWFs(wfList).toLowerCase();
    if (DEBUG) { 
      System.err.println("Sentence: " + sentence);
    }
    List<Span> neSpans = new ArrayList<Span>();
    Iterator<String> dictIterator = dictionaries.person.iterator();
    while (dictIterator.hasNext()) {
      String name = (String) dictIterator.next();
      List<Integer> ne = findName(sentence,name);
      if (sentence.matches(name)) {
        String[] neTokens = name.split(" ");
        for (WF wf: wfList) { 
          if (neTokens[0].equals(wf.getForm().toLowerCase())) { 
            int startId = Integer.parseInt(wf.getId().substring(1)) - 1;
            int endId = startId + neTokens.length;
            Span neSpan = new Span(startId, endId, "PERSON");
            neSpans.add(neSpan);
          }
        }
      }
    }
    return neSpans;
  }

  
  /**
   * This method tags Named Entities.
   * 
   * It also reads <wf>, <terms> elements from the input KAF document and fills
   * the KAF object with those elements plus the annotated Named Entities.
   * 
   * @param KAFDocument
   *          kaf
   * @return KAF document containing <wf>,<terms> and <entities> elements.
   * 
   */

  public void annotateNEsToKAF(KAFDocument kaf, Dictionaries dictionaries)
      throws IOException {

    List<List<WF>> sentences = kaf.getSentences();
    for (List<WF> sentence : sentences) {
      // get array of token forms from a list of WF objects
      String[] tokens = new String[sentence.size()];
      String[] tokenIds = new String[sentence.size()];
      for (int i = 0; i < sentence.size(); i++) {
        tokens[i] = sentence.get(i).getForm();
        tokenIds[i] = sentence.get(i).getId();
      }
      List<Name> names = nameFinder.getNames(tokens);
          
      for (Name name : names) {
        Integer start_index = name.getSpan().getStart();
        Integer end_index = name.getSpan().getEnd();
        
        List<Term> nameTerms = kaf.getTermsFromWFs(Arrays.asList(Arrays
            .copyOfRange(tokenIds, start_index, end_index)));
        List<List<Term>> references = new ArrayList<List<Term>>();
        references.add(nameTerms);
        
        kaf.createEntity(name.getType(), references);
      }
    }
  }

  
  /**
   * This method tags Named Entities.
   * 
   * It also reads <wf>, <terms> elements from the input KAF document and fills
   * the KAF object with those elements plus the annotated Named Entities.
   * 
   * @param KAFDocument
   *          kaf
   * @return KAF document containing <wf>,<terms> and <entities> elements.
   * 
   */

  public void annotat(KAFDocument kaf, Dictionaries dictionaries)
      throws IOException {

    List<List<WF>> sentences = kaf.getSentences();
    for (List<WF> sentence : sentences) {
      // get array of token forms from a list of WF objects
      String[] tokens = new String[sentence.size()];
      String[] tokenIds = new String[sentence.size()];
      for (int i = 0; i < sentence.size(); i++) {
        tokens[i] = sentence.get(i).getForm();
        tokenIds[i] = sentence.get(i).getId();
      }
      // probabilistic
      Span[] nameSpans = nameFinder.nercToSpans(tokens);
      //Span[] reducedSpans = NameFinderME.dropOverlappingSpans(nameSpans);
      Span[] reducedList = NameFinderME.dropOverlappingSpans(nameSpans);
      
      // gazetteers
      //TODO what about using the HUGE Google corpus for NER based on frecuencies?
     // List<Span> gazetteerSpans = getNEsFromDictionaries(sentence, dictionaries);

      // concatenate both and check for overlaps
      //concatenateSpans(gazetteerSpans, reducedSpans);
      //Span[] allSpans = gazetteerSpans.toArray(new Span[gazetteerSpans.size()]);
      //Span[] reducedList = NameFinderME.dropOverlappingSpans(allSpans);

      for (int i = 0; i < reducedList.length; i++) {
        if (DEBUG) {
          System.err.println("1 " + reducedList[i].toString());
        }
        Integer start_index = reducedList[i].getStart();
        Integer end_index = reducedList[i].getEnd();
        List<Term> nameTerms = kaf.getTermsFromWFs(Arrays.asList(Arrays
            .copyOfRange(tokenIds, start_index, end_index)));
        List<List<Term>> references = new ArrayList<List<Term>>();
        references.add(nameTerms);
        
         
        // postProcessing: change NE tag if in non ambiguous gazetteers
        String type = gazetteerPostProcessing(reducedList[i],tokens,dictionaries);
        kaf.createEntity(type, references);
      }
    }
  }
  
  private String gazetteerPostProcessing(Span neSpan, String[] sentTokens, Dictionaries dictionaries) { 
    String type;
    String neString = nameFinder.getStringFromSpan(neSpan,sentTokens).toLowerCase();
    if (dictionaries.person.contains(neString)) {
      if (DEBUG) {
        System.err.println(neString + " post-processed into " + "PERSON");
      }
      type = "PERSON";
    } else {
      type = neSpan.getType().toUpperCase();
    }
    return type; 
  }
}
  