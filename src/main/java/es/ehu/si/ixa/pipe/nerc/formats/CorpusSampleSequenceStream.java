package es.ehu.si.ixa.pipe.nerc.formats;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import es.ehu.si.ixa.pipe.nerc.features.AdaptiveFeatureGenerator;
import es.ehu.si.ixa.pipe.nerc.features.NameContextGenerator;
import es.ehu.si.ixa.pipe.nerc.train.DefaultNameContextGenerator;
import es.ehu.si.ixa.pipe.nerc.train.NameClassifier;
import es.ehu.si.ixa.pipe.nerc.train.NameFinderEventStream;
import es.ehu.si.ixa.pipe.nerc.train.NameModel;

import opennlp.model.AbstractModel;
import opennlp.model.Event;
import opennlp.model.Sequence;
import opennlp.model.SequenceStream;
import opennlp.tools.util.ObjectStream;

public class CorpusSampleSequenceStream implements SequenceStream {

  private NameContextGenerator pcg;
  private List<CorpusSample> samples;
  
  public CorpusSampleSequenceStream(ObjectStream<CorpusSample> psi) throws IOException {
    this(psi, new DefaultNameContextGenerator((AdaptiveFeatureGenerator) null));
  }
  
  public CorpusSampleSequenceStream(ObjectStream<CorpusSample> psi, AdaptiveFeatureGenerator featureGen) 
  throws IOException {
    this(psi, new DefaultNameContextGenerator(featureGen));
  }
  
  public CorpusSampleSequenceStream(ObjectStream<CorpusSample> psi, NameContextGenerator pcg)
      throws IOException {
    samples = new ArrayList<CorpusSample>();
    
    CorpusSample sample;
    while((sample = psi.read()) != null) {
      samples.add(sample);
    }
    
    System.err.println("Got "+samples.size()+" sequences");
    
    this.pcg = pcg;
  }
  
  
  @SuppressWarnings("unchecked")
  public Event[] updateContext(Sequence sequence, AbstractModel model) {
    Sequence<CorpusSample> pss = sequence;
    NameClassifier tagger = new NameClassifier(new NameModel("x-unspecified", model, Collections.<String, Object>emptyMap(), null));
    String[] sentence = pss.getSource().getSentence();
    String[] tags = NameFinderEventStream.generateOutcomes(tagger.find(sentence), null, sentence.length);
    Event[] events = new Event[sentence.length];
    
    NameFinderEventStream.generateEvents(sentence,tags,pcg).toArray(events);
    
    return events;
  }
  
  @SuppressWarnings("unchecked")
  public Iterator<Sequence> iterator() {
    return new CorpusSampleSequenceIterator(samples.iterator());
  }

}

class CorpusSampleSequenceIterator implements Iterator<Sequence> {

  private Iterator<CorpusSample> psi;
  private NameContextGenerator cg;
  
  public CorpusSampleSequenceIterator(Iterator<CorpusSample> psi) {
    this.psi = psi;
    cg = new DefaultNameContextGenerator(null);
  }
  
  public boolean hasNext() {
    return psi.hasNext();
  }

  public Sequence<CorpusSample> next() {
    CorpusSample sample = psi.next();
    
    String sentence[] = sample.getSentence();
    String tags[] = NameFinderEventStream.generateOutcomes(sample.getNames(), null, sentence.length);
    Event[] events = new Event[sentence.length];
    
    for (int i=0; i < sentence.length; i++) {

      // it is safe to pass the tags as previous tags because
      // the context generator does not look for non predicted tags
      String[] context = cg.getContext(i, sentence, tags, null);

      events[i] = new Event(tags[i], context);
    }
    Sequence<CorpusSample> sequence = new Sequence<CorpusSample>(events,sample);
    return sequence;
  }

  public void remove() {
    throw new UnsupportedOperationException();
  }
  
}
