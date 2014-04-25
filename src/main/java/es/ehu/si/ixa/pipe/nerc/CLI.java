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

import es.ehu.si.ixa.pipe.nerc.eval.Evaluate;
import es.ehu.si.ixa.pipe.nerc.train.BaselineNameFinderTrainer;
import es.ehu.si.ixa.pipe.nerc.train.DictLbjNameFinderTrainer;
import es.ehu.si.ixa.pipe.nerc.train.InputOutputUtils;
import es.ehu.si.ixa.pipe.nerc.train.NameFinderTrainer;
import es.ehu.si.ixa.pipe.nerc.train.OpenNLPDefaultTrainer;

/**
 * Main class of ixa-pipe-nerc.
 *
 * @author ragerri
 * @version 2014-04-18
 *
 */
public class CLI {

  /**
   * Get dynamically the version of ixa-pipe-nerc by looking at the MANIFEST file.
   */
  private final String version =  CLI.class.getPackage().getImplementationVersion();
  /**
   * Name space of the arguments provided at the CLI.
   */
  private Namespace parsedArguments = null;
  /**
   * Argument parser instance.
   */
  private ArgumentParser argParser = ArgumentParsers
      .newArgumentParser("ixa-pipe-nerc-" + version + ".jar")
      .description(
          "ixa-pipe-nerc-" + version + " is a multilingual NERC module developed by IXA NLP Group.\n");
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
  /**
   * Construct a CLI object with the three sub-parsers to manage
   * the command line parameters.
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
   * @throws IOException exception if input data not available
   * @throws JDOMException if problems with the xml formatting of NAF
   */
  public static void main(final String[] args) throws IOException, JDOMException {

    CLI cmdLine = new CLI();
    cmdLine.parseCLI(args);
  }

  /**
   * Parse the command interface parameters with the argParser.
   *
   * @param args
   *          the arguments passed through the CLI
   * @throws IOException exception if problems with the incoming data
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
      System.out
          .println("Run java -jar target/ixa-pipe-nerc-" + version + ".jar (tag|train|eval) -help for details");
      System.exit(1);
    }
  }

  /**
   * Main method to do Named Entity tagging.
   *
   * @param inputStream the input stream containing the content to tag
   * @param outputStream the output stream providing the named entities
   * @throws IOException exception if problems in input or output streams
   */
  public final void annotate(final InputStream inputStream, final OutputStream outputStream)
      throws IOException {

    int beamsize = parsedArguments.getInt("beamsize");
    String features = parsedArguments.getString("features");
    String gazetteer = parsedArguments.getString("gazetteers");
    String model;
    if (parsedArguments.get("model") == null) {
      model = "baseline";
    } else {
      model = parsedArguments.getString("model");
    }
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
      lang = parsedArguments.getString("lang");
    }
    KAFDocument.LinguisticProcessor newLp = kaf.addLinguisticProcessor("entities", "ixa-pipe-nerc-" + lang, version);
    newLp.setBeginTimestamp();
    if (parsedArguments.get("gazetteers") != null) {
      Annotate annotator = new Annotate(lang, gazetteer, model, features,
          beamsize);
      annotator.annotateNEsToKAF(kaf);
    } else {
      Annotate annotator = new Annotate(lang, model, features, beamsize);
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
   * @throws IOException input output exception if problems with corpora
   */
  public final void train() throws IOException {

    NameFinderTrainer nercTrainer = null;
    String trainFile = parsedArguments.getString("input");
    String testFile = parsedArguments.getString("testSet");
    String devFile = parsedArguments.getString("devSet");
    String outModel = null;
    // load training parameters file
    String paramFile = parsedArguments.getString("params");
    TrainingParameters params = InputOutputUtils
        .loadTrainingParameters(paramFile);
    String lang = params.getSettings().get("Language");
    String corpusFormat = params.getSettings().get("Corpus");
    Integer beamsize = Integer.valueOf(params.getSettings().get("Beamsize"));
    String evalParam = params.getSettings().get("CrossEval");
    String[] evalRange = evalParam.split("[ :-]");

    if (parsedArguments.get("output") != null) {
      outModel = parsedArguments.getString("output");
    } else {
      outModel = FilenameUtils.removeExtension(trainFile) + "-"
          + parsedArguments.getString("features").toString() + "-model"
          + ".bin";
    }

    if (parsedArguments.getString("features").equalsIgnoreCase("opennlp")) {
      nercTrainer = new OpenNLPDefaultTrainer(trainFile, testFile, lang,
          beamsize, corpusFormat);
    } else if (parsedArguments.getString("features").equalsIgnoreCase(
        "baseline")) {
      nercTrainer = new BaselineNameFinderTrainer(trainFile, testFile, lang,
          beamsize, corpusFormat);
    } else if (parsedArguments.getString("features")
        .equalsIgnoreCase("dictlbj")) {
      nercTrainer = new DictLbjNameFinderTrainer(trainFile, testFile, lang,
          beamsize, corpusFormat);
    }

    TokenNameFinderModel trainedModel = null;
    if (evalRange.length == 2) {
      if (parsedArguments.get("devSet") == null) {
        InputOutputUtils.devSetException();
      } else {
        trainedModel = nercTrainer.trainCrossEval(trainFile, devFile, params,
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
   * @throws IOException throws exception if test set not available
   */
  public final void eval() throws IOException {

    String testFile = parsedArguments.getString("testSet");
    String features = parsedArguments.getString("features");
    String model = parsedArguments.getString("model");
    String lang = parsedArguments.getString("language");
    int beam = parsedArguments.getInt("beamsize");
    String corpusFormat = parsedArguments.getString("corpus");

    Evaluate evaluator = new Evaluate(testFile, model, features, lang, beam,
        corpusFormat);
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
  }

  /**
   * Create the available parameters for NER tagging.
   */
  private void loadAnnotateParameters() {
    annotateParser.addArgument("-l", "--lang").choices("en", "es")
        .required(false)
        .help("Choose a language to perform annotation with ixa-pipe-nerc");
    annotateParser.addArgument("-f", "--features")
        .choices("opennlp", "baseline", "dictlbj")
        .required(false).setDefault("baseline")
        .help("Choose features for NERC; it defaults to baseline");
    annotateParser.addArgument("-m", "--model").required(false)
        .help("Choose model to perform NERC annotation");
    annotateParser
        .addArgument("--beamsize")
        .setDefault(DEFAULT_BEAM_SIZE)
        .type(Integer.class)
        .help(
            "Choose beam size for decoding: 1 is faster and amounts to greedy search");
    annotateParser
        .addArgument("-g", "--gazetteers")
        .choices("tag", "post")
        .required(false)
        .help(
            "Use gazetteers directly for tagging or "
                + "for post-processing the probabilistic NERC output.\n");
  }

  /**
   * Create the main parameters available for training NERC models.
   */
  private void loadTrainingParameters() {
    trainParser.addArgument("-f", "--features")
        .choices("opennlp", "baseline", "dictlbj")
        .required(true).help("Choose features to train NERC model");
    trainParser.addArgument("-p", "--params").required(true)
        .help("Load the parameters file");
    trainParser.addArgument("-i", "--input").required(true)
        .help("Input training set");
    trainParser.addArgument("-t", "--testSet").required(true)
        .help("Input testset for evaluation");
    trainParser.addArgument("-d", "--devSet").required(false)
        .help("Input development set for cross-evaluation");
    trainParser.addArgument("-o", "--output").required(false)
        .help("Choose output file to save the annotation");
  }

  /**
   * Create the parameters available for evaluation.
   */
  private void loadEvalParameters() {
    evalParser.addArgument("-m", "--model").required(true).help("Choose model");
    evalParser.addArgument("-f", "--features")
        .choices("opennlp", "baseline", "dictlbj")
        .required(true).help("Choose features for evaluation");
    evalParser.addArgument("-l", "--language").required(true)
        .choices("en", "es")
        .help("Choose language to load model for evaluation");
    evalParser.addArgument("-t", "--testSet").required(true)
        .help("Input testset for evaluation");
    evalParser.addArgument("--evalReport").required(false)
        .choices("brief", "detailed", "error")
        .help("Choose type of evaluation report; defaults to detailed");
    evalParser.addArgument("-c", "--corpus").setDefault("opennlp")
        .choices("conll", "opennlp").help("choose format input of corpus");
    evalParser
        .addArgument("--beamsize")
        .setDefault(DEFAULT_BEAM_SIZE)
        .type(Integer.class)
        .help(
            "Choose beam size for evaluation: 1 is faster and amounts to greedy search");
  }

}
