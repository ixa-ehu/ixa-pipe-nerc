package es.ehu.si.ixa.pipe.nerc.eval;

import opennlp.tools.util.Span;
import opennlp.tools.util.eval.Evaluator;
import opennlp.tools.util.eval.FMeasure;
import es.ehu.si.ixa.pipe.nerc.formats.CorpusSample;
import es.ehu.si.ixa.pipe.nerc.train.NameClassifier;

public class NameFinderEvaluator extends Evaluator<CorpusSample> {

  private FMeasure fmeasure = new FMeasure();
  
  /**
   * The {@link TokenNameFinder} used to create the predicted
   * {@link NameSample} objects.
   */
  private NameClassifier nameFinder;
  
  /**
   * Initializes the current instance with the given
   * {@link TokenNameFinder}.
   *
   * @param nameFinder the {@link TokenNameFinder} to evaluate.
   * @param listeners evaluation sample listeners 
   */
  public NameFinderEvaluator(NameClassifier nameFinder, NameFinderEvaluationMonitor ... listeners) {
    super(listeners);
    this.nameFinder = nameFinder;
  }

  /**
   * Evaluates the given reference {@link NameSample} object.
   *
   * This is done by finding the names with the
   * {@link TokenNameFinder} in the sentence from the reference
   * {@link NameSample}. The found names are then used to
   * calculate and update the scores.
   *
   * @param reference the reference {@link NameSample}.
   * 
   * @return the predicted {@link NameSample}.
   */
  @Override
  protected CorpusSample processSample(CorpusSample reference) {
    
    if (reference.isClearAdaptiveDataSet()) {
      nameFinder.clearAdaptiveData();
    }
    
    Span predictedNames[] = nameFinder.find(reference.getSentence());    
    Span references[] = reference.getNames();

    // OPENNLP-396 When evaluating with a file in the old format
    // the type of the span is null, but must be set to default to match
    // the output of the name finder.
    for (int i = 0; i < references.length; i++) {
      if (references[i].getType() == null) {
        references[i] = new Span(references[i].getStart(), references[i].getEnd(), "default");
      }
    }
    
    fmeasure.updateScores(references, predictedNames);
    
    return new CorpusSample(reference.getSentence(), predictedNames, reference.isClearAdaptiveDataSet());
  }
  
  public FMeasure getFMeasure() {
    return fmeasure;
  }
  
}

