package es.ehu.si.ixa.pipe.nerc.formats;

import java.io.IOException;
import java.util.Collections;

import opennlp.tools.ml.model.AbstractModel;
import opennlp.tools.ml.model.Event;
import opennlp.tools.ml.model.Sequence;
import opennlp.tools.ml.model.SequenceStream;
import opennlp.tools.namefind.BioCodec;
import opennlp.tools.util.ObjectStream;
import opennlp.tools.util.SequenceCodec;
import es.ehu.si.ixa.pipe.nerc.features.AdaptiveFeatureGenerator;
import es.ehu.si.ixa.pipe.nerc.features.NameContextGenerator;
import es.ehu.si.ixa.pipe.nerc.train.DefaultNameContextGenerator;
import es.ehu.si.ixa.pipe.nerc.train.NameClassifier;
import es.ehu.si.ixa.pipe.nerc.train.NameFinderEventStream;
import es.ehu.si.ixa.pipe.nerc.train.NameModel;

public class CorpusSampleSequenceStream implements SequenceStream {
  private NameContextGenerator pcg;
  private final boolean useOutcomes;
  private ObjectStream<CorpusSample> psi;
  private SequenceCodec<String> seqCodec;

  public CorpusSampleSequenceStream(ObjectStream<CorpusSample> psi) throws IOException {
    this(psi, new DefaultNameContextGenerator((AdaptiveFeatureGenerator) null), true);
  }

  public CorpusSampleSequenceStream(ObjectStream<CorpusSample> psi, AdaptiveFeatureGenerator featureGen)
  throws IOException {
    this(psi, new DefaultNameContextGenerator(featureGen), true);
  }

  public CorpusSampleSequenceStream(ObjectStream<CorpusSample> psi, AdaptiveFeatureGenerator featureGen, boolean useOutcomes)
  throws IOException {
    this(psi, new DefaultNameContextGenerator(featureGen), useOutcomes);
  }

  public CorpusSampleSequenceStream(ObjectStream<CorpusSample> psi, NameContextGenerator pcg)
      throws IOException {
    this(psi, pcg, true);
  }

  public CorpusSampleSequenceStream(ObjectStream<CorpusSample> psi, NameContextGenerator pcg, boolean useOutcomes)
      throws IOException {
    this(psi, pcg, useOutcomes, new BioCodec());
  }

  public CorpusSampleSequenceStream(ObjectStream<CorpusSample> psi, NameContextGenerator pcg, boolean useOutcomes,
      SequenceCodec<String> seqCodec)
      throws IOException {
    this.psi = psi;
    this.useOutcomes = useOutcomes;
    this.pcg = pcg;
    this.seqCodec = seqCodec;
  }

  @SuppressWarnings("unchecked")
  public Event[] updateContext(Sequence sequence, AbstractModel model) {
    Sequence<CorpusSample> pss = sequence;
    NameClassifier tagger = new NameClassifier(new NameModel("x-unspecified", model, Collections.<String, Object>emptyMap(), null));
    String[] sentence = pss.getSource().getSentence();
    String[] tags = seqCodec.encode(tagger.find(sentence), sentence.length);
    Event[] events = new Event[sentence.length];

    NameFinderEventStream.generateEvents(sentence,tags,pcg).toArray(events);

    return events;
  }

  @Override
  public Sequence read() throws IOException {
    CorpusSample sample = psi.read();
    if (sample != null) {
      String sentence[] = sample.getSentence();
      String tags[] = seqCodec.encode(sample.getNames(), sentence.length);
      Event[] events = new Event[sentence.length];

      for (int i=0; i < sentence.length; i++) {

        // it is safe to pass the tags as previous tags because
        // the context generator does not look for non predicted tags
        String[] context;
        if (useOutcomes) {
          context = pcg.getContext(i, sentence, tags, null);
        }
        else {
          context = pcg.getContext(i, sentence, null, null);
        }

        events[i] = new Event(tags[i], context);
      }
      Sequence<CorpusSample> sequence = new Sequence<CorpusSample>(events,sample);
      return sequence;
      }
      else {
        return null;
      }
  }

  @Override
  public void reset() throws IOException, UnsupportedOperationException {
    psi.reset();
  }

  @Override
  public void close() throws IOException {
    psi.close();
  }
  
}
