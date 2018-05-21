/*
 *  Copyright 2016 Rodrigo Agerri

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

package eus.ixa.ixa.pipe.nerc;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import com.google.common.collect.Lists;

import eus.ixa.ixa.pipe.ml.StatisticalSequenceLabeler;
import eus.ixa.ixa.pipe.ml.nerc.DictionariesNERTagger;
import eus.ixa.ixa.pipe.ml.nerc.NumericNERTagger;
import eus.ixa.ixa.pipe.ml.resources.Dictionaries;
import eus.ixa.ixa.pipe.ml.sequence.SequenceLabel;
import eus.ixa.ixa.pipe.ml.sequence.SequenceLabelSample;
import eus.ixa.ixa.pipe.ml.sequence.SequenceLabelerME;
import eus.ixa.ixa.pipe.ml.utils.Flags;
import eus.ixa.ixa.pipe.ml.utils.Span;
import eus.ixa.ixa.pipe.ml.utils.StringUtils;
import ixa.kaflib.Entity;
import ixa.kaflib.KAFDocument;
import ixa.kaflib.Term;
import ixa.kaflib.WF;

/**
 * Annotation class for Named Entities in ixa-pipe-nerc. Use this class for
 * examples on using ixa-pipe-ml API for Named Entity tagging.
 * 
 * @author ragerri
 * @version 2016-04-22
 * 
 */
public class Annotate {

  /**
   * The SequenceLabeler to do the annotation.
   */
  private StatisticalSequenceLabeler nerTagger;
  /**
   * The dictionaries.
   */
  private Dictionaries dictionaries;
  /**
   * The dictionary Named Entity Tagger.
   */
  private DictionariesNERTagger nerTaggerDict;
  /**
   * The Named Entity Lexer for rule-based name tagging.
   */
  private NumericNERTagger numericNerTaggerLexer;
  /**
   * True if the NER tagger is statistical.
   */
  private boolean statistical;
  /**
   * Activates post processing of statistical sequence labeling with dictionary
   * name finders.
   */
  private boolean postProcess;
  /**
   * Activates NER tagging using dictionaries only.
   */
  private boolean dictTag;
  /**
   * Activates NER tagging using {@code NumericNameFinder}s.
   */
  private boolean lexerTagger;
  /**
   * Clear features after every sentence or when a -DOCSTART- mark appears.
   */
  private String clearFeatures;

  /**
   * It manages the use of the three different name finders:
   * {@code StatisticalSequenceLabeler}, {@code DictionariesNameFinder} and
   * {@code NumericNameFinder}. In particular, if --dictTag option in CLI is
   * off, statistical models are used (this is the default). If --dictTag is
   * activated, it has two options, "tag" and "post": tag only tags with a
   * gazetteer and "post" post-processes the probabilistic annotation giving
   * priority to the gazetteer. Obviously, this option depends on the --dictPath
   * parameter being correctly specified. The --lexer numeric option annotates
   * numeric entities (dates, percentages, and so on) via rules.
   * 
   * @param properties
   *          the properties
   * @throws IOException
   *           the io thrown
   */
  public Annotate(final Properties properties) throws IOException {

    this.clearFeatures = properties.getProperty("clearFeatures");
    annotateOptions(properties);
  }

  /**
   * Generates the right options for NERC tagging: using the
   * {@link StatisticalSequenceLabeler} or using the
   * {@link DictionariesNERTagger} or a combination of those with the
   * {@link NumericNERTagger}.
   * 
   * @param properties
   *          the parameters to choose the NameFinder are lexer, dictTag and
   *          dictPath
   * @throws IOException
   *           the io exception
   */
  // TODO surely we can simplify this?
  private void annotateOptions(Properties properties) throws IOException {

    String ruleBasedOption = properties.getProperty("ruleBasedOption");
    String dictOption = properties.getProperty("dictTag");
    String dictPath = properties.getProperty("dictPath");

    if (!dictOption.equals(Flags.DEFAULT_DICT_OPTION)) {
      if (dictPath.equals(Flags.DEFAULT_DICT_PATH)) {
        Flags.dictionaryException();
      }
      if (!ruleBasedOption.equals(Flags.DEFAULT_LEXER)) {
        lexerTagger = true;
      }
      if (!dictPath.equals(Flags.DEFAULT_DICT_PATH)) {
        if (dictionaries == null) {
          dictionaries = new Dictionaries(dictPath);
          nerTaggerDict = new DictionariesNERTagger(dictionaries);
        }
        if (dictOption.equalsIgnoreCase("tag")) {
          dictTag = true;
          postProcess = false;
          statistical = false;
        } else if (dictOption.equalsIgnoreCase("post")) {
          nerTagger = new StatisticalSequenceLabeler(properties);
          statistical = true;
          postProcess = true;
          dictTag = false;
        } else {
          nerTagger = new StatisticalSequenceLabeler(properties);
          statistical = true;
          dictTag = false;
          postProcess = false;
        }
      }
    } else if (!ruleBasedOption.equals(Flags.DEFAULT_LEXER)) {
      lexerTagger = true;
      statistical = true;
      dictTag = false;
      postProcess = false;
      nerTagger = new StatisticalSequenceLabeler(properties);
    } else {
      lexerTagger = false;
      statistical = true;
      dictTag = false;
      postProcess = false;
      nerTagger = new StatisticalSequenceLabeler(properties);
    }
  }

  /**
   * Classify Named Entities creating the entities layer in the
   * {@link KAFDocument} using statistical models, post-processing and/or
   * dictionaries only.
   * 
   * @param kaf
   *          the kaf document to be used for annotation
   * @throws IOException
   *           throws exception if problems with the kaf document
   */
  public final void annotateNEsToKAF(final KAFDocument kaf) throws IOException {

    List<Span> allSpans = null;
    List<List<WF>> sentences = kaf.getSentences();

    for (List<WF> sentence : sentences) {
      // process each sentence
      String[] tokens = new String[sentence.size()];
      String[] tokenIds = new String[sentence.size()];
      for (int i = 0; i < sentence.size(); i++) {
        tokens[i] = sentence.get(i).getForm();
        tokenIds[i] = sentence.get(i).getId();
      }
      if (statistical) {
        if (clearFeatures.equalsIgnoreCase("docstart")
            && tokens[0].startsWith("-DOCSTART-")) {
          nerTagger.clearAdaptiveData();
        }
        Span[] statSpans = nerTagger.seqToSpans(tokens);
        allSpans = Lists.newArrayList(statSpans);
      }
      if (postProcess) {
        Span[] dictSpans = nerTaggerDict.nercToSpansExact(tokens);
        Span.postProcessDuplicatedSpans(allSpans, dictSpans);
        Span.concatenateSpans(allSpans, dictSpans);
      }
      if (dictTag) {
        Span[] dictOnlySpans = nerTaggerDict.nercToSpansExact(tokens);
        allSpans = Lists.newArrayList(dictOnlySpans);
      }
      if (lexerTagger) {
        String sentenceText = StringUtils.getStringFromTokens(tokens);
        StringReader stringReader = new StringReader(sentenceText);
        BufferedReader sentenceReader = new BufferedReader(stringReader);
        numericNerTaggerLexer = new NumericNERTagger(sentenceReader);
        Span[] numericSpans = numericNerTaggerLexer.nercToSpans(tokens);
        Span.concatenateSpans(allSpans, numericSpans);
      }
      Span[] allSpansArray = SequenceLabelerME
          .dropOverlappingSpans(allSpans.toArray(new Span[allSpans.size()]));
      List<SequenceLabel> names = new ArrayList<>();
      if (statistical) {
        names = nerTagger.getSequencesFromSpans(tokens, allSpansArray);
      } else {
        names = nerTaggerDict.getNamesFromSpans(allSpansArray, tokens);
      }
      for (SequenceLabel name : names) {
        Integer startIndex = name.getSpan().getStart();
        Integer endIndex = name.getSpan().getEnd();
        List<String> wfIds = Arrays
            .asList(Arrays.copyOfRange(tokenIds, startIndex, endIndex));
        List<String> wfTermIds = getAllWFIdsFromTerms(kaf);
        if (checkTermsRefsIntegrity(wfIds, wfTermIds)) {
          List<Term> nameTerms = kaf.getTermsFromWFs(wfIds);
          ixa.kaflib.Span<Term> neSpan = KAFDocument.newTermSpan(nameTerms);
          List<ixa.kaflib.Span<Term>> references = new ArrayList<ixa.kaflib.Span<Term>>();
          references.add(neSpan);
          Entity neEntity = kaf.newEntity(references);
          neEntity.setType(name.getType());
        }
      }
      if (clearFeatures.equalsIgnoreCase("yes")) {
        nerTagger.clearAdaptiveData();
      }
    }
    if (statistical) {
      nerTagger.clearAdaptiveData();
    }
  }

  /**
   * Get all the WF ids for the terms contained in the KAFDocument.
   * 
   * @param kaf
   *          the KAFDocument
   * @return the list of all WF ids in the terms layer
   */
  public List<String> getAllWFIdsFromTerms(KAFDocument kaf) {
    List<Term> terms = kaf.getTerms();
    List<String> wfTermIds = new ArrayList<>();
    for (int i = 0; i < terms.size(); i++) {
      List<WF> sentTerms = terms.get(i).getWFs();
      for (WF form : sentTerms) {
        wfTermIds.add(form.getId());
      }
    }
    return wfTermIds;
  }

  /**
   * Check that the references from the entity spans are actually contained in
   * the term ids.
   * 
   * @param wfIds
   *          the worform ids corresponding to the Term span
   * @param termWfIds
   *          all the terms in the document
   * @return true or false
   */
  public boolean checkTermsRefsIntegrity(List<String> wfIds,
      List<String> termWfIds) {
    for (int i = 0; i < wfIds.size(); i++) {
      if (!termWfIds.contains(wfIds.get(i))) {
        return false;
      }
    }
    return true;
  }

  /**
   * Enumeration class for CoNLL 2003 BIO format
   */
  private static enum BIO {
    BEGIN("B-"), IN("I-"), OUT("O");
    String tag;

    BIO(String tag) {
      this.tag = tag;
    }

    public String toString() {
      return this.tag;
    }
  }

  /**
   * Output Conll2003 format.
   * 
   * @param kaf
   *          the kaf document
   * @return the annotated named entities in conll03 format
   */
  public String annotateNEsToCoNLL2003(KAFDocument kaf) {
    List<Entity> namedEntityList = kaf.getEntities();
    Map<String, Integer> entityToSpanSize = new HashMap<String, Integer>();
    Map<String, String> entityToType = new HashMap<String, String>();
    for (Entity ne : namedEntityList) {
      List<ixa.kaflib.Span<Term>> entitySpanList = ne.getSpans();
      for (ixa.kaflib.Span<Term> spanTerm : entitySpanList) {
        Term neTerm = spanTerm.getFirstTarget();
        // create map from term Id to Entity span size
        entityToSpanSize.put(neTerm.getId(), spanTerm.size());
        // create map from term Id to Entity type
        entityToType.put(neTerm.getId(), ne.getType());
      }
    }

    List<List<WF>> sentences = kaf.getSentences();
    StringBuilder sb = new StringBuilder();
    for (List<WF> sentence : sentences) {
      int sentNumber = sentence.get(0).getSent();
      List<Term> sentenceTerms = kaf.getSentenceTerms(sentNumber);
      String previousType = null;
      boolean previousIsEntity = false;

      for (int i = 0; i < sentenceTerms.size(); i++) {
        Term thisTerm = sentenceTerms.get(i);
        // if term is inside an entity span then annotate B-I entities
        if (entityToSpanSize.get(thisTerm.getId()) != null) {
          int neSpanSize = entityToSpanSize.get(thisTerm.getId());
          String neClass = entityToType.get(thisTerm.getId());
          String neType = this.convertToConLLTypes(neClass);
          // if Entity span is multi token
          if (neSpanSize > 1) {
            for (int j = 0; j < neSpanSize; j++) {
              thisTerm = sentenceTerms.get(i + j);
              sb.append(thisTerm.getForm());
              sb.append("\t");
              sb.append(thisTerm.getLemma());
              sb.append("\t");
              sb.append(thisTerm.getMorphofeat());
              sb.append("\t");
              if (j == 0 && previousIsEntity
                  && previousType.equalsIgnoreCase(neType)) {
                sb.append(BIO.BEGIN.toString());
              } else {
                sb.append(BIO.IN.toString());
              }
              sb.append(neType);
              sb.append("\n");
            }
            previousType = neType;
          } else {
            sb.append(thisTerm.getForm());
            sb.append("\t");
            sb.append(thisTerm.getLemma());
            sb.append("\t");
            sb.append(thisTerm.getMorphofeat());
            sb.append("\t");
            if (previousIsEntity && previousType.equalsIgnoreCase(neType)) {
              sb.append(BIO.BEGIN.toString());
            } else {
              sb.append(BIO.IN.toString());
            }
            sb.append(neType);
            sb.append("\n");
          }
          previousIsEntity = true;
          previousType = neType;
          i += neSpanSize - 1;
        } else {
          sb.append(thisTerm.getForm());
          sb.append("\t");
          sb.append(thisTerm.getLemma());
          sb.append("\t");
          sb.append(thisTerm.getMorphofeat());
          sb.append("\t");
          sb.append(BIO.OUT);
          sb.append("\n");
          previousIsEntity = false;
          previousType = BIO.OUT.toString();
        }
      }
      sb.append("\n");// end of sentence
    }
    return sb.toString();
  }

  /**
   * Output Conll2002 format.
   * 
   * @param kaf
   *          the kaf document
   * @return the annotated named entities in conll03 format
   */
  public String annotateNEsToCoNLL2002(KAFDocument kaf) {
    List<Entity> namedEntityList = kaf.getEntities();
    Map<String, Integer> entityToSpanSize = new HashMap<String, Integer>();
    Map<String, String> entityToType = new HashMap<String, String>();
    for (Entity ne : namedEntityList) {
      List<ixa.kaflib.Span<Term>> entitySpanList = ne.getSpans();
      for (ixa.kaflib.Span<Term> spanTerm : entitySpanList) {
        Term neTerm = spanTerm.getFirstTarget();
        entityToSpanSize.put(neTerm.getId(), spanTerm.size());
        entityToType.put(neTerm.getId(), ne.getType());
      }
    }

    List<List<WF>> sentences = kaf.getSentences();
    StringBuilder sb = new StringBuilder();
    for (List<WF> sentence : sentences) {
      int sentNumber = sentence.get(0).getSent();
      List<Term> sentenceTerms = kaf.getSentenceTerms(sentNumber);

      for (int i = 0; i < sentenceTerms.size(); i++) {
        Term thisTerm = sentenceTerms.get(i);

        if (entityToSpanSize.get(thisTerm.getId()) != null) {
          int neSpanSize = entityToSpanSize.get(thisTerm.getId());
          String neClass = entityToType.get(thisTerm.getId());
          String neType = convertToConLLTypes(neClass);
          if (neSpanSize > 1) {
            for (int j = 0; j < neSpanSize; j++) {
              thisTerm = sentenceTerms.get(i + j);
              sb.append(thisTerm.getForm());
              sb.append("\t");
              sb.append(thisTerm.getLemma());
              sb.append("\t");
              sb.append(thisTerm.getMorphofeat());
              sb.append("\t");
              if (j == 0) {
                sb.append(BIO.BEGIN.toString());
              } else {
                sb.append(BIO.IN.toString());
              }
              sb.append(neType);
              sb.append("\n");
            }
          } else {
            sb.append(thisTerm.getForm());
            sb.append("\t");
            sb.append(thisTerm.getLemma());
            sb.append("\t");
            sb.append(thisTerm.getMorphofeat());
            sb.append("\t");
            sb.append(BIO.BEGIN.toString());
            sb.append(neType);
            sb.append("\n");
          }
          i += neSpanSize - 1;
        } else {
          sb.append(thisTerm.getForm());
          sb.append("\t");
          sb.append(thisTerm.getLemma());
          sb.append("\t");
          sb.append(thisTerm.getMorphofeat());
          sb.append("\t");
          sb.append(BIO.OUT);
          sb.append("\n");
        }
      }
      sb.append("\n");// end of sentence
    }
    return sb.toString();
  }

  /**
   * Convert Entity class annotation to CoNLL formats.
   * 
   * @param neType
   *          named entity class
   * @return the converted string
   */
  public String convertToConLLTypes(String neType) {
    String conllType = null;
    if (neType.equalsIgnoreCase("PERSON")
        || neType.equalsIgnoreCase("ORGANIZATION")
        || neType.equalsIgnoreCase("LOCATION") || neType.length() == 3) {
      conllType = neType.substring(0, 3);
    } else {
      conllType = neType;
    }
    return conllType;
  }
  
  public final String annotateNEsToOpenNLP(KAFDocument kaf) {
    StringBuilder sb = new StringBuilder();
    List<Span> allSpans = null;
    List<List<WF>> sentences = kaf.getSentences();

    for (List<WF> sentence : sentences) {
      // process each sentence
      String[] tokens = new String[sentence.size()];
      String[] tokenIds = new String[sentence.size()];
      for (int i = 0; i < sentence.size(); i++) {
        tokens[i] = sentence.get(i).getForm();
        tokenIds[i] = sentence.get(i).getId();
      }
      if (statistical) {
        if (clearFeatures.equalsIgnoreCase("docstart")
            && tokens[0].startsWith("-DOCSTART-")) {
          nerTagger.clearAdaptiveData();
        }
        Span[] statSpans = nerTagger.seqToSpans(tokens);
        allSpans = Lists.newArrayList(statSpans);
      }
      if (postProcess) {
        Span[] dictSpans = nerTaggerDict.nercToSpansExact(tokens);
        Span.postProcessDuplicatedSpans(allSpans, dictSpans);
        Span.concatenateSpans(allSpans, dictSpans);
      }
      if (dictTag) {
        Span[] dictOnlySpans = nerTaggerDict.nercToSpansExact(tokens);
        allSpans = Lists.newArrayList(dictOnlySpans);
      }
      if (lexerTagger) {
        String sentenceText = StringUtils.getStringFromTokens(tokens);
        StringReader stringReader = new StringReader(sentenceText);
        BufferedReader sentenceReader = new BufferedReader(stringReader);
        numericNerTaggerLexer = new NumericNERTagger(sentenceReader);
        Span[] numericSpans = numericNerTaggerLexer.nercToSpans(tokens);
        Span.concatenateSpans(allSpans, numericSpans);
      }
      boolean isClearAdaptiveData = false;
      if (clearFeatures.equalsIgnoreCase("yes")) {
        isClearAdaptiveData = true;
      }
      Span[] allSpansArray = SequenceLabelerME
          .dropOverlappingSpans(allSpans.toArray(new Span[allSpans.size()]));
      SequenceLabelSample seqSample = new SequenceLabelSample(tokens, allSpansArray, isClearAdaptiveData);
      sb.append(seqSample.toString()).append("\n");
    }
    nerTagger.clearAdaptiveData();
    return sb.toString().trim();
  }

}
