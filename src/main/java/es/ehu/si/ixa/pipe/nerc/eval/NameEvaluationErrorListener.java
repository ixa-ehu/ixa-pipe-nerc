package es.ehu.si.ixa.pipe.nerc.eval;

import java.io.OutputStream;

import es.ehu.si.ixa.pipe.nerc.formats.CorpusSample;

import opennlp.tools.cmdline.EvaluationErrorPrinter;

/**
 * A default implementation of {@link EvaluationMonitor} that prints
 * to an output stream.
 * 
 */
public class NameEvaluationErrorListener extends
    EvaluationErrorPrinter<CorpusSample> implements NameFinderEvaluationMonitor {

  /**
   * Creates a listener that will print to System.err
   */
  public NameEvaluationErrorListener() {
    super(System.err);
  }

  /**
   * Creates a listener that will print to a given {@link OutputStream}
   */
  public NameEvaluationErrorListener(OutputStream outputStream) {
    super(outputStream);
  }

  @Override
  public void missclassified(CorpusSample reference, CorpusSample prediction) {
    printError(reference.getNames(), prediction.getNames(), reference,
        prediction, reference.getSentence());
  }

}
