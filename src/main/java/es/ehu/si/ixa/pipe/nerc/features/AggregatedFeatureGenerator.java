package es.ehu.si.ixa.pipe.nerc.features;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;


public class AggregatedFeatureGenerator implements AdaptiveFeatureGenerator {

 /**
  * Contains all aggregated {@link AdaptiveFeatureGenerator}s.
  */
 private Collection<AdaptiveFeatureGenerator> generators;

 /**
  * Initializes the current instance.
  *
  * @param generators array of generators, null values are not permitted
  */
 public AggregatedFeatureGenerator(AdaptiveFeatureGenerator... generators) {

   for (AdaptiveFeatureGenerator generator : generators) {
     if (generator == null)
       throw new IllegalArgumentException("null values in generators are not permitted!");
   }
   
   this.generators = new ArrayList<AdaptiveFeatureGenerator>(generators.length);

   Collections.addAll(this.generators, generators);

   this.generators = Collections.unmodifiableCollection(this.generators);
 }

 public AggregatedFeatureGenerator(Collection<AdaptiveFeatureGenerator> generators) {
   this(generators.toArray(new AdaptiveFeatureGenerator[generators.size()]));
 }
 
 /**
  * Calls the {@link AdaptiveFeatureGenerator#clearAdaptiveData()} method
  * on all aggregated {@link AdaptiveFeatureGenerator}s.
  */
 public void clearAdaptiveData() {

   for (AdaptiveFeatureGenerator generator : generators) {
     generator.clearAdaptiveData();
   }
 }

 /**
  * Calls the {@link AdaptiveFeatureGenerator#createFeatures(List, String[], int, String[])}
  * method on all aggregated {@link AdaptiveFeatureGenerator}s.
  */
 public void createFeatures(List<String> features, String[] tokens, int index,
     String[] previousOutcomes) {

   for (AdaptiveFeatureGenerator generator : generators) {
     generator.createFeatures(features, tokens, index, previousOutcomes);
   }
 }

 /**
  * Calls the {@link AdaptiveFeatureGenerator#updateAdaptiveData(String[], String[])}
  * method on all aggregated {@link AdaptiveFeatureGenerator}s.
  */
 public void updateAdaptiveData(String[] tokens, String[] outcomes) {

   for (AdaptiveFeatureGenerator generator : generators) {
     generator.updateAdaptiveData(tokens, outcomes);
   }
 }

 /**
  * Retrieves a {@link Collections} of all aggregated
  * {@link AdaptiveFeatureGenerator}s.
  *
  * @return all aggregated generators
  */
 public Collection<AdaptiveFeatureGenerator> getGenerators() {
   return generators;
 }
}
