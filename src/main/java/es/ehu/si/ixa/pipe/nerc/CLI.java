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
import java.util.Properties;

import net.sourceforge.argparse4j.ArgumentParsers;
import net.sourceforge.argparse4j.inf.ArgumentParser;
import net.sourceforge.argparse4j.inf.ArgumentParserException;
import net.sourceforge.argparse4j.inf.Namespace;
import net.sourceforge.argparse4j.inf.Subparser;
import net.sourceforge.argparse4j.inf.Subparsers;
import opennlp.tools.util.TrainingParameters;

import org.apache.commons.io.FilenameUtils;
import org.jdom2.JDOMException;

import es.ehu.si.ixa.pipe.nerc.eval.CorpusEvaluate;
import es.ehu.si.ixa.pipe.nerc.eval.Evaluate;
import es.ehu.si.ixa.pipe.nerc.train.FixedTrainer;
import es.ehu.si.ixa.pipe.nerc.train.InputOutputUtils;
import es.ehu.si.ixa.pipe.nerc.train.NameModel;
import es.ehu.si.ixa.pipe.nerc.train.Trainer;

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
  public static final String DEFAULT_EVALUATE_MODEL = "off";
  public static final String DEFAULT_NE_TYPES = "off";
  public static final String DEFAULT_FEATURES = "baseline";
  public static final String DEFAULT_LEXER = "off";
  public static final String DEFAULT_DICT_OPTION = "off";
  public static final String DEFAULT_DICT_PATH = "off";

  /**
   * Construct a CLI object with the three sub-parsers to manage the command
   * line parameters.
   */
  public CLI() {
    annotateParser = subParsers.addParser("tag").help("Tagging CLI");
    loadAnnotateParameters();
    trainParser = subParsers.addParser("train").help("Training CLI");
    loadTrainingParameters();
    evalParser = subParsers.addParser("eval").help("Evaluation CLI");
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

    String model = parsedArguments.getString("model");
    String dictionariesOption = parsedArguments.getString("dictionaries");
    String lexer = parsedArguments.getString("lexer");
    String dictPath = parsedArguments.getString("dictPath");
    // load training parameters file
    String paramFile = parsedArguments.getString("params");
    TrainingParameters params = InputOutputUtils
        .loadTrainingParameters(paramFile);
    BufferedReader breader = new BufferedReader(new InputStreamReader(
        inputStream, "UTF-8"));
    BufferedWriter bwriter = new BufferedWriter(new OutputStreamWriter(
        outputStream, "UTF-8"));
    // read KAF document from inputstream
    KAFDocument kaf = KAFDocument.createFromStream(breader);
    // language parameter
    String lang;
    if (parsedArguments.get("lang") == null) {
      lang = kaf.getLang();
    } else {
      lang = parsedArguments.get("lang");
    }
    KAFDocument.LinguisticProcessor newLp = kaf.addLinguisticProcessor(
        "entities", "ixa-pipe-nerc-" + lang + "-" + model, version);
    newLp.setBeginTimestamp();
    Properties properties = setAnnotateProperties(lang, model, dictionariesOption,
        dictPath, lexer);
    Annotate annotator = new Annotate(properties, params);
    annotator.annotateNEsToKAF(kaf);
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

    String trainSet = parsedArguments.getString("trainSet");
    String devSet = parsedArguments.getString("devSet");
    String testSet = parsedArguments.getString("testSet");
    String trainMethod = parsedArguments.getString("trainMethod");
    String dictPath = parsedArguments.getString("dictPath");
    String outModel = null;
    // load training parameters file
    String paramFile = parsedArguments.getString("params");
    TrainingParameters params = InputOutputUtils
        .loadTrainingParameters(paramFile);

    if (parsedArguments.get("output") != null) {
      outModel = parsedArguments.getString("output");
    } else {
      outModel = FilenameUtils.removeExtension(trainSet) + "-"
          + trainMethod + "-model"
          + ".bin";
    }
    Properties props = setTrainProperties(dictPath, trainMethod);
    Trainer nercTrainer = chooseTrainer(trainSet, testSet, props, params);
    String evalParam = params.getSettings().get("CrossEval");
    String[] evalRange = evalParam.split("[ :-]");
    NameModel trainedModel = null;
    if (evalRange.length == 2) {
      if (parsedArguments.get("devSet") == null) {
        InputOutputUtils.devSetException();
      } else {
        trainedModel = nercTrainer.trainCrossEval(devSet, params, evalRange);
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

    String dictPath = parsedArguments.getString("dictPath");
    String model = parsedArguments.getString("model");
    String testSet = parsedArguments.getString("testSet");
    String predFile = parsedArguments.getString("prediction");
    // load training parameters file
    String paramFile = parsedArguments.getString("params");
    TrainingParameters params = InputOutputUtils
        .loadTrainingParameters(paramFile);
    Properties properties = null;
    if (!parsedArguments.getString("model").equals(DEFAULT_EVALUATE_MODEL)) {
      properties = setEvaluateProperties(testSet, model, dictPath);
      Evaluate evaluator = new Evaluate(properties, params);
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
      CorpusEvaluate corpusEvaluator = new CorpusEvaluate(predFile, properties);
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
    annotateParser.addArgument("-p", "--params").required(true)
        .help("Load the parameters file\n");
    annotateParser.addArgument("-l", "--lang").required(false).choices("de","en","es","it","nl").help("choose language for annotation\n");
    annotateParser.addArgument("-m", "--model").required(false)
        .setDefault(DEFAULT_EVALUATE_MODEL)
        .help("Choose model to perform NERC annotation\n");
    annotateParser
        .addArgument("-d", "--dictionaries")
        .choices("tag", "post")
        .setDefault(DEFAULT_DICT_OPTION)
        .required(false)
        .help(
            "Use gazetteers directly for tagging or "
                + "for post-processing the probabilistic NERC output\n");
    annotateParser
        .addArgument("--dictPath")
        .setDefault(DEFAULT_DICT_PATH)
        .required(false)
        .help(
            "Path to the dictionaries if -d or -f dict options (or both) are chosen\n");
    annotateParser.addArgument("--lexer").choices("numeric")
        .setDefault(DEFAULT_LEXER).required(false)
        .help("Use lexer rules for NERC tagging\n");
  }

  /**
   * Create the main parameters available for training NERC models.
   */
  private void loadTrainingParameters() {
    trainParser
        .addArgument("-m", "--trainMethod")
        .choices("fixed", "optimized")
        .required(true)
        .help(
            "Uses features as specified in trainParams.txt file or will try to optimize the variables values\n");
    trainParser
        .addArgument("--dictPath")
        .setDefault(DEFAULT_DICT_PATH)
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
    evalParser.addArgument("-p", "--params").required(true)
    .help("Load the parameters file\n");
    evalParser.addArgument("-m", "--model").required(false)
        .setDefault(DEFAULT_EVALUATE_MODEL)
        .help("Choose model or prediction file\n");
    evalParser
        .addArgument("--dictPath")
        .required(false)
        .setDefault(DEFAULT_DICT_PATH)
        .help(
            "Path to the gazetteers for evaluation if dict features are used\n");
    evalParser.addArgument("-t", "--testSet").required(true)
        .help("Input testset for evaluation\n");
    evalParser
        .addArgument("--prediction")
        .required(false)
        .help(
            "Use this parameter to evaluate one prediction corpus against a reference corpus\n");
    evalParser.addArgument("--evalReport").required(false)
        .choices("brief", "detailed", "error");
  }

  /**
   * Choose the NameFinder training according to training method.
   * 
   * @return the name finder trainer
   * @throws IOException
   *           throws
   */
  private Trainer chooseTrainer(String trainSet, String testSet,
      Properties props, TrainingParameters params) throws IOException {
    Trainer nercTrainer = null;
    if (props.getProperty("trainMethod").equalsIgnoreCase("fixed")) {
      nercTrainer = new FixedTrainer(props, trainSet, testSet, params);
    } else if (props.getProperty("trainMethod").equalsIgnoreCase("optimized")) {
      nercTrainer = new FixedTrainer(props, trainSet, testSet, params);
    } else {
      System.err
          .println("You need to provide the directory containing the dictionaries!\n");
      System.exit(1);
    }
    return nercTrainer;
  }

  private Properties setAnnotateProperties(String lang, String model, String dictOption,
      String dictPath, String ruleBasedOption) {
    Properties annotateProperties = new Properties();
    annotateProperties.setProperty("lang", lang);
    annotateProperties.setProperty("model", model);
    annotateProperties.setProperty("dictOption", dictOption);
    annotateProperties.setProperty("dictPath", dictPath);
    annotateProperties.setProperty("ruleBasedOption", ruleBasedOption);
    return annotateProperties;
  }

  private Properties setTrainProperties(String dictPath, String trainMethod) {
    Properties trainProperties = new Properties();
    trainProperties.setProperty("dictPath", dictPath);
    trainProperties.setProperty("trainMethod", trainMethod);
    return trainProperties;
  }

  private Properties setEvaluateProperties(String testSet, String model, String dictPath) {
    Properties evaluateProperties = new Properties();
    evaluateProperties.setProperty("testSet", testSet);
    evaluateProperties.setProperty("model", model);
    evaluateProperties.setProperty("dictPath", dictPath);
    return evaluateProperties;
  }

}
