/*
 * Copyright 2014 Rodrigo Agerri

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

package ixa.pipe.nerc;

import ixa.kaflib.KAFDocument;
import ixa.pipe.nerc.train.InputOutputUtils;
import ixa.pipe.nerc.train.StatisticalNameFinderTrainer;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
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

/**
 * 
 *
 * @author ragerri
 * @version 1.0
 *
 */

public class CLI {

  /**
   *
   * @param args
   * @throws IOException
   * @throws JDOMException
   */
  public static void main(String[] args) throws IOException, JDOMException {

    Namespace parsedArguments = null;
    ArgumentParser parser = ArgumentParsers
        .newArgumentParser("ixa-pipe-nerc-1.0.jar")
        .description(
            "ixa-pipe-nerc-1.0 is a multilingual NERC module developed by IXA NLP Group.\n");
    Subparsers subparsers = parser.addSubparsers().help("sub-command help");
    
    ////////////////////////
    //// Annotation CLI ////
    ////////////////////////
    
    Subparser annotateParser = subparsers.addParser("tag").help("Tagging CLI");
    annotateParser
        .addArgument("-l", "--lang")
        .choices("en", "es")
        .required(false)
        .help(
            "It is REQUIRED to choose a language to perform annotation with ixa-pipe-nerc");
    annotateParser.addArgument("-g","--gazetteers").required(false).help("Two arguments are "+
        "available: tag and post; if both are concatenated by a comma " +
        "(e.g., 'tag,post'), both options will be activated\n");
    
    //////////////////////
    //// Training CLI ////
    //////////////////////
    
    Subparser trainParser = subparsers.addParser("train").help("Training CLI");
    trainParser.addArgument("-m","--model")
        .choices("baseline","dictionaries").required(false).help("Train NERC models");
    trainParser.addArgument("-p", "--params").required(true)
        .help("load the parameters file");
    trainParser.addArgument("-i", "--input").required(true)
        .help("Input training set");
    trainParser.addArgument("-e", "--evalSet").required(true)
        .help("Input testset for evaluation");
    trainParser.addArgument("-d", "--devSet").required(false)
        .help("Input development set for cross-evaluation");
    trainParser.addArgument("-o", "--output").required(false)
        .help("choose output file to save the annotation");
    
    try {
      parsedArguments = parser.parseArgs(args);
      System.err.println("CLI options: " + parsedArguments);
    } catch (ArgumentParserException e) {
      parser.handleError(e);
      System.out
          .println("Run java -jar target/ixa-pipe-nerc-1.0.jar (tag|train) -help for details");
      System.exit(1);
    }
    
    try {
      
      if (parsedArguments.get("model") != null) {
        String trainFile = parsedArguments.getString("input");
        String testFile = parsedArguments.getString("evalSet");
        String devFile = parsedArguments.getString("devSet");
        String outModel = null;
        // load training parameters file
        String paramFile = parsedArguments.getString("params");
        TrainingParameters params = InputOutputUtils.loadTrainingParameters(paramFile);
        String lang = params.getSettings().get("Language");
        String evalParam = params.getSettings().get("CrossEval");
        String[] evalRange = evalParam.split("[ :-]");
        
        if (parsedArguments.get("output") != null) {
          outModel = parsedArguments.getString("output");
        } else {
          outModel = FilenameUtils.removeExtension(trainFile) + "-"
              + parsedArguments.get("train").toString() + "-model" + ".bin";
        }
        
        if (parsedArguments.getString("model").equalsIgnoreCase("baseline")) {
            StatisticalNameFinderTrainer nercTrainer = new StatisticalNameFinderTrainer(trainFile, testFile,lang);
            TokenNameFinderModel trainedModel = null;
            if (evalRange.length==2) {
              if (parsedArguments.get("devSet") == null) {
                InputOutputUtils.devSetException();
              } else {
                trainedModel = nercTrainer.trainCrossEval(trainFile, devFile,
                    params, evalRange);
              }
            } else {
              trainedModel = nercTrainer.train(params);
            }
            InputOutputUtils.saveModel(trainedModel, outModel);
            System.out.println();
            System.out.println("Wrote trained NERC model to " + outModel);
          }
      }
    
    ////////////////////
    //// Annotation ////
    ////////////////////
    else {
      String gazetteer = parsedArguments.getString("gazetteers");
      BufferedReader breader = new BufferedReader(new InputStreamReader(System.in, "UTF-8"));
      BufferedWriter bwriter = new BufferedWriter(new OutputStreamWriter(System.out, "UTF-8"));
      
      // read KAF document from inputstream
      KAFDocument kaf = KAFDocument.createFromStream(breader);
      // language parameter
      String lang;
      if (parsedArguments.get("lang") == null) {
	    lang = kaf.getLang();
      }
      else {
	    lang =  parsedArguments.getString("lang");
      }
      if (parsedArguments.get("gazetteers") != null) {
        Annotate annotator = new Annotate(lang,gazetteer);
        annotator.annotateNEsToKAF(kaf);
      }
      else { 
        Annotate annotator = new Annotate(lang);
        annotator.annotateNEsToKAF(kaf);
      }
      kaf.addLinguisticProcessor("entities","ixa-pipe-nerc-"+lang, "1.0");
      bwriter.write(kaf.toString());
      bwriter.close();
      breader.close();
     }
    }
      catch (IOException e) {
      e.printStackTrace();
    }

  }
}
