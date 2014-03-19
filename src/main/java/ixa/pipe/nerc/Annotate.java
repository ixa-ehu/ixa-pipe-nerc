/*
 *  Copyright 2014 Rodrigo Agerri

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
 
  private NameFinder nameFinder;
  private DictionaryNameFinder perDictFinder;
  private DictionaryNameFinder orgDictFinder;
  private DictionaryNameFinder locDictFinder;
  
  private boolean STATISTICAL;
  private boolean POSTPROCESS; 
  private boolean DICTTAG;
  
  public Annotate(String lang,String model) {
    Models modelRetriever = new Models();
    InputStream nerModel = modelRetriever.getNERModel(lang);
    NameFactory nameFactory = new NameFactory();
    nameFinder = new StatisticalNameFinder(nerModel,nameFactory,model);
    STATISTICAL = true;
  }
  
  public Annotate(String lang, String gazetteerOption, String model) {
    NameFactory nameFactory = new NameFactory();
    Models modelRetriever = new Models();
    InputStream nerModel = modelRetriever.getNERModel(lang);
    nameFinder = new StatisticalNameFinder(nerModel,nameFactory, model);
    perDictFinder = createDictNameFinder("en/wikiperson.txt","PERSON",nameFactory);
    orgDictFinder = createDictNameFinder("en/wikiorganization.txt","ORGANIZATION",nameFactory);
    locDictFinder = createDictNameFinder("en/wikilocation.txt","LOCATION",nameFactory);
    if (gazetteerOption.equalsIgnoreCase("post")) { 
      POSTPROCESS = true;
      STATISTICAL = true;
    }
    if (gazetteerOption.equalsIgnoreCase("tag")) {
      DICTTAG = true;
      STATISTICAL = false;
      POSTPROCESS = false;
    }
  }
  
  /**
   * Classify Named Entities and write them to a {@link KAFDocument} 
   * using stastitical models, post-processing and/or dictionaries only. 
   * 
   * @param kaf
   * @throws IOException
   */
  public void annotateNEsToKAF(KAFDocument kaf)
      throws IOException {
    
    List<Span> allSpans = new ArrayList<Span>();
    List<List<WF>> sentences = kaf.getSentences();
    for (List<WF> sentence : sentences) {
      String[] tokens = new String[sentence.size()];
      String[] tokenIds = new String[sentence.size()];
      for (int i = 0; i < sentence.size(); i++) {
        tokens[i] = sentence.get(i).getForm();
        tokenIds[i] = sentence.get(i).getId();
      }
      if (STATISTICAL) {
        allSpans = nameFinder.nercToSpans(tokens);
      }
      if (POSTPROCESS) {
        List<Span> perDictSpans = perDictFinder.nercToSpans(tokens);
        List<Span> orgDictSpans = orgDictFinder.nercToSpans(tokens);
        List<Span> locDictSpans = locDictFinder.nercToSpans(tokens);
        perDictFinder.concatenateSpans(perDictSpans,orgDictSpans);
        perDictFinder.concatenateSpans(perDictSpans,locDictSpans);
        // TODO postprocessing
        List<Span> postSpans = perDictFinder.concatenateNoOverlappingSpans(allSpans, perDictSpans);
        allSpans.clear();
        perDictFinder.concatenateSpans(allSpans, postSpans);
      }
      if (DICTTAG) {
        allSpans = perDictFinder.nercToSpans(tokens);
        List<Span> orgDictSpans = orgDictFinder.nercToSpans(tokens);
        List<Span> locDictSpans = locDictFinder.nercToSpans(tokens);
        perDictFinder.concatenateSpans(allSpans,orgDictSpans);
        perDictFinder.concatenateSpans(allSpans,locDictSpans);
      }
      Span[] allSpansArray = NameFinderME.dropOverlappingSpans(allSpans.toArray(new Span[allSpans.size()]));
      List<Name> names = nameFinder.getNamesFromSpans(allSpansArray, tokens);
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
   * Construct a {@link DictionaryNameFinder} using a {@link Dictionary}, a NE type and a {@link NameFactory} to 
   * create {@link Name} objects
   * 
   * @param dictFile
   * @param type
   * @param nameFactory
   * @return an instance of a {@link DictionaryNameFinder}
   */
  public DictionaryNameFinder createDictNameFinder(String dictFile, String type, NameFactory nameFactory) { 
    InputStream dictStream = getClass().getResourceAsStream("/"+dictFile);
    Dictionary dict = new Dictionary(dictStream);
    DictionaryNameFinder dictNameFinder = new DictionaryNameFinder(dict,type,nameFactory);
    return dictNameFinder;
  }
  
}
  