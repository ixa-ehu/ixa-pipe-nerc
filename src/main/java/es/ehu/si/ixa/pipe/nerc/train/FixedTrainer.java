/*
 *  Copyright 2014 Rodrigo Agerri

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
 */

package es.ehu.si.ixa.pipe.nerc.train;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import es.ehu.si.ixa.pipe.nerc.dict.BrownCluster;
import es.ehu.si.ixa.pipe.nerc.dict.ClarkCluster;
import es.ehu.si.ixa.pipe.nerc.dict.Dictionaries;
import es.ehu.si.ixa.pipe.nerc.dict.Dictionary;
import es.ehu.si.ixa.pipe.nerc.dict.Word2VecCluster;
import es.ehu.si.ixa.pipe.nerc.features.BigramClassFeatureGenerator;
import es.ehu.si.ixa.pipe.nerc.features.BrownBigramFeatureGenerator;
import es.ehu.si.ixa.pipe.nerc.features.BrownTokenClassFeatureGenerator;
import es.ehu.si.ixa.pipe.nerc.features.BrownTokenFeatureGenerator;
import es.ehu.si.ixa.pipe.nerc.features.CharacterNgramFeatureGenerator;
import es.ehu.si.ixa.pipe.nerc.features.ClarkFeatureGenerator;
import es.ehu.si.ixa.pipe.nerc.features.DictionaryFeatureGenerator;
import es.ehu.si.ixa.pipe.nerc.features.FivegramClassFeatureGenerator;
import es.ehu.si.ixa.pipe.nerc.features.FourgramClassFeatureGenerator;
import es.ehu.si.ixa.pipe.nerc.features.OutcomePriorFeatureGenerator;
import es.ehu.si.ixa.pipe.nerc.features.Prefix34FeatureGenerator;
import es.ehu.si.ixa.pipe.nerc.features.Prev2MapFeatureGenerator;
import es.ehu.si.ixa.pipe.nerc.features.PreviousMapFeatureGenerator;
import es.ehu.si.ixa.pipe.nerc.features.PreviousMapTokenFeatureGenerator;
import es.ehu.si.ixa.pipe.nerc.features.SentenceFeatureGenerator;
import es.ehu.si.ixa.pipe.nerc.features.SuffixFeatureGenerator;
import es.ehu.si.ixa.pipe.nerc.features.TokenClassFeatureGenerator;
import es.ehu.si.ixa.pipe.nerc.features.TokenFeatureGenerator;
import es.ehu.si.ixa.pipe.nerc.features.TrigramClassFeatureGenerator;
import es.ehu.si.ixa.pipe.nerc.features.WindowFeatureGenerator;
import es.ehu.si.ixa.pipe.nerc.features.Word2VecClusterFeatureGenerator;

import opennlp.tools.cmdline.CmdLineUtil;
import opennlp.tools.namefind.TokenNameFinderFactory;
import opennlp.tools.namefind.TokenNameFinderModel;
import opennlp.tools.util.InvalidFormatException;
import opennlp.tools.util.SequenceCodec;
import opennlp.tools.util.TrainingParameters;
import opennlp.tools.util.featuregen.AdaptiveFeatureGenerator;
import opennlp.tools.util.featuregen.CachedFeatureGenerator;
import opennlp.tools.util.featuregen.GeneratorFactory;
import opennlp.tools.util.model.ArtifactSerializer;

/**
 * Training NER based on Apache OpenNLP Machine Learning API for English. This
 * class creates a feature set based on the features activated in the
 * trainParams.txt properties file:
 * <ol>
 * <li>Window: specify left and right window lengths.
 * <li>TokenFeatures: tokens as features in a window length.
 * <li>TokenClassFeatures: token shape features in a window length.
 * <li>OutcomePriorFeatures: take into account previous outcomes.
 * <li>PreviousMapFeatures: add features based on tokens and previous decisions.
 * <li>SentenceFeatures: add beginning and end of sentence words.
 * <li>PrefixFeatures: first 4 characters in current token.
 * <li>SuffixFeatures: last 4 characters in current token.
 * <li>BigramClassFeatures: bigrams of tokens and token class.
 * <li>TrigramClassFeatures: trigrams of token and token class.
 * <li>FourgramClassFeatures: fourgrams of token and token class.
 * <li>FivegramClassFeatures: fivegrams of token and token class.
 * <li>CharNgramFeatures: character ngram features of current token.
 * <li>DictionaryFeatures: check if current token appears in some gazetteer.
 * <li>ClarkClusterFeatures: use the clustering class of a token as a feature.
 * <li>BrownClusterFeatures: use brown clusters as features for each feature containing a token.
 * <li>Word2VecClusterFeatures: use the word2vec clustering class of a token as a feature.
 * <ol>
 * 
 * @author ragerri
 * @version 2014-09-24
 */
public class FixedTrainer extends AbstractTrainer {  
  
  /**
   * Construct a trainer based on features specified in the trainParams.txt
   * properties file.
   */
  
  /**
   * Construct a trainer based on features specified in the trainParams.txt
   * properties file.
   */
  public FixedTrainer(final String trainData,
      final String testData, final TrainingParameters params)
      throws IOException {
    super(trainData, testData, params);
    createTrainer(params);
  }

  /**
   * Create fixed trainer parameters from the params file.
   * @param params
   * @throws IOException 
   */
  public void createTrainer(TrainingParameters params) throws IOException {
    String seqCodec = getSequenceCodec();
    SequenceCodec<String> sequenceCodec = TokenNameFinderFactory.instantiateSequenceCodec(seqCodec);
    Map<String, Object> resources = loadResources(new File("jar.txt"), new File("jar.txt"));
    String featureDescription = XMLFeatureDescriptor.createXMLFeatureDescriptor(params);
    byte[] featureGeneratorBytes = featureDescription.getBytes(Charset.forName("UTF-8"));
    setNameClassifierFactory(FixedNameFinderFactory.create("FixedNameFinderFactory", featureGeneratorBytes, resources, sequenceCodec));
    
   
  }
  
  /**
   * Loads the external resources (cluster lexicons, etc.).
   * @param resourcePath
   * @param featureGenDescriptor
   * @return
   */
  public static Map<String, Object> loadResources(File resourcePath, File featureGenDescriptor) {
    Map<String, Object> resources = new HashMap<String, Object>();

    if (resourcePath != null) {
      Map<String, ArtifactSerializer> artifactSerializers = TokenNameFinderModel
          .createArtifactSerializers();
      
      // TODO: If there is descriptor file, it should be consulted too
      if (featureGenDescriptor != null) {
        InputStream xmlDescriptorIn = null;
        try {
          artifactSerializers.putAll(GeneratorFactory.extractCustomArtifactSerializerMappings(xmlDescriptorIn));
        } catch (IOException e) {
          // TODO: Improve error handling!
          e.printStackTrace();
        }
      }
      File resourceFiles[] = resourcePath.listFiles();
      // TODO: Filter files, also files with start with a dot
      for (File resourceFile : resourceFiles) {
        // TODO: Move extension extracting code to method and
        // write unit test for it
        // extract file ending
        String resourceName = resourceFile.getName();
        int lastDot = resourceName.lastIndexOf('.');
        if (lastDot == -1) {
          continue;
        }
        String ending = resourceName.substring(lastDot + 1);
        // lookup serializer from map
        ArtifactSerializer serializer = artifactSerializers.get(ending);
        // TODO: Do different? For now just ignore ....
        if (serializer == null)
          continue;
        InputStream resoruceIn = CmdLineUtil.openInFile(resourceFile);
        try {
          resources.put(resourceName, serializer.create(resoruceIn));
        } catch (InvalidFormatException e) {
          // TODO: Fix exception handling
          e.printStackTrace();
        } catch (IOException e) {
          // TODO: Fix exception handling
          e.printStackTrace();
        } finally {
          try {
            resoruceIn.close();
          } catch (IOException e) {
          }
        }
      }
    }
    return resources;
  }

  static Map<String, Object> loadResources(String resourceDirectory, File featureGeneratorDescriptor) {

    if (resourceDirectory != null) {
      File resourcePath = new File(resourceDirectory);
      return loadResources(resourcePath, featureGeneratorDescriptor);
    }
    return new HashMap<String, Object>();
  }
  
}
