package es.ehu.si.ixa.pipe.nerc.formats;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;



import opennlp.tools.util.FilterObjectStream;
import opennlp.tools.util.ObjectStream;
import opennlp.tools.util.Span;

/**
 * A stream which removes Name Samples which do not have a certain type.
 */
public class CorpusSampleTypeFilter extends
    FilterObjectStream<CorpusSample, CorpusSample> {

  private final Set<String> types;

  public CorpusSampleTypeFilter(String[] types,
      ObjectStream<CorpusSample> samples) {
    super(samples);
    this.types = Collections.unmodifiableSet(new HashSet<String>(Arrays
        .asList(types)));
  }

  public CorpusSampleTypeFilter(Set<String> types,
      ObjectStream<CorpusSample> samples) {
    super(samples);
    this.types = Collections.unmodifiableSet(new HashSet<String>(types));
  }

  public CorpusSample read() throws IOException {

    CorpusSample sample = samples.read();

    if (sample != null) {

      List<Span> filteredNames = new ArrayList<Span>();

      for (Span name : sample.getNames()) {
        if (types.contains(name.getType())) {
          filteredNames.add(name);
        }
      }

      return new CorpusSample(sample.getSentence(),
          filteredNames.toArray(new Span[filteredNames.size()]),
          sample.isClearAdaptiveDataSet());
    } else {
      return null;
    }
  }
}
