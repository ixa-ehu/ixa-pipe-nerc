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
  private String getStringFromSpan(Span reducedSpan, String[] tokens) {
    StringBuilder sb = new StringBuilder();
    for (int si = reducedSpan.getStart(); si < reducedSpan.getEnd(); si++) {
      sb.append(tokens[si]).append(" ");
    }
    return sb.toString().trim();
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

  private List<Span> getNEsFromDictionaries(List<WF> wfList, Dictionaries dictionaries) {
    List<Span> neSpans = new ArrayList<Span>();
    Iterator<String> dictIterator = dictionaries.person.iterator();
    while (dictIterator.hasNext()) {
      String name = (String) dictIterator.next();
      for (WF wf : wfList) {
        if (name.contains(wf.getForm().toLowerCase())) {
          String[] neTokens = name.split(" ");
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

  private static void concatenateSpans(List<Span> listSpans, Span[] probSpans) {
    for (Span span : probSpans) {
      listSpans.add(span);
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
      // probabilistic
      Span[] nameSpans = nameFinder.nercAnnotate(tokens);
      //Span[] reducedSpans = NameFinderME.dropOverlappingSpans(nameSpans);
      Span[] reducedList = NameFinderME.dropOverlappingSpans(nameSpans);
      
      // gazeteers
      //List<Span> listSpans = getNEsFromDictionaries(sentence, dictionaries);

      // concatenate both and check for overlaps
      //concatenateSpans(listSpans, reducedSpans);
      //Span[] allSpans = listSpans.toArray(new Span[listSpans.size()]);
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

        String type;
        // postProcessing: change NE tag if in non ambiguous gazeteers
        String neString = getStringFromSpan(reducedList[i], tokens)
            .toLowerCase();
        if (dictionaries.person.contains(neString)) {
          if (DEBUG) {
            System.err.println(neString + " post-processed into " + "PERSON");
          }
          type = "PERSON";
        } else {
          type = reducedList[i].getType().toUpperCase();
        }
        kaf.createEntity(type, references);
      }
    }
  }
}
