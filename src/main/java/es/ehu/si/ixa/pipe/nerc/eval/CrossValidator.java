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

package es.ehu.si.ixa.pipe.nerc.eval;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import opennlp.tools.cmdline.namefind.NameEvaluationErrorListener;
import opennlp.tools.cmdline.namefind.TokenNameFinderDetailedFMeasureListener;
import opennlp.tools.formats.Conll02NameSampleStream;
import opennlp.tools.formats.Conll03NameSampleStream;
import opennlp.tools.formats.EvalitaNameSampleStream;
import opennlp.tools.namefind.BilouCodec;
import opennlp.tools.namefind.BioCodec;
import opennlp.tools.namefind.NameSample;
import opennlp.tools.namefind.NameSampleDataStream;
import opennlp.tools.namefind.NameSampleTypeFilter;
import opennlp.tools.namefind.TokenNameFinderCrossValidator;
import opennlp.tools.namefind.TokenNameFinderEvaluationMonitor;
import opennlp.tools.namefind.TokenNameFinderFactory;
import opennlp.tools.util.ObjectStream;
import opennlp.tools.util.SequenceCodec;
import opennlp.tools.util.TrainingParameters;
import opennlp.tools.util.eval.EvaluationMonitor;
import es.ehu.si.ixa.pipe.nerc.features.XMLFeatureDescriptor;
import es.ehu.si.ixa.pipe.nerc.formats.GermEval2014InnerNameStream;
import es.ehu.si.ixa.pipe.nerc.formats.GermEval2014OuterNameStream;
import es.ehu.si.ixa.pipe.nerc.train.FixedTrainer;
import es.ehu.si.ixa.pipe.nerc.train.Flags;
import es.ehu.si.ixa.pipe.nerc.train.InputOutputUtils;

/**
 * Abstract class for common training functionalities. Every other trainer class
 * needs to extend this class.
 * @author ragerri
 * @version 2014-04-17
 */
public class CrossValidator {
  
  /**
   * The language.
   */
  private String lang;
  /**
   * String holding the training data.
   */
  private String trainData;
  /**
   * ObjectStream of the training data.
   */
  private ObjectStream<NameSample> trainSamples;
  /**
   * beamsize value needs to be established in any class extending this one.
   */
  private int beamSize;
  /**
   * The folds valute for cross validation.
   */
  private int folds;
  /**
   * The sequence encoding of the named entity spans, e.g., BIO or BILOU.
   */
  private SequenceCodec<String> sequenceCodec;
  /**
   * The corpus format: conll02, conll03, germEvalOuter2014, germEvalInner2014 and opennlp.
   */
  private String corpusFormat;
  /**
   * The named entity types.
   */
  private static int types;
  /**
   * features needs to be implemented by any class extending this one.
   */
  private TokenNameFinderFactory nameClassifierFactory;
  /**
   * The evaluation listeners.
   */
  private List<EvaluationMonitor<NameSample>> listeners = new LinkedList<EvaluationMonitor<NameSample>>();
  TokenNameFinderDetailedFMeasureListener detailedFListener;

  
  public CrossValidator(final TrainingParameters params) throws IOException {
    
    this.lang = Flags.getLanguage(params);
    this.corpusFormat = Flags.getCorpusFormat(params);
    this.trainData = params.getSettings().get("TrainSet");
    trainSamples = getNameStream(trainData, lang, corpusFormat);
    this.beamSize = Flags.getBeamsize(params);
    this.folds = Flags.getFolds(params);
    this.sequenceCodec =  TokenNameFinderFactory.instantiateSequenceCodec(getSequenceCodec(Flags.getSequenceCodec(params)));
    if (params.getSettings().get("Types") != null) {
      String netypes = params.getSettings().get("Types");
      String[] neTypes = netypes.split(",");
      trainSamples = new NameSampleTypeFilter(neTypes, trainSamples);
      types = neTypes.length;
    }
    createNameFactory(params);
    getEvalListeners(params);
  }

  private void createNameFactory(TrainingParameters params) throws IOException {
    String featureDescription = XMLFeatureDescriptor
        .createXMLFeatureDescriptor(params);
    System.err.println(featureDescription);
    byte[] featureGeneratorBytes = featureDescription.getBytes(Charset
        .forName("UTF-8"));
    Map<String, Object> resources = FixedTrainer.loadResources(params, featureGeneratorBytes);
    this.nameClassifierFactory = TokenNameFinderFactory.create(
        TokenNameFinderFactory.class.getName(), featureGeneratorBytes,
        resources, sequenceCodec);
  }
  
  private void getEvalListeners(TrainingParameters params) {
    if (params.getSettings().get("EvaluationType").equalsIgnoreCase("error")) {
      listeners.add(new NameEvaluationErrorListener());
    }
    if (params.getSettings().get("EvaluationType").equalsIgnoreCase("detailed")) {
      detailedFListener = new TokenNameFinderDetailedFMeasureListener();
      listeners.add(detailedFListener);
    }
  }
  
  public final void crossValidate(final TrainingParameters params) {
    if (nameClassifierFactory == null) {
      throw new IllegalStateException(
          "Classes derived from AbstractNameFinderTrainer must create and fill the AdaptiveFeatureGenerator features!");
    }
    TokenNameFinderCrossValidator validator = null;
    try {
      validator = new TokenNameFinderCrossValidator(lang,
          null, params, nameClassifierFactory,
          listeners.toArray(new TokenNameFinderEvaluationMonitor[listeners.size()]));
      validator.evaluate(trainSamples, folds);
    } catch (IOException e) {
      System.err.println("IO error while loading training set!");
      e.printStackTrace();
      System.exit(1);
    } finally {
      try {
        trainSamples.close();
      } catch (IOException e) {
        System.err.println("IO error with the train samples!");
      }
    }
    if (detailedFListener == null) {
      System.out.println(validator.getFMeasure());
    } else {
      System.out.println(detailedFListener.toString());
    }
  }

  /**
   * Getting the stream with the right corpus format.
   * @param inputData
   *          the input data
   * @param aLang
   *          the language
   * @param aCorpusFormat
   *          the corpus format
   * @return the stream from the several corpus formats
   * @throws IOException
   *           the io exception
   */
  public static ObjectStream<NameSample> getNameStream(final String inputData,
      final String aLang, final String aCorpusFormat) throws IOException {
    ObjectStream<NameSample> samples = null;
    if (aCorpusFormat.equalsIgnoreCase("conll03")) {
      ObjectStream<String> nameStream = InputOutputUtils.readFileIntoMarkableStreamFactory(inputData);
      if (aLang.equalsIgnoreCase("en")) {
        samples = new Conll03NameSampleStream(Conll03NameSampleStream.LANGUAGE.EN, nameStream, types);
      }
      else if (aLang.equalsIgnoreCase("de")) {
        samples = new Conll03NameSampleStream(Conll03NameSampleStream.LANGUAGE.DE, nameStream, types);
      } 
    } else if (aCorpusFormat.equalsIgnoreCase("conll02")) {
      ObjectStream<String> nameStream = InputOutputUtils
          .readFileIntoMarkableStreamFactory(inputData);
      if (aLang.equalsIgnoreCase("es")) {
        samples = new Conll02NameSampleStream(Conll02NameSampleStream.LANGUAGE.ES, nameStream, types);
      }
      else if (aLang.equalsIgnoreCase("nl")) {
        samples = new Conll02NameSampleStream(Conll02NameSampleStream.LANGUAGE.NL, nameStream, types);
      }
    } else if (aCorpusFormat.equalsIgnoreCase("evalita")) {
      ObjectStream<String> nameStream = InputOutputUtils.readFileIntoMarkableStreamFactory(inputData);
      samples = new EvalitaNameSampleStream(EvalitaNameSampleStream.LANGUAGE.IT, nameStream, types);
    } else if (aCorpusFormat.equalsIgnoreCase("germEvalOuter2014")) {
      ObjectStream<String> nameStream = InputOutputUtils
          .readFileIntoMarkableStreamFactory(inputData);
      samples = new GermEval2014OuterNameStream(nameStream);
    } else if (aCorpusFormat.equalsIgnoreCase("germEvalInner2014")) {
      ObjectStream<String> nameStream = InputOutputUtils.readFileIntoMarkableStreamFactory(inputData);
      samples = new GermEval2014InnerNameStream(nameStream);
    } else if (aCorpusFormat.equalsIgnoreCase("opennlp")) {
      ObjectStream<String> nameStream = InputOutputUtils.readFileIntoMarkableStreamFactory(inputData);
      samples = new NameSampleDataStream(nameStream);
    } else {
      System.err.println("Input data format not valid!!");
      System.exit(1);
    }
    return samples;
  }  
  /**
   * Get the Sequence codec.
   * @return the sequence codec
   */
  public final String getSequenceCodec(String seqCodecOption) {
    String seqCodec = null;
    if ("BIO".equals(seqCodecOption)) {
      seqCodec = BioCodec.class.getName();
    }
    else if ("BILOU".equals(seqCodecOption)) {
      seqCodec = BilouCodec.class.getName();
    }
    return seqCodec;
  }
  
  public final int getBeamSize() {
    return beamSize;
  }

}

