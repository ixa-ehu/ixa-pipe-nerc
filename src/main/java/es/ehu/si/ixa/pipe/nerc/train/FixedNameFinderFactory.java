package es.ehu.si.ixa.pipe.nerc.train;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import es.ehu.si.ixa.pipe.nerc.features.GeneratorFactory;


import opennlp.tools.namefind.TokenNameFinderFactory;
import opennlp.tools.util.InvalidFormatException;
import opennlp.tools.util.featuregen.AdaptiveFeatureGenerator;
import opennlp.tools.util.featuregen.AggregatedFeatureGenerator;
import opennlp.tools.util.featuregen.FeatureGeneratorResourceProvider;

public class FixedNameFinderFactory extends TokenNameFinderFactory {

  public FixedNameFinderFactory() {  
  }
  
  
  /**
   * Creates the {@link AdaptiveFeatureGenerator}. Usually this
   * is a set of generators contained in the {@link AggregatedFeatureGenerator}.
   *
   * Note:
   * The generators are created on every call to this method.
   *
   * @return the feature generator or null if there is no descriptor in the model
   */
  // TODO: During training time the resources need to be loaded from the resources map!
  public AdaptiveFeatureGenerator createFeatureGenerators() {

    byte descriptorBytes[] = null;
    descriptorBytes = getFeatureGeneratorBytes();
    System.err.println("featurebytes 2 " + descriptorBytes);
    if (descriptorBytes != null) {
      InputStream descriptorIn = new ByteArrayInputStream(descriptorBytes);
      AdaptiveFeatureGenerator generator = null;
      try {
        generator = GeneratorFactory.create(descriptorIn, new FeatureGeneratorResourceProvider() {
          public Object getResource(String key) {
            if (artifactProvider != null) {
              return artifactProvider.getArtifact(key);
            }
            else {
              return getResources().get(key);
            }
          }
        });
      } catch (InvalidFormatException e) {
        // It is assumed that the creation of the feature generation does not
        // fail after it succeeded once during model loading.

        // But it might still be possible that such an exception is thrown,
        // in this case the caller should not be forced to handle the exception
        // and a Runtime Exception is thrown instead.

        // If the re-creation of the feature generation fails it is assumed
        // that this can only be caused by a programming mistake and therefore
        // throwing a Runtime Exception is reasonable

        //throw new FeatureGeneratorCreationError(e);
      } catch (IOException e) {
        throw new IllegalStateException("Reading from mem cannot result in an I/O error", e);
      }
      System.err.println("generator done! " + generator);

      return generator;
    }
    else {
      return null;
    }
  }
  

}
