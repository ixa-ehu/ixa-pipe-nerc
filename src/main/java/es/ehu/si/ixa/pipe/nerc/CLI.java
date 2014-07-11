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

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;

import net.sourceforge.argparse4j.ArgumentParsers;
import net.sourceforge.argparse4j.inf.ArgumentParser;
import net.sourceforge.argparse4j.inf.ArgumentParserException;
import net.sourceforge.argparse4j.inf.Namespace;
import net.sourceforge.argparse4j.inf.Subparser;
import net.sourceforge.argparse4j.inf.Subparsers;
import opennlp.tools.namefind.TokenNameFinderModel;
import opennlp.tools.util.TrainingParameters;

import org.apache.commons.io.FilenameUtils;
import org.jdom2.JDOMException;

import es.ehu.si.ixa.pipe.nerc.dict.Dictionaries;
import es.ehu.si.ixa.pipe.nerc.eval.CorpusEvaluate;
import es.ehu.si.ixa.pipe.nerc.eval.Evaluate;
import es.ehu.si.ixa.pipe.nerc.train.DefaultNameFinderTrainer;
import es.ehu.si.ixa.pipe.nerc.train.InputOutputUtils;
import es.ehu.si.ixa.pipe.nerc.train.NameFinderTrainer;

/**
 * Main class of ixa-pipe-nerc, the ixa pipes (ixa2.si.ehu.es/ixa-pipes) NERC
 * tagger.
 * 
 * @author ragerri
 * @version 2014-06-26
 * 
 */
public class CLI {

  /**
   * Get dynamically the version of ixa-pipe-nerc by looking at the MANIFEST
   * file.
   */
  private final String version = CLI.class.getPackage()
      .getImplementationVersion();
  /**
   * Name space of the arguments provided at the CLI.
   */
  private Namespace parsedArguments = null;
  /**
   * Argument parser instance.
   */
  private ArgumentParser argParser = ArgumentParsers.newArgumentParser(
      "ixa-pipe-nerc-" + version + ".jar").description(
      "ixa-pipe-nerc-" + version
          + " is a multilingual NERC module developed by IXA NLP Group.\n");
  /**
   * Sub parser instance.
   */
  private Subparsers subParsers = argParser.addSubparsers().help(
      "sub-command help");
  /**
   * The parser that manages the tagging sub-command.
   */
  private Subparser annotateParser;
  /**
   * The parser that manages the training sub-command.
   */
  private Subparser trainParser;
  /**
   * The parser that manages the evaluation sub-command.
   */
  private Subparser evalParser;

  /**
   * Default beam size for decoding.
   */
  public static final int DEFAULT_BEAM_SIZE = 3;

  private String lang;
  private String features;
  private String trainSet;
  private String testSet;
  private String devSet;
  private String dictPath;
  private int beamsize;
  private String model;
  private String corpusFormat;
  private String neTypes;
  /**
   * Construct a CLI object with the three sub-parsers to manage the command
   * line parameters.
   */
  public CLI() {
    this.lang = parsedArguments.getString("lang");
    this.features = parsedArguments.getString("features");
    this.trainSet = parsedArguments.getString("trainSet");
    this.testSet = parsedArguments.getString("testSet");
    this.devSet = parsedArguments.getString("devSet");
    this.dictPath = parsedArguments.getString("dictPath");
    this.beamsize = parsedArguments.getInt("beamsize");
    this.model = parsedArguments.getString("model");
    this.corpusFormat = parsedArguments.getString("corpusFormat");
    this.neTypes = parsedArguments.getString("neTypes");
    //subparsers
    this.annotateParser = subParsers.addParser("tag").help("Tagging CLI");
    loadAnnotateParameters();
    this.trainParser = subParsers.addParser("train").help("Training CLI");
    loadTrainingParameters();
    this.evalParser = subParsers.addParser("eval").help("Evaluation CLI");
    loadEvalParameters();
  }

  /**
   * Main entry point of ixa-pipe-nerc.
   * 
   * @param args
   *          the arguments passed through the CLI
   * @throws IOException
   *           exception if input data not available
   * @throws JDOMException
   *           if problems with the xml formatting of NAF
   */
  public static void main(final String[] args) throws IOException,
      JDOMException {

    CLI cmdLine = new CLI();
    cmdLine.parseCLI(args);
  }

  /**
   * Parse the command interface parameters with the argParser.
   * 
   * @param args
   *          the arguments passed through the CLI
   * @throws IOException
   *           exception if problems with the incoming data
   */
  public final void parseCLI(final String[] args) throws IOException {
    try {
      parsedArguments = argParser.parseArgs(args);
      System.err.println("CLI options: " + parsedArguments);
      if (args[0].equals("tag")) {
        annotate(System.in, System.out);
      } else if (args[0].equals("eval")) {
        eval();
      } else if (args[0].equals("train")) {
        train();
      }
    } catch (ArgumentParserException e) {
      argParser.handleError(e);
      System.out.println("Run java -jar target/ixa-pipe-nerc-" + version
          + ".jar (tag|train|eval) -help for details");
      System.exit(1);
    }
  }

  /**
   * Main method to do Named Entity tagging.
   * 
   * @param inputStream
   *          the input stream containing the content to tag
   * @param outputStream
   *          the output stream providing the named entities
   * @throws IOException
   *           exception if problems in input or output streams
   */
  public final void annotate(final InputStream inputStream,
      final OutputStream outputStream) throws IOException {

    String gazetteerOption = parsedArguments.getString("dictionaries");
    String ruleBasedOption = parsedArguments.getString("lexer");
    String aModel;
    if (model == null) {
      aModel = "default";
    } else {
      aModel = this.model;
    }
    BufferedReader breader = new BufferedReader(new InputStreamReader(
        inputStream, "UTF-8"));
    BufferedWriter bwriter = new BufferedWriter(new OutputStreamWriter(
        outputStream, "UTF-8"));
    // read KAF document from inputstream
    KAFDocument kaf = KAFDocument.createFromStream(breader);
    // language parameter
    String aLang;
    if (parsedArguments.get("lang") == null) {
      aLang = kaf.getLang();
    } else {
      aLang = parsedArguments.getString("lang");
    }
    KAFDocument.LinguisticProcessor newLp = kaf.addLinguisticProcessor(
        "entities", "ixa-pipe-nerc-" + aLang + "-" + aModel, version);
    newLp.setBeginTimestamp();

    if (gazetteerOption != null || 
        ruleBasedOption != null || 
        dictPath != null || 
        features.equalsIgnoreCase("dict")) {
      Annotate annotator = new Annotate(aLang, aModel, features, beamsize, gazetteerOption, dictPath,
          ruleBasedOption);
      annotator.annotateNEsToKAF(kaf);
    } else {
      Annotate annotator = new Annotate(aLang, aModel, features, beamsize);
      annotator.annotateNEsToKAF(kaf);
    }
    newLp.setEndTimestamp();
    bwriter.write(kaf.toString());
    bwriter.close();
    breader.close();
  }

  /**
   * Main access to the train functionalities.
   * 
   * @throws IOException
   *           input output exception if problems with corpora
   */
  public final void train() throws IOException {

   
    String outModel = null;
    // load training parameters file
    String paramFile = parsedArguments.getString("params");
    TrainingParameters params = InputOutputUtils
        .loadTrainingParameters(paramFile);
    this.lang = params.getSettings().get("Language");
    this.neTypes = params.getSettings().get("Types");
    this.corpusFormat = params.getSettings().get("Corpus");
    this.beamsize = Integer.valueOf(params.getSettings().get("Beamsize"));
    String evalParam = params.getSettings().get("CrossEval");
    String[] evalRange = evalParam.split("[ :-]");

    if (parsedArguments.get("output") != null) {
      outModel = parsedArguments.getString("output");
    } else {
      outModel = FilenameUtils.removeExtension(trainSet) + "-"
          + parsedArguments.getString("features").toString() + "-model"
          + ".bin";
    }

   NameFinderTrainer nercTrainer = chooseTrainer();
    TokenNameFinderModel trainedModel = null;
    if (evalRange.length == 2) {
      if (parsedArguments.get("devSet") == null) {
        InputOutputUtils.devSetException();
      } else {
        trainedModel = nercTrainer.trainCrossEval(devSet, params,
            evalRange);
      }
    } else {
      trainedModel = nercTrainer.train(params);
    }
    InputOutputUtils.saveModel(trainedModel, outModel);
    System.out.println();
    System.out.println("Wrote trained NERC model to " + outModel);
  }

  /**
   * Main evaluation entry point.
   * 
   * @throws IOException
   *           throws exception if test set not available
   */
  public final void eval() throws IOException {

    String predFile = parsedArguments.getString("prediction");

    if (parsedArguments.getString("model") != null) {
      Dictionaries dictionaries = null;
      if (dictPath != null) {
        dictionaries = new Dictionaries(dictPath);
      }
      Evaluate evaluator = new Evaluate(dictionaries, testSet, model, features,
          lang, beamsize, corpusFormat, neTypes);
      if (parsedArguments.getString("evalReport") != null) {
        if (parsedArguments.getString("evalReport").equalsIgnoreCase("brief")) {
          evaluator.evaluate();
        } else if (parsedArguments.getString("evalReport").equalsIgnoreCase(
            "error")) {
          evaluator.evalError();
        } else if (parsedArguments.getString("evalReport").equalsIgnoreCase(
            "detailed")) {
          evaluator.detailEvaluate();
        }
      } else {
        evaluator.detailEvaluate();
      }
    } else if (parsedArguments.getString("prediction") != null) {
      CorpusEvaluate corpusEvaluator = new CorpusEvaluate(testSet, predFile,
          lang, corpusFormat, neTypes);
      corpusEvaluator.evaluate();
    } else {
      System.err
          .println("Provide either a model or a predictionFile to perform evaluation!");
    }
  }

  /**
   * Create the available parameters for NER tagging.
   */
  private void loadAnnotateParameters() {
    annotateParser.addArgument("-l", "--lang")
        .choices("de", "en", "es", "it", "nl").required(false)
        .help("Choose a language to perform annotation with ixa-pipe-nerc\n");
    annotateParser.addArgument("-f", "--features")
        .choices("opennlp", "baseline", "dict").required(false)
        .setDefault("baseline")
        .help("Choose features for NERC; it defaults to baseline\n");
    annotateParser.addArgument("-m", "--model").required(false)
        .help("Choose model to perform NERC annotation\n");
    annotateParser
        .addArgument("--beamsize")
        .setDefault(DEFAULT_BEAM_SIZE)
        .type(Integer.class)
        .help(
            "Choose beam size for decoding: 1 is faster and amounts to greedy search\n");
    annotateParser
        .addArgument("-d", "--dictionaries")
        .choices("tag", "post")
        .required(false)
        .help(
            "Use gazetteers directly for tagging or "
                + "for post-processing the probabilistic NERC output\n");
    annotateParser.addArgument("--dictPath").required(false)
        .help("Path to the dictionaries if -d or -f dict options (or both) are chosen\n");
    annotateParser.addArgument("--lexer").choices("numeric").required(false)
        .help("Use lexer rules for NERC tagging\n");
  }

  /**
   * Create the main parameters available for training NERC models.
   */
  private void loadTrainingParameters() {
    trainParser.addArgument("-f", "--features")
        .choices("opennlp", "baseline", "dict").required(true)
        .help("Choose features to train NERC model\n");
    trainParser
        .addArgument("--dictPath")
        .required(false)
        .help(
            "Provide directory containing dictionaries for its use with dict featureset\n");
    trainParser.addArgument("-p", "--params").required(true)
        .help("Load the parameters file\n");
    trainParser.addArgument("-i", "--trainSet").required(true)
        .help("Input training set\n");
    trainParser.addArgument("-t", "--testSet").required(true)
        .help("Input testset for evaluation\n");
    trainParser.addArgument("-d", "--devSet").required(false)
        .help("Input development set for cross-evaluation\n");
    trainParser.addArgument("-o", "--output").required(false)
        .help("Choose output file to save the annotation\n");
  }

  /**
   * Create the parameters available for evaluation.
   */
  private void loadEvalParameters() {
    evalParser.addArgument("-m", "--model").required(false)
        .help("Choose model\n");
    evalParser.addArgument("-f", "--features")
        .choices("opennlp", "baseline", "dict").required(true)
        .help("Choose features for evaluation\n");
    evalParser
        .addArgument("--dictPath")
        .required(false)
        .help(
            "Path to the gazetteers for evaluation if dict features are used\n");
    evalParser.addArgument("-l", "--lang").required(true)
        .choices("de", "en", "es", "it", "nl")
        .help("Choose language to load model for evaluation\n");
    evalParser.addArgument("-t", "--testSet").required(true)
        .help("Input testset for evaluation\n");
    evalParser
        .addArgument("--prediction")
        .required(false)
        .help(
            "Use this parameter to evaluate one prediction corpus against a reference corpus\n");
    evalParser.addArgument("--evalReport").required(false)
        .choices("brief", "detailed", "error")
        .help("Choose type of evaluation report; defaults to detailed\n");
    evalParser.addArgument("-c", "--corpus").setDefault("opennlp")
        .choices("conll", "opennlp", "germEvalOuter2014", "germEvalInner2014").help("choose format input of corpus\n");
    evalParser
        .addArgument("-n", "--neTypes")
        .required(false)
        .help(
            "Choose ne types to do the evaluation; it defaults to all represented in the testset\n");
    evalParser
        .addArgument("--beamsize")
        .setDefault(DEFAULT_BEAM_SIZE)
        .type(Integer.class)
        .help(
            "Choose beam size for evaluation: 1 is faster and amounts to greedy search\n");
  }
  
  /**
   * Choose the NameFinder training according to feature type and language.
   * @return the name finder trainer
   * @throws IOException throws
   */
  public NameFinderTrainer chooseTrainer() throws IOException {
    NameFinderTrainer nercTrainer = null;
    if (parsedArguments.getString("features").equalsIgnoreCase("opennlp")) {
      nercTrainer = new DefaultNameFinderTrainer(trainSet, testSet, lang,
          beamsize, corpusFormat, neTypes);
    } else if (features.equalsIgnoreCase(
        "baseline")) {
      if (lang.equalsIgnoreCase("de")) {
        nercTrainer = new es.ehu.si.ixa.pipe.nerc.train.lang.de.BaselineNameFinderTrainer(trainSet, testSet, lang, beamsize, corpusFormat, neTypes);
      }
      else if (lang.equalsIgnoreCase("en")) {
        nercTrainer = new es.ehu.si.ixa.pipe.nerc.train.lang.en.BaselineNameFinderTrainer(trainSet, testSet, lang, beamsize, corpusFormat, neTypes);
      }
      else if (lang.equalsIgnoreCase("es")) {
        nercTrainer = new es.ehu.si.ixa.pipe.nerc.train.lang.es.BaselineNameFinderTrainer(trainSet, testSet, lang, beamsize, corpusFormat, neTypes);
      }
      else if (lang.equalsIgnoreCase("it")) {
        nercTrainer = new es.ehu.si.ixa.pipe.nerc.train.lang.it.BaselineNameFinderTrainer(trainSet, testSet, lang, beamsize, corpusFormat, neTypes);
      }
      else if(lang.equalsIgnoreCase("nl")) {
        nercTrainer = new es.ehu.si.ixa.pipe.nerc.train.lang.nl.BaselineNameFinderTrainer(trainSet, testSet, lang, beamsize, corpusFormat, neTypes);
      }
    
    } else if (features.equalsIgnoreCase("dict")) {
      if (dictPath != null) {
        Dictionaries dictionaries = new Dictionaries(dictPath);
        if (lang.equalsIgnoreCase("de")) {
          nercTrainer = new es.ehu.si.ixa.pipe.nerc.train.lang.de.DictNameFinderTrainer(dictionaries, trainSet, testSet,
              lang, beamsize, corpusFormat, neTypes);
        }
        else if (lang.equalsIgnoreCase("en")) {
          nercTrainer = new es.ehu.si.ixa.pipe.nerc.train.lang.en.DictNameFinderTrainer(dictionaries, trainSet, testSet,
              lang, beamsize, corpusFormat, neTypes);
        }
        else if (lang.equalsIgnoreCase("es")) {
          nercTrainer = new es.ehu.si.ixa.pipe.nerc.train.lang.es.DictNameFinderTrainer(dictionaries, trainSet, testSet,
              lang, beamsize, corpusFormat, neTypes);
        }
        else if (lang.equalsIgnoreCase("it")) {
          nercTrainer = new es.ehu.si.ixa.pipe.nerc.train.lang.it.DictNameFinderTrainer(dictionaries, trainSet, testSet,
              lang, beamsize, corpusFormat, neTypes);
        }
        else if (lang.equalsIgnoreCase("nl")) {
          nercTrainer = new es.ehu.si.ixa.pipe.nerc.train.lang.nl.DictNameFinderTrainer(dictionaries, trainSet, testSet,
              lang, beamsize, corpusFormat, neTypes);
        }
        
      } else {
        System.err
            .println("You need to provide the directory containing the dictionaries!\n");
        System.exit(1);
      }
    }
    return nercTrainer;
  }

}
