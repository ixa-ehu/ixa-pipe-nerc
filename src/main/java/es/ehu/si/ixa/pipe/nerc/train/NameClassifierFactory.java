/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package es.ehu.si.ixa.pipe.nerc.train;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Properties;

import es.ehu.si.ixa.pipe.nerc.features.AdaptiveFeatureGenerator;
import es.ehu.si.ixa.pipe.nerc.features.NameContextGenerator;

import opennlp.tools.namefind.BioCodec;
import opennlp.tools.util.BaseToolFactory;
import opennlp.tools.util.InvalidFormatException;
import opennlp.tools.util.SequenceCodec;
import opennlp.tools.util.SequenceValidator;
import opennlp.tools.util.ext.ExtensionLoader;

// Idea of this factory is that most resources/impls used by the name finder
// can be modified through this class!
// That only works if thats the central class used for training/runtime

public class NameClassifierFactory extends BaseToolFactory {

  private byte[] featureGeneratorBytes;
  private Map<String, Object> resources;
  private SequenceCodec<String> seqCodec;

  /**
   * Creates a {@link NameClassifierFactory} that provides the default implementation
   * of the resources.
   */
  public NameClassifierFactory() {
    this.seqCodec = new BioCodec();
  }

  public NameClassifierFactory(byte[] featureGeneratorBytes, final Map<String, Object> resources,
      SequenceCodec<String> seqCodec) {
    init(featureGeneratorBytes, resources, seqCodec);
  }

  void init(byte[] featureGeneratorBytes, final Map<String, Object> resources, SequenceCodec<String> seqCodec) {
    this.featureGeneratorBytes = featureGeneratorBytes;
    this.resources = resources;
    this.seqCodec = seqCodec;
  }

  protected SequenceCodec<String> getSequenceCodec() {
    return seqCodec;
  }

  protected Map<String, Object> getResources() {
    return resources;
  }

  public static NameClassifierFactory create(String subclassName, byte[] featureGeneratorBytes, final Map<String, Object> resources,
      SequenceCodec<String> seqCodec)
      throws InvalidFormatException {
    if (subclassName == null) {
      // will create the default factory
      return new NameClassifierFactory();
    }
    try {
      NameClassifierFactory theFactory = ExtensionLoader.instantiateExtension(
          NameClassifierFactory.class, subclassName);
      theFactory.init(featureGeneratorBytes, resources, seqCodec);
      return theFactory;
    } catch (Exception e) {
      String msg = "Could not instantiate the " + subclassName
          + ". The initialization throw an exception.";
      System.err.println(msg);
      e.printStackTrace();
      throw new InvalidFormatException(msg, e);
    }
  }

  @Override
  public void validateArtifactMap() throws InvalidFormatException {
    // no additional artifacts
  }

  public SequenceCodec<String> createSequenceCodec() {

    if (artifactProvider != null) {
      String sequeceCodecImplName = artifactProvider.getManifestProperty(
          NameModel.SEQUENCE_CODEC_CLASS_NAME_PARAMETER);
      return instantiateSequenceCodec(sequeceCodecImplName);
    }
    else {
      return seqCodec;
    }
  }

  public NameContextGenerator createContextGenerator() {

    AdaptiveFeatureGenerator featureGenerator = createFeatureGenerators();

    if (featureGenerator == null) {
      featureGenerator = NameClassifier.createFeatureGenerator();
    }

    return new DefaultNameContextGenerator(featureGenerator);
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

   return null;
  }

  public static SequenceCodec<String> instantiateSequenceCodec(
      String sequenceCodecImplName) {

    if (sequenceCodecImplName != null) {
      return ExtensionLoader.instantiateExtension(
          SequenceCodec.class, sequenceCodecImplName);
    }
    else {
      // If nothing is specified return old default!
      return new BioCodec();
    }
  }
}


