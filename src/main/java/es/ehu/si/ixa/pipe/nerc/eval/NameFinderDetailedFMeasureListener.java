package es.ehu.si.ixa.pipe.nerc.eval;

import es.ehu.si.ixa.pipe.nerc.train.CorpusSample;
import opennlp.tools.cmdline.DetailedFMeasureListener;
import opennlp.tools.util.Span;

public class NameFinderDetailedFMeasureListener extends
DetailedFMeasureListener<CorpusSample> implements
NameFinderEvaluationMonitor {

@Override
protected Span[] asSpanArray(CorpusSample sample) {
return sample.getNames();
}

}
