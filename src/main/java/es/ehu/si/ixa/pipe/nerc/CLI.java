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
import java.io.File;
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
import opennlp.tools.cmdline.CmdLineUtil;
import opennlp.tools.namefind.TokenNameFinderModel;
import opennlp.tools.util.TrainingParameters;

import org.jdom2.JDOMException;

import com.google.common.io.Files;

import es.ehu.si.ixa.pipe.nerc.eval.CorpusEvaluate;
import es.ehu.si.ixa.pipe.nerc.eval.Evaluate;
import es.ehu.si.ixa.pipe.nerc.train.FixedTrainer;
import es.ehu.si.ixa.pipe.nerc.train.Flags;
import es.ehu.si.ixa.pipe.nerc.train.InputOutputUtils;
import es.ehu.si.ixa.pipe.nerc.train.Trainer;

/**
 * Main class of ixa-pipe-nerc, the ixa pipes (ixa2.si.ehu.es/ixa-pipes) NERC
 * tagger.
 * 
 * @author ragerri
 * @version 2014-10-15
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
   * @throws JDOMException 
   */
  public final void parseCLI(final String[] args) throws IOException, JDOMException {
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
      final OutputStream outputStream) throws IOException, JDOMException {

    BufferedReader breader = new BufferedReader(new InputStreamReader(
        inputStream, "UTF-8"));
    BufferedWriter bwriter = new BufferedWriter(new OutputStreamWriter(
        outputStream, "UTF-8"));
    // read KAF document from inputstream
    KAFDocument kaf = KAFDocument.createFromStream(breader);
    // load parameters into a properties
    String model = parsedArguments.getString("model");
    String outputFormat = parsedArguments.getString("outputFormat");
    String lexer = parsedArguments.getString("lexer");
    String dictTag = parsedArguments.getString("dictTag");
    String dictPath = parsedArguments.getString("dictPath");
    // language parameter
    String lang = null;
    if (parsedArguments.getString("language") != null) {
      lang = parsedArguments.getString("language");
      if (!kaf.getLang().equalsIgnoreCase(lang)) {
        System.err
            .println("Language parameter in NAF and CLI do not match!!");
        System.exit(1);
      }
    } else {
      lang = kaf.getLang();
    }
    Properties properties = setAnnotateProperties(model, lang, lexer, dictTag, dictPath);
    KAFDocument.LinguisticProcessor newLp = kaf.addLinguisticProcessor(
        "entities", "ixa-pipe-nerc-" + lang + "-" + Files.getNameWithoutExtension(model), version);
    newLp.setBeginTimestamp();
    Annotate annotator = new Annotate(properties);
    annotator.annotateNEs(kaf);
    newLp.setEndTimestamp();
    String kafToString = null;
    if (outputFormat.equalsIgnoreCase("conll03")) {
      kafToString = annotator.annotateNEsToCoNLL2003(kaf);
    } else if (outputFormat.equalsIgnoreCase("conll02")) {
      kafToString = annotator.annotateNEsToCoNLL2002(kaf);
    } else if (outputFormat.equalsIgnoreCase("opennlp")) {
      kafToString = annotator.annotateNEsToOpenNLP(kaf);
    } else {
      kafToString = annotator.annotateNEsToKAF(kaf);
    }
    bwriter.write(kafToString);
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

    // load training parameters file
    String paramFile = parsedArguments.getString("params");
    TrainingParameters params = InputOutputUtils
        .loadTrainingParameters(paramFile);
    String outModel = null;
    if (params.getSettings().get("OutputModel") == null || params.getSettings().get("OutputModel").length() == 0) {
      outModel = Files.getNameWithoutExtension(paramFile) + ".bin";
      params.put("OutputModel", outModel);
    }
    else {
      outModel = Flags.getModel(params);
    }
    String trainSet = Flags.getDataSet("TrainSet", params);
    String testSet = Flags.getDataSet("TestSet", params);
    Trainer nercTrainer = new FixedTrainer(trainSet, testSet, params);
    TokenNameFinderModel trainedModel = null;
    // check if CrossEval
    if (params.getSettings().get("CrossEval") != null) {
      String evalParam = params.getSettings().get("CrossEval");
      String[] evalRange = evalParam.split("[ :-]");
      if (evalRange.length != 2) {
        Flags.devSetException();
      } else {
        if (params.getSettings().get("DevSet") != null) {
          String devSet = params.getSettings().get("DevSet");
          trainedModel = nercTrainer.trainCrossEval(devSet, params, evalRange);
        } else {
          Flags.devSetException();
        }
      }
    } else {
      trainedModel = nercTrainer.train(params);
    }
    CmdLineUtil.writeModel("ixa-pipe-nerc ", new File(outModel), trainedModel);
    System.err.println();
    System.err.println("Wrote trained NERC model to " + outModel);
  }

  /**
   * Main evaluation entry point.
   * 
   * @throws IOException
   *           throws exception if test set not available
   */
  public final void eval() throws IOException {

    String lang = parsedArguments.getString("language");
    String model = parsedArguments.getString("model");
    String testset = parsedArguments.getString("testset");
    String corpusFormat = parsedArguments.getString("corpusFormat");
    Properties props = setEvalProperties(lang, model, testset, corpusFormat);
    
    if (parsedArguments.getString("prediction") == null) {
      Evaluate evaluator = new Evaluate(props);
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
      String predFile = parsedArguments.getString("prediction");
      CorpusEvaluate corpusEvaluator = new CorpusEvaluate(predFile, props);
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
    
    annotateParser.addArgument("-m", "--model")
        .required(true)
        .help("Pass the model to do the tagging as a parameter.\n");
    annotateParser.addArgument("-l","--language")
        .required(false)
        .choices("de", "en", "es", "eu", "it", "nl")
        .help("Choose language; it defaults to the language value in incoming NAF file.\n");
    annotateParser.addArgument("-o","--outputFormat")
        .required(false)
        .choices("conll03", "conll02", "naf", "opennlp")
        .setDefault(Flags.DEFAULT_OUTPUT_FORMAT)
        .help("Choose output format; it defaults to NAF.\n");
    annotateParser.addArgument("--lexer")
        .choices("numeric")
        .setDefault(Flags.DEFAULT_LEXER)
        .required(false)
        .help("Use lexer rules for NERC tagging; it defaults to false.\n");
    annotateParser.addArgument("--dictTag")
        .required(false)
        .choices("tag", "post")
        .setDefault(Flags.DEFAULT_DICT_OPTION)
        .help("Choose to directly tag entities by dictionary look-up; if the 'tag' option is chosen, " +
        		"only tags entities found in the dictionary; if 'post' option is chosen, it will " +
        		"post-process the results of the statistical model.\n");
    annotateParser.addArgument("--dictPath")
        .required(false)
        .setDefault(Flags.DEFAULT_DICT_PATH)
        .help("Provide the path to the dictionaries for direct dictionary tagging; it ONLY WORKS if --dictTag " +
        		"option is activated.\n");
  }

  /**
   * Create the main parameters available for training NERC models.
   */
  private void loadTrainingParameters() {
    trainParser.addArgument("-p", "--params").required(true)
        .help("Load the training parameters file\n");
  }

  /**
   * Create the parameters available for evaluation.
   */
  private void loadEvalParameters() {
    evalParser.addArgument("-l", "--language")
        .required(true)
        .choices("de", "en", "es", "eu", "it", "nl")
        .help("Choose language.\n");
    evalParser.addArgument("-m", "--model")
        .required(false)
        .setDefault(Flags.DEFAULT_EVALUATE_MODEL)
        .help("Pass the model to evaluate as a parameter.\n");
    evalParser.addArgument("-t", "--testset")
        .required(true)
        .help("The test or reference corpus.\n");
    evalParser.addArgument("-f","--corpusFormat")
        .required(false)
        .choices("conll03", "conll02", "opennlp")
        .setDefault(Flags.DEFAULT_EVAL_FORMAT)
        .help("Choose format of reference corpus; it defaults to opennlp format.\n");
    evalParser
        .addArgument("--prediction")
        .required(false)
        .help(
            "Use this parameter to evaluate one prediction corpus against the reference corpus.\n");
    evalParser.addArgument("--evalReport")
        .required(false)
        .choices("brief", "detailed", "error")
        .help("Choose level of detail of evaluation report; it defaults to detailed evaluation.\n");
  }

  /**
   * Set a Properties object with the CLI parameters for annotation.
   * @param model the model parameter
   * @param language language parameter
   * @param lexer rule based parameter
   * @param dictTag directly tag from a dictionary
   * @param dictPath directory to the dictionaries
   * @return the properties object
   */
  private Properties setAnnotateProperties(String model, String language, String lexer, String dictTag, String dictPath) {
    Properties annotateProperties = new Properties();
    annotateProperties.setProperty("model", model);
    annotateProperties.setProperty("language", language);
    annotateProperties.setProperty("ruleBasedOption", lexer);
    annotateProperties.setProperty("dictTag", dictTag);
    annotateProperties.setProperty("dictPath", dictPath);
    return annotateProperties;
  }
  
  /**
   * Set a Properties object with the CLI parameters for evaluation.
   * @param model the model parameter
   * @param testset the reference set
   * @param corpusFormat the format of the testset
   * @return the properties object
   */
  private Properties setEvalProperties(String language, String model, String testset, String corpusFormat) {
    Properties evalProperties = new Properties();
    evalProperties.setProperty("language", language);
    evalProperties.setProperty("model", model);
    evalProperties.setProperty("testset", testset);
    evalProperties.setProperty("corpusFormat", corpusFormat);
    return evalProperties;
  }

}
