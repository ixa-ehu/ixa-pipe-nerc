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

package es.ehu.si.ixa.pipe.nerc;

import es.ehu.si.ixa.pipe.nerc.train.BaselineNameFinderTrainer;
import es.ehu.si.ixa.pipe.nerc.train.Dict3NameFinderTrainer;
import es.ehu.si.ixa.pipe.nerc.train.DictLbjNameFinderTrainer;
import es.ehu.si.ixa.pipe.nerc.train.NameFinderTrainer;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import opennlp.tools.namefind.NameFinderME;
import opennlp.tools.namefind.TokenNameFinderModel;
import opennlp.tools.util.Span;

/**
 * Named Entity Recognition module based on Apache OpenNLP Machine Learning API.
 *
 * @author ragerri
 * @version 2014-04-04
 *
 */

public class StatisticalNameFinder implements NameFinder {

  /**
   * The model to be instantiated. We ensure that only one instance of nercModel
   * is used for every instantiation of this class.
   */
  private static TokenNameFinderModel nercModel;
  /**
   * The name finder.
   */
  private NameFinderME nameFinder;
  /**
   * The name factory.
   */
  private NameFactory nameFactory;
  /**
   * The trainer called to obtain the appropriate features.
   */
  private NameFinderTrainer nameFinderTrainer;

  /**
   * Construct a StatisticalNameFinder without name factory.
   *
   * @param lang
   *          the language
   * @param model
   *          the name of the model to be used
   * @param features
   *          the features
   * @param beamsize
   *          the beam size decoding
   */
  public StatisticalNameFinder(final String lang, final String model,
      final String features, final int beamsize) {

    InputStream trainedModelInputStream = null;
    try {
      if (nercModel == null) {
        if (model.equalsIgnoreCase("baseline")) {
          trainedModelInputStream = getBaselineModelStream(lang, model);
          System.err.println("No model chosen, reverting to baseline model!");
        } else {
          trainedModelInputStream = new FileInputStream(model);
        }
        nercModel = new TokenNameFinderModel(trainedModelInputStream);
      }
    } catch (IOException e) {
      e.printStackTrace();
    } finally {
      if (trainedModelInputStream != null) {
        try {
          trainedModelInputStream.close();
        } catch (IOException e) {
          System.err.println("Could not load model!");
        }
      }
    }
    nameFinderTrainer = getNameFinderTrainer(features, beamsize);
    nameFinder = new NameFinderME(nercModel,
        nameFinderTrainer.createFeatureGenerator(), beamsize);
  }

  /**
   * Construct a StatisticalNameFinder without name factory and with
   * default beam size.
   *
   * @param lang the language
   * @param model the model
   * @param features the features
   */
  public StatisticalNameFinder(final String lang, final String model, final String features) {
    this(lang, model, features, CLI.DEFAULT_BEAM_SIZE);
  }

  /**
   * Construct a StatisticalNameFinder specifying the language,
   * a name factory, the model, the features and the beam size for
   * decoding.
   *
   * @param lang the language
   * @param aNameFactory the name factory to construct Name objects
   * @param model the model
   * @param features the features
   * @param beamsize the beam size for decoding
   */
  public StatisticalNameFinder(final String lang, final NameFactory aNameFactory,
      final String model, final String features, final int beamsize) {

    this.nameFactory = aNameFactory;
    InputStream trainedModelInputStream = null;
    try {
      if (nercModel == null) {
        if (model.equalsIgnoreCase("baseline")) {
          trainedModelInputStream = getBaselineModelStream(lang, model);
          System.err.println("No model chosen, reverting to baseline model!");
        } else {
          trainedModelInputStream = new FileInputStream(model);
        }
        nercModel = new TokenNameFinderModel(trainedModelInputStream);
      }
    } catch (IOException e) {
      e.printStackTrace();
    } finally {
      if (trainedModelInputStream != null) {
        try {
          trainedModelInputStream.close();
        } catch (IOException e) {
          System.err.println("Could not load model!");
        }
      }
    }
    nameFinderTrainer = getNameFinderTrainer(features, beamsize);
    nameFinder = new NameFinderME(nercModel,
        nameFinderTrainer.createFeatureGenerator(), beamsize);
  }

  /**
   * Construct a StatisticalNameFinder with name factory and
   * with default beam size.
   *
   * @param lang the language
   * @param aNameFactory the name factory
   * @param model the model
   * @param features the features
   */
  public StatisticalNameFinder(final String lang, final NameFactory aNameFactory,
      final String model, final String features) {
    this(lang, aNameFactory, model, features, CLI.DEFAULT_BEAM_SIZE);
  }

  /**
   * Method to produce a list of the {@link Name} objects classified by the
   * probabilistic model.
   *
   * Takes an array of tokens, calls nercToSpans function for probabilistic NERC
   * and returns a List of {@link Name} objects containing the nameString, the
   * type and the {@link Span}
   *
   * @param tokens
   *          an array of tokenized text
   * @return a List of names
   */
  public final List<Name> getNames(final String[] tokens) {
    List<Span> origSpans = nercToSpans(tokens);
    Span[] neSpans = NameFinderME.dropOverlappingSpans(origSpans
        .toArray(new Span[origSpans.size()]));
    List<Name> names = getNamesFromSpans(neSpans, tokens);
    return names;
  }

  /**
   * This method receives as input an array of tokenized text and calls the
   * NameFinderME.find(tokens) to recognize and classify Named Entities. It
   * outputs the spans of the detected and classified Named Entities.
   *
   * From Apache OpenNLP documentation: "After every document clearAdaptiveData
   * must be called to clear the adaptive data in the feature generators. Not
   * calling clearAdaptiveData can lead to a sharp drop in the detection rate
   * after a few documents."
   *
   * @param tokens
   *          an array of tokenized text
   * @return an list of {@link Span}s of Named Entities
   */
  public final List<Span> nercToSpans(final String[] tokens) {
    Span[] annotatedText = nameFinder.find(tokens);
    clearAdaptiveData();
    List<Span> probSpans = new ArrayList<Span>(Arrays.asList(annotatedText));
    return probSpans;
  }

  /**
   * Creates a list of {@link Name} objects from spans and tokens.
   *
   * @param neSpans the named entity spans of a sentence
   * @param tokens the tokens in the sentence
   * @return a list of {@link Name} objects
   */
  public final List<Name> getNamesFromSpans(final Span[] neSpans, final String[] tokens) {
    List<Name> names = new ArrayList<Name>();
    for (Span neSpan : neSpans) {
      String nameString = StringUtils.getStringFromSpan(neSpan, tokens);
      String neType = neSpan.getType();
      Name name = nameFactory.createName(nameString, neType, neSpan);
      names.add(name);
    }
    return names;
  }

  /**
   * Forgets all adaptive data which was collected during previous calls to one
   * of the find methods. This method is typically called at the end of a
   * document.
   *
   * From Apache OpenNLP documentation: "After every document clearAdaptiveData
   * must be called to clear the adaptive data in the feature generators. Not
   * calling clearAdaptiveData can lead to a sharp drop in the detection rate
   * after a few documents."
   */
  public final void clearAdaptiveData() {
    nameFinder.clearAdaptiveData();
  }

  /**
   * Instantiates a NameFinderTrainer with specific features and beam size.
   *
   * @param features the features
   * @param beamsize the beam size
   * @return an instance of a NameFinderTrainer
   */
  public final NameFinderTrainer getNameFinderTrainer(final String features, final int beamsize) {
    if (features.equalsIgnoreCase("baseline")) {
      nameFinderTrainer = new BaselineNameFinderTrainer(beamsize);
    } else if (features.equalsIgnoreCase("dict3")) {
      nameFinderTrainer = new Dict3NameFinderTrainer(beamsize);
    } else if (features.equalsIgnoreCase("dict4")) {
      nameFinderTrainer = new Dict3NameFinderTrainer(beamsize);
    } else if (features.equalsIgnoreCase("dictlbj")) {
      nameFinderTrainer = new DictLbjNameFinderTrainer(beamsize);
    }
    return nameFinderTrainer;
  }

  /**
   * Method to produce back-off baseline model when no model is
   * chosen in the command line.
   *
   * @param lang the language
   * @param model the default value to load the baseline model
   * @return the inputstream from a model
   */
  private InputStream getBaselineModelStream(final String lang, final String model) {
    InputStream trainedModelInputStream = null;
    if (lang.equalsIgnoreCase("en")) {
      trainedModelInputStream = getClass().getResourceAsStream(
          "/en/en-nerc-perceptron-baseline-c0-b3.bin");
    }
    if (lang.equalsIgnoreCase("es")) {
      trainedModelInputStream = getClass().getResourceAsStream(
          "/es/es-nerc-maxent-baseline-500-c4-b3.bin");
    }
    return trainedModelInputStream;
  }

}
