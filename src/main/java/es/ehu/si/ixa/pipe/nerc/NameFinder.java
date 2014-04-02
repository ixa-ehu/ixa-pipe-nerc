package es.ehu.si.ixa.pipe.nerc;

import java.util.List;

import opennlp.tools.util.Span;

public interface NameFinder {
  
  /** Generates {@link Name} objects for the given sequence, typically a sentence
   * @param tokens an array of the tokens or words of the sequence, typically a sentence.
   * @return a list of names
   */
  public List<Name> getNames(String[] tokens);
  
  /**
   * This method receives as input an array of tokenized text
   * and returns the {@link Span}s of the detected and classified Named Entities. 
   * 
   * @param tokens
   *          an array of tokenized text
   * @return an list of Spans of Named Entities
   */
  public List<Span> nercToSpans(String[] tokens);
  
  /**
   * Create a list of {@link Name} objects from spans and tokens
   * 
   * @param neSpans
   * @param tokens
   * @return a list of name objects
   */
  public List<Name> getNamesFromSpans(Span[] neSpans, String[] tokens);
  
  /**
   * Forget all adaptive data which was collected during previous
   * calls to one of the find methods.
   *
   * This method is typically called at the end of a document.
   */
  public void clearAdaptiveData();

}
