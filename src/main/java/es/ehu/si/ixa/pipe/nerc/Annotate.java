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

  /**
   * The NameFinder to do the annotation.
   */
  private NameFinder nameFinder;
  /**
   * The dictionary name finders to do the post processing.
   */
  private DictionaryNameFinder perDictFinder;
  /**
   * The dictionary name finders to do the post processing.
   */
  private DictionaryNameFinder orgDictFinder;
  /**
   * The dictionary name finders to do the post processing.
   */
  private DictionaryNameFinder locDictFinder;
  /**
   * True if the name finder is statistical.
   */
  private boolean statitiscal;
  /**
   * Activates post processing of statistical name finder with dictionary
   * name finders.
   */
  private boolean postProcess;
  /**
   * Activates name finding using dictionaries only.
   */
  private boolean dictTag;

  /**
   * Construct a probabilistic annotator.
   *
   * @param lang the language
   * @param model the model
   * @param features the features
   * @param beamsize the beam size for decoding
   */
  public Annotate(final String lang, final String model, final String features,
      final int beamsize) {
    if (model.equalsIgnoreCase("baseline")) {
      System.err.println("No NERC model chosen, reverting to baseline model!");
    }
    NameFactory nameFactory = new NameFactory();
    nameFinder = new StatisticalNameFinder(lang, nameFactory, model, features, beamsize);
    statitiscal = true;
  }

  /**
   * Construct an annotator with options for post processing of probabilistic
   * annotation and tagging with dictionaries only.
   *
   * @param lang the language
   * @param dictOption whether dictionaries are used for tagging or post processing
   * @param model the model
   * @param features the features
   * @param beamsize the beam size for decoding
   */
  public Annotate(final String lang, final String dictOption, final String model,
      final String features, final int beamsize) {
    if (model.equalsIgnoreCase("baseline") && !dictOption.equalsIgnoreCase("tag")) {
      System.err.println("No NERC model chosen, reverting to baseline model!");
   }
    NameFactory nameFactory = new NameFactory();
    nameFinder = new StatisticalNameFinder(lang, nameFactory, model, features, beamsize);
    //TODO remove hard coding of these dictionaries
    perDictFinder = createDictNameFinder("en/en-wiki-person.txt", "PERSON", nameFactory);
    orgDictFinder = createDictNameFinder("en/en-wiki-organization.txt", "ORGANIZATION", nameFactory);
    locDictFinder = createDictNameFinder("en/en-wiki-location.txt", "LOCATION", nameFactory);
    if (dictOption.equalsIgnoreCase("post")) {
      postProcess = true;
      statitiscal = true;
    }
    if (dictOption.equalsIgnoreCase("tag")) {
      dictTag = true;
      statitiscal = false;
      postProcess = false;
    }
  }

  /**
   * Classify Named Entities and write them to a {@link KAFDocument}
   * using stastitical models, post-processing and/or dictionaries only.
   *
   * @param kaf the kaf document to be used for annotation
   * @throws IOException throws exception if problems with the kaf document
   */
  public final void annotateNEsToKAF(final KAFDocument kaf)
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
      if (statitiscal) {
        //TODO clearAdaptiveFeatures; evaluate
        allSpans = nameFinder.nercToSpans(tokens);
      }
      if (postProcess) {
        List<Span> perDictSpans = perDictFinder.nercToSpansExact(tokens);
        List<Span> orgDictSpans = orgDictFinder.nercToSpansExact(tokens);
        List<Span> locDictSpans = locDictFinder.nercToSpansExact(tokens);
        perDictFinder.concatenateSpans(perDictSpans, orgDictSpans);
        perDictFinder.concatenateSpans(perDictSpans, locDictSpans);
        perDictFinder.postProcessDuplicatedSpans(allSpans, perDictSpans);
        perDictFinder.concatenateSpans(allSpans, perDictSpans);
      }
      if (dictTag) {
        allSpans = perDictFinder.nercToSpansExact(tokens);
        List<Span> orgDictSpans = orgDictFinder.nercToSpansExact(tokens);
        List<Span> locDictSpans = locDictFinder.nercToSpansExact(tokens);
        perDictFinder.concatenateSpans(allSpans, orgDictSpans);
        perDictFinder.concatenateSpans(allSpans, locDictSpans);
      }
      Span[] allSpansArray = NameFinderME.dropOverlappingSpans(allSpans.toArray(new Span[allSpans.size()]));
      List<Name> names = nameFinder.getNamesFromSpans(allSpansArray, tokens);
      for (Name name : names) {
        Integer startIndex = name.getSpan().getStart();
        Integer endIndex = name.getSpan().getEnd();
        List<Term> nameTerms = kaf.getTermsFromWFs(Arrays.asList(Arrays
            .copyOfRange(tokenIds, startIndex, endIndex)));
        List<List<Term>> references = new ArrayList<List<Term>>();
        references.add(nameTerms);
        kaf.createEntity(name.getType(), references);
      }
    }
  }

  /**
   * Construct a {@link DictionaryNameFinder} using a {@link Dictionary},
   * a NE type and a {@link NameFactory} to create {@link Name} objects.
   *
   * @param dictFile the dictionary to be used
   * @param type the named entity class
   * @param nameFactory the factory
   * @return an instance of a {@link DictionaryNameFinder}
   */
  public final DictionaryNameFinder createDictNameFinder(final String dictFile, final String type,
      final NameFactory nameFactory) {
    InputStream dictStream = getClass().getResourceAsStream("/" + dictFile);
    Dictionary dict = new Dictionary(dictStream);
    DictionaryNameFinder dictNameFinder = new DictionaryNameFinder(dict, type, nameFactory);
    return dictNameFinder;
  }
}

