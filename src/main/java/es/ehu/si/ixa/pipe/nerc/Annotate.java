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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
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
  
  NameFactory nameFactory;
  /**
   * The NameFinder to do the annotation.
   */
  private NameFinder nameFinder;
  /**
   * The dictionary name finders to do the post processing.
   */
  private DictionariesNameFinder dictFinder;
  /**
   * The NameFinder Lexer for rule-based name finding.
   */
  private NumericNameFinder numericLexerFinder;
  /**
   * True if the name finder is statistical.
   */
  private boolean statistical;
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
   * Activates name finding using {@code NameFinderLexer}s.
   */
  private boolean lexerFind;
  
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
    nameFactory = new NameFactory();
    nameFinder = new StatisticalNameFinder(lang, nameFactory, model, features, beamsize);
    statistical = true;
  }

  //TODO this constructor needs heavy refactoring
  public Annotate(final String lang, final String dictOption, final String dictPath, final String ruleBasedOption, final String model,
      final String features, final int beamsize) {
    if (dictOption != null) {
      if (model.equalsIgnoreCase("baseline") && !dictOption.equalsIgnoreCase("tag")) {
      System.err.println("No NERC model chosen, reverting to baseline model!");
      }
    }
    nameFactory = new NameFactory();
    nameFinder = new StatisticalNameFinder(lang, nameFactory, model, features, beamsize, dictPath);
    if (dictOption != null && dictPath != null) {
      Dictionaries dictionary = new Dictionaries(dictPath);
      dictFinder = new DictionariesNameFinder(dictionary, nameFactory);
      if (dictOption.equalsIgnoreCase("post")) {
        postProcess = true;
        statistical = true;
      }
      if (dictOption.equalsIgnoreCase("tag")) {
        dictTag = true;
        statistical = false;
        postProcess = false;
      }
    }
    if (ruleBasedOption != null) {
      if (ruleBasedOption.equalsIgnoreCase("numeric")) {
        if (dictOption != null) {
          if (dictOption.equalsIgnoreCase("tag")) {
            lexerFind = true;
            statistical = false;
          }
        }
        else {
          lexerFind = true;
          statistical = true;
        }
      }
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
      if (statistical) {
        //TODO clearAdaptiveFeatures; evaluate
        allSpans = nameFinder.nercToSpans(tokens);
      }
      if (postProcess) {
        List<Span> dictSpans = dictFinder.nercToSpansExact(tokens);
        SpanUtils.postProcessDuplicatedSpans(allSpans, dictSpans);
        SpanUtils.concatenateSpans(allSpans, dictSpans);
      }
      if (dictTag) {
        allSpans = dictFinder.nercToSpansExact(tokens);
      }
      if (lexerFind) {
        String sentenceText = StringUtils.getSentenceFromTokens(tokens);
        //System.err.println("Sentence: " + sentenceText);
        StringReader stringReader = new StringReader(sentenceText);
        BufferedReader sentenceReader = new BufferedReader(stringReader);
        numericLexerFinder = new NumericNameFinder(sentenceReader, nameFactory);
        List<Span> numericSpans = numericLexerFinder.nercToSpans(tokens);
        SpanUtils.concatenateSpans(allSpans, numericSpans);
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
  public final DictionariesNameFinder createDictNameFinder(final String dictPath,
      final NameFactory nameFactory) {
    Dictionaries dict = new Dictionaries(dictPath);
    DictionariesNameFinder dictNameFinder = new DictionariesNameFinder(dict, nameFactory);
    return dictNameFinder;
  }
}

