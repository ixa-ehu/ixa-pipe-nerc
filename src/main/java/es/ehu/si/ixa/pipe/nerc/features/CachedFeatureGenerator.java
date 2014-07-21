package es.ehu.si.ixa.pipe.nerc.features;

import java.util.ArrayList;
import java.util.List;

import opennlp.tools.util.Cache;

/**
 * Caches features of the aggregated {@link AdaptiveFeatureGenerator}s.
 */
public class CachedFeatureGenerator implements AdaptiveFeatureGenerator {

  private final AdaptiveFeatureGenerator generator;

  private String[] prevTokens;

  private Cache contextsCache;

  private long numberOfCacheHits;
  private long numberOfCacheMisses;

  public CachedFeatureGenerator(AdaptiveFeatureGenerator... generators) {
    this.generator = new AggregatedFeatureGenerator(generators);
    contextsCache = new Cache(100);
  }

  @SuppressWarnings("unchecked")
  public void createFeatures(List<String> features, String[] tokens, int index,
      String[] previousOutcomes) {

    List<String> cacheFeatures;

    if (tokens == prevTokens) {
      cacheFeatures = (List<String>) contextsCache.get(index);

      if (cacheFeatures != null) {
        numberOfCacheHits++;
        features.addAll(cacheFeatures);
        return;
      }

    } else {
      contextsCache.clear();
      prevTokens = tokens;
    }

    cacheFeatures = new ArrayList<String>();

    numberOfCacheMisses++;

    generator.createFeatures(cacheFeatures, tokens, index, previousOutcomes);

    contextsCache.put(index, cacheFeatures);
    features.addAll(cacheFeatures);
  }

  public void updateAdaptiveData(String[] tokens, String[] outcomes) {
    generator.updateAdaptiveData(tokens, outcomes);
  }

  public void clearAdaptiveData() {
    generator.clearAdaptiveData();
  }

  /**
   * Retrieves the number of times a cache hit occurred.
   *
   * @return number of cache hits
   */
  public long getNumberOfCacheHits() {
    return numberOfCacheHits;
  }

  /**
   * Retrieves the number of times a cache miss occurred.
   *
   * @return number of cache misses
   */
  public long getNumberOfCacheMisses() {
    return numberOfCacheMisses;
  }

  @Override
  public String toString() {
    return super.toString()+": hits=" + numberOfCacheHits+" misses="+ numberOfCacheMisses+" hit%"+ (numberOfCacheHits > 0 ?
        (double) numberOfCacheHits/(numberOfCacheMisses+numberOfCacheHits) : 0);
  }
}
