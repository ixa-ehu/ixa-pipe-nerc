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
import java.util.List;

/**
 * @author ragerri
 * 
 */
public class Annotate {

  private final static boolean DEBUG = false;
  private NERC nameFinder;
  private boolean POSTPROCESS; 
  private boolean DICTTAG;
  
  public Annotate(String cmdOption) {
    Models modelRetriever = new Models();
    InputStream nerModel = modelRetriever.getNERModel(cmdOption);
    NameFactory nameFactory = new NameFactory();
    nameFinder = new NERC(nerModel,nameFactory);
    POSTPROCESS = false;
  }
  
  public Annotate(String cmdOption, String gazetteerOption) {
    Models modelRetriever = new Models();
    InputStream nerModel = modelRetriever.getNERModel(cmdOption);
    NameFactory nameFactory = new NameFactory();
    nameFinder = new NERC(nerModel,nameFactory);
    if (gazetteerOption.equalsIgnoreCase("post")) { 
      POSTPROCESS = true;
    }
    if (gazetteerOption.equalsIgnoreCase("tag")) { 
      DICTTAG = true;
    }
    if (gazetteerOption.equalsIgnoreCase("post,tag") || gazetteerOption.equalsIgnoreCase("tag,post")) { 
      POSTPROCESS = true;
      DICTTAG = true;
    }
  }
  
  
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
      List<Name> dictNames = nameFinder.getNamesFromDictionaries(tokens, dictionaries);
      for (Name name : names) {
        Integer start_index = name.getSpan().getStart();
        Integer end_index = name.getSpan().getEnd();
        List<Term> nameTerms = kaf.getTermsFromWFs(Arrays.asList(Arrays
            .copyOfRange(tokenIds, start_index, end_index)));
        List<List<Term>> references = new ArrayList<List<Term>>();
        references.add(nameTerms);
        // NE type
        String type;
        if (POSTPROCESS) {
          type = nameFinder.gazetteerPostProcessing(name,dictionaries);
        }
        else { 
          type = name.getType();
        }
        kaf.createEntity(type, references);
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
      //Span[] nameSpans = nameFinder.nercToSpans(tokens);
      //Span[] reducedSpans = NameFinderME.dropOverlappingSpans(nameSpans);
      //Span[] reducedList = NameFinderME.dropOverlappingSpans(nameSpans);
      
      // gazetteers
      //TODO what about using the HUGE Google corpus for NER based on frecuencies?
     // List<Span> gazetteerSpans = getNEsFromDictionaries(sentence, dictionaries);

      // concatenate both and check for overlaps
      //concatenateSpans(gazetteerSpans, reducedSpans);
      //Span[] allSpans = gazetteerSpans.toArray(new Span[gazetteerSpans.size()]);
      //Span[] reducedList = NameFinderME.dropOverlappingSpans(allSpans);

      /*for (int i = 0; i < reducedList.length; i++) {
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
        //String type = gazetteerPostProcessing(reducedList[i],tokens,dictionaries);
        //kaf.createEntity(type, references);
      }
    */}
  }
  
  
}
  