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

package es.ehu.si.ixa.pipe.nerc;

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
  
  public Annotate(String lang, String model, String features,int beamsize) {
    NameFactory nameFactory = new NameFactory();
    nameFinder = new StatisticalNameFinder(lang,nameFactory,model,features,beamsize);
    STATISTICAL = true;
  }
  
  public Annotate(String lang, String gazetteerOption,String model,String features,int beamsize) {
    NameFactory nameFactory = new NameFactory();
    nameFinder = new StatisticalNameFinder(lang,nameFactory,model,features,beamsize);
    perDictFinder = createDictNameFinder("en/en-wiki-person.txt","PERSON",nameFactory);
    orgDictFinder = createDictNameFinder("en/en-wiki-organization.txt","ORGANIZATION",nameFactory);
    locDictFinder = createDictNameFinder("en/en-wiki-location.txt","LOCATION",nameFactory);
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
        //TODO clearAdaptiveFeatures; evaluate
        allSpans = nameFinder.nercToSpans(tokens);
      }
      if (POSTPROCESS) {
        List<Span> perDictSpans = perDictFinder.nercToSpansExact(tokens);
        List<Span> orgDictSpans = orgDictFinder.nercToSpansExact(tokens);
        List<Span> locDictSpans = locDictFinder.nercToSpansExact(tokens);
        perDictFinder.concatenateSpans(perDictSpans,orgDictSpans);
        perDictFinder.concatenateSpans(perDictSpans,locDictSpans);
        perDictFinder.postProcessDuplicatedSpans(allSpans, perDictSpans);
        perDictFinder.concatenateSpans(allSpans, perDictSpans);
      }
      if (DICTTAG) {
        allSpans = perDictFinder.nercToSpansExact(tokens);
        List<Span> orgDictSpans = orgDictFinder.nercToSpansExact(tokens);
        List<Span> locDictSpans = locDictFinder.nercToSpansExact(tokens);
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
  
