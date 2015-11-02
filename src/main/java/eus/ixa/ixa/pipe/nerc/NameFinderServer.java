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

package eus.ixa.ixa.pipe.nerc;

import ixa.kaflib.KAFDocument;

import java.io.BufferedReader;
import java.io.DataInput;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.StringReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Properties;

import com.google.common.io.Files;

public class NameFinderServer {
  
  /**
   * Get dynamically the version of ixa-pipe-nerc by looking at the MANIFEST
   * file.
   */
  private final String version = CLI.class.getPackage().getImplementationVersion();
  /**
   * Get the git commit of the ixa-pipe-nerc compiled by looking at the MANIFEST
   * file.
   */
  private final String commit = CLI.class.getPackage().getSpecificationVersion();

  /**
   * Construct a NameFinder server.
   * @param properties the properties
   */
  public NameFinderServer(Properties properties) {

    Integer port = Integer.parseInt(properties.getProperty("port"));
    String model = properties.getProperty("model");
    String outputFormat = properties.getProperty("outputFormat");
    ServerSocket socketServer = null;

    try {
      Annotate annotator = new Annotate(properties);
      System.out.println("-> Trying to listen port... " + port);
      socketServer = new ServerSocket(port);
      System.out.println("-> SUCCESS!! Listening to port " + port);

      while (true) {
        Socket socketClient = socketServer.accept();
        //data from client;
        DataInput inFromClient = new DataInputStream(socketClient.getInputStream());
        DataOutputStream outToClient = new DataOutputStream(socketClient.getOutputStream());
        
        try {
          //get data from client and build a string with it
          StringBuilder stringFromClient = new StringBuilder();
          boolean endOfClientFile = inFromClient.readBoolean();
          String line = "";
          while (!endOfClientFile) {
            line = inFromClient.readUTF();
            stringFromClient.append(line).append("\n");
            endOfClientFile = inFromClient.readBoolean();
          }
          //get a breader from the string coming from the client
          BufferedReader clientReader = new BufferedReader(new StringReader(stringFromClient.toString()));
          KAFDocument kaf = KAFDocument.createFromStream(clientReader);
          KAFDocument.LinguisticProcessor newLp = kaf.addLinguisticProcessor(
              "entities",
              "ixa-pipe-nerc-" + Files.getNameWithoutExtension(model), version
                  + "-" + commit);
          newLp.setBeginTimestamp();
          annotator.annotateNEs(kaf);
          newLp.setEndTimestamp();
          
          // get outputFormat
          String kafToString = null;
          if (outputFormat.equalsIgnoreCase("conll03")) {
            kafToString = annotator.annotateNEsToCoNLL2003(kaf);
          } else if (outputFormat.equalsIgnoreCase("conll02")) {
            kafToString = annotator.annotateNEsToCoNLL2002(kaf);
          } else if (outputFormat.equalsIgnoreCase("opennlp")) {
            kafToString = annotator.annotateNEsToOpenNLP(kaf);
          } else {
            kafToString = annotator.annotateNEsToKAF(kaf);
          }
          
          //get a reader from the final NAF document
          BufferedReader kafReader = new BufferedReader(new StringReader(
              kafToString));
          //send NAF to client
          String kafLine = kafReader.readLine();
          while (kafLine != null) {
            outToClient.writeBoolean(false);
            outToClient.writeUTF(kafLine);
            kafLine = kafReader.readLine();
          }
          outToClient.writeBoolean(true);
          
        } catch (Exception e) {
          System.out.println(e.getMessage());
        }
      }
    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    try {
      socketServer.close();
    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }

}
