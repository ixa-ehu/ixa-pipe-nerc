/*
 *  Copyright 2015 Rodrigo Agerri

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

package es.ehu.si.ixa.ixa.pipe.nerc.dict;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import opennlp.tools.postag.POSModel;
import opennlp.tools.postag.POSTaggerME;
import opennlp.tools.util.InvalidFormatException;
import opennlp.tools.util.model.ArtifactSerializer;
import opennlp.tools.util.model.SerializableArtifact;



/**
 * 
 * Class to load a Word2Vec cluster document: word\\s+word_class
 * http://code.google.com/p/word2vec/
 * 
 * The file containing the clustering lexicon has to be passed as the 
 * argument of the Word2VecCluster property.
 * 
 * @author ragerri
 * @version 2014/07/29
 * 
 */
public class POSModelResource implements SerializableArtifact {
  
  public static class POSModelResourceSerializer implements ArtifactSerializer<POSModelResource> {

    public POSModelResource create(InputStream in) throws IOException,
        InvalidFormatException {
      return new POSModelResource(in);
    }

    public void serialize(POSModelResource artifact, OutputStream out)
        throws IOException {
      artifact.serialize(out);
    }
  }
  
  private POSModel posModel;
  private POSTaggerME posTagger;
  
  public POSModelResource(InputStream in) throws IOException {
    posModel = new POSModel(in);
    posTagger = new POSTaggerME(posModel);
  }
  
  public String[] posTag(String[] tokens) {
    String[] posTags = posTagger.tag(tokens);
    return posTags;
  }
  
  public void serialize(OutputStream out) throws IOException {
    Writer writer = new BufferedWriter(new OutputStreamWriter(out));
    posModel.serialize(out);

    writer.flush();
  }

  public Class<?> getArtifactSerializerClass() {
    return POSModelResourceSerializer.class;
  }

}


