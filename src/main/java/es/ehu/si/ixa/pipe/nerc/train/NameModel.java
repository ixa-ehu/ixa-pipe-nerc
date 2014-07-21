package es.ehu.si.ixa.pipe.nerc.train;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import es.ehu.si.ixa.pipe.nerc.features.AdaptiveFeatureGenerator;

import opennlp.model.AbstractModel;
import opennlp.model.MaxentModel;
import opennlp.tools.util.InvalidFormatException;
import opennlp.tools.util.model.ArtifactSerializer;
import opennlp.tools.util.model.BaseModel;
import opennlp.tools.util.model.ModelUtil;

/**
 * The {@link TokenNameFinderModel} is the model used
 * by a learnable {@link TokenNameFinder}.
 *
 * @see NameFinderME
 */
public class NameModel extends BaseModel {

  public static class FeatureGeneratorCreationError extends RuntimeException {
    FeatureGeneratorCreationError(Throwable t) {
      super(t);
    }
  }
  
  private static class ByteArraySerializer implements ArtifactSerializer<byte[]> {

    public byte[] create(InputStream in) throws IOException,
        InvalidFormatException {
      
      return ModelUtil.read(in);
    }

    public void serialize(byte[] artifact, OutputStream out) throws IOException {
      out.write(artifact);
    }
  }
  
  private static final String COMPONENT_NAME = "NameFinderME";
  private static final String MAXENT_MODEL_ENTRY_NAME = "nameFinder.model";
 
  private static final String GENERATOR_DESCRIPTOR_ENTRY_NAME = "generator.featuregen";
 
  public NameModel(String languageCode, AbstractModel nameFinderModel,
      byte[] generatorDescriptor, Map<String, Object> resources, Map<String, String> manifestInfoEntries) {
    
    super(COMPONENT_NAME, languageCode, manifestInfoEntries);
    
    if (!isModelValid(nameFinderModel)) {
      throw new IllegalArgumentException("Model not compatible with name finder!");
    }

    artifactMap.put(MAXENT_MODEL_ENTRY_NAME, nameFinderModel);
    
    if (generatorDescriptor != null && generatorDescriptor.length > 0)
      artifactMap.put(GENERATOR_DESCRIPTOR_ENTRY_NAME, generatorDescriptor);
    
    if (resources != null) {
      // The resource map must not contain key which are already taken
      // like the name finder maxent model name
      if (resources.containsKey(MAXENT_MODEL_ENTRY_NAME) ||
          resources.containsKey(GENERATOR_DESCRIPTOR_ENTRY_NAME)) {
        throw new IllegalArgumentException();
      }
      
      // TODO: Add checks to not put resources where no serializer exists,
      // make that case fail here, should be done in the BaseModel
      artifactMap.putAll(resources); 
    }
    checkArtifactMap();
  }

  public NameModel(String languageCode, AbstractModel nameFinderModel,
      Map<String, Object> resources, Map<String, String> manifestInfoEntries) {
    this(languageCode, nameFinderModel, null, resources, manifestInfoEntries);
  }
      
  public NameModel(InputStream in) throws IOException, InvalidFormatException {
    super(COMPONENT_NAME, in);
  }
  
  public NameModel(File modelFile) throws IOException, InvalidFormatException {
    super(COMPONENT_NAME, modelFile);
  }
  
  public NameModel(URL modelURL) throws IOException, InvalidFormatException {
    super(COMPONENT_NAME, modelURL);
  }
  
  
  /**
   * Retrieves the {@link TokenNameFinder} model.
   *
   * @return the classification model
   */
  public AbstractModel getNameFinderModel() {
    return (AbstractModel) artifactMap.get(MAXENT_MODEL_ENTRY_NAME);
  }
  
  public NameModel updateFeatureGenerator(byte descriptor[]) {
        
    NameModel model = new NameModel(getLanguage(), getNameFinderModel(),
        descriptor, Collections.<String, Object>emptyMap(), Collections.<String, String>emptyMap());
    
    // TODO: Not so nice!
    model.artifactMap.clear();
    model.artifactMap.putAll(artifactMap);
    model.artifactMap.put(GENERATOR_DESCRIPTOR_ENTRY_NAME, descriptor);
    
    return model;
  }
  
  @Override
  protected void createArtifactSerializers(Map<String, ArtifactSerializer> serializers) {
    super.createArtifactSerializers(serializers);
    
    serializers.put("featuregen", new ByteArraySerializer());
  }
  
  public static Map<String, ArtifactSerializer> createArtifactSerializers()  {
    
    // TODO: Not so nice, because code cannot really be reused by the other create serializer method
    //       Has to be redesigned, we need static access to default serializers
    //       and these should be able to extend during runtime ?! 
    
    Map<String, ArtifactSerializer> serializers = BaseModel.createArtifactSerializers();
    
    serializers.put("featuregen", new ByteArraySerializer());
    
    return serializers;
  }
  
  // TODO: Write test for this method
  public static boolean isModelValid(MaxentModel model) {
    
    // We should have *optionally* one outcome named "other", some named xyz-start and sometimes 
    // they have a pair xyz-cont. We should not have any other outcome
    // To validate the model we check if we have one outcome named "other", at least
    // one outcome with suffix start. After that we check if all outcomes that ends with
    // "cont" have a pair that ends with "start".
    List<String> start = new ArrayList<String>();
    List<String> cont = new ArrayList<String>();

    for (int i = 0; i < model.getNumOutcomes(); i++) {
      String outcome = model.getOutcome(i);
      if (outcome.endsWith(NameClassifier.START)) {
        start.add(outcome.substring(0, outcome.length()
            - NameClassifier.START.length()));
      } else if (outcome.endsWith(NameClassifier.CONTINUE)) {
        cont.add(outcome.substring(0, outcome.length()
            - NameClassifier.CONTINUE.length()));
      } else if (outcome.equals(NameClassifier.OTHER)) {
        // don't fail anymore if couldn't find outcome named OTHER
      } else {
        // got unexpected outcome
        return false;
      }
    }

    if (start.size() == 0) {
      return false;
    } else {
      for (String contPreffix : cont) {
        if (!start.contains(contPreffix)) {
          return false;
        }
      }
    }

    return true;
  }
  
  @Override
  protected void validateArtifactMap() throws InvalidFormatException {
    super.validateArtifactMap();
    
    if (artifactMap.get(MAXENT_MODEL_ENTRY_NAME) instanceof AbstractModel) {
      AbstractModel model = (AbstractModel) artifactMap.get(MAXENT_MODEL_ENTRY_NAME);
      isModelValid(model);
    }
    else {
      throw new InvalidFormatException("Token Name Finder model is incomplete!");
    }
  }
}
