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

import opennlp.tools.namefind.NameFinderME;
import opennlp.tools.util.Span;

/**
 * @author ragerri
 * 
 */
public class Annotate {

  private final static boolean DEBUG = false;
  private Dictionaries dictionaries;
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
    dictionaries = new Dictionaries(cmdOption);
    Models modelRetriever = new Models();
    InputStream nerModel = modelRetriever.getNERModel(cmdOption);
    NameFactory nameFactory = new NameFactory();
    nameFinder = new NERC(nerModel,nameFactory);
    if (gazetteerOption.equalsIgnoreCase("post")) { 
      POSTPROCESS = true;
    }
    if (gazetteerOption.equalsIgnoreCase("tag")) { 
      System.err.println("The tag option requires the post option: tag,post!!");
      System.exit(1);
    }
    if (gazetteerOption.equalsIgnoreCase("post,tag") || gazetteerOption.equalsIgnoreCase("tag,post")) { 
      POSTPROCESS = true;
      DICTTAG = true;
    }
  }
  
  public void annotateNEsToKAF(KAFDocument kaf)
      throws IOException {

    List<Span> probSpans = new ArrayList<Span>();
    List<Name> names = new ArrayList<Name>();
    List<List<WF>> sentences = kaf.getSentences();
    for (List<WF> sentence : sentences) {
      String[] tokens = new String[sentence.size()];
      String[] tokenIds = new String[sentence.size()];
      for (int i = 0; i < sentence.size(); i++) {
        tokens[i] = sentence.get(i).getForm();
        tokenIds[i] = sentence.get(i).getId();
      }
      probSpans = nameFinder.nercToSpans(tokens);
      if (DICTTAG) {
        //TODO what about using the HUGE Google corpus for NER based on frecuencies?
        List<Span> dictSpans = nameFinder.nerFromDictToSpans(tokens, dictionaries);
        nameFinder.concatenateSpans(probSpans, dictSpans);
      }
      Span[] allSpans = NameFinderME.dropOverlappingSpans(probSpans.toArray(new Span[probSpans.size()]));
      nameFinder.getNamesFromSpans(names, allSpans, tokens);
      for (Name name : names) {
        Integer start_index = name.getSpan().getStart();
        Integer end_index = name.getSpan().getEnd();
        List<Term> nameTerms = kaf.getTermsFromWFs(Arrays.asList(Arrays
            .copyOfRange(tokenIds, start_index, end_index)));
        List<List<Term>> references = new ArrayList<List<Term>>();
        references.add(nameTerms);
        String neType;
        if (POSTPROCESS) {
          neType = nameFinder.gazetteerPostProcessing(name,dictionaries);
        }
        else { 
          neType = name.getType();
        }
        kaf.createEntity(neType, references);
      }
    }
  }
  
}
  