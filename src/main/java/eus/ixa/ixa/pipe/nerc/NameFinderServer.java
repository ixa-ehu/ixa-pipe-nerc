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
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Properties;

public class NameFinderServer {

  public NameFinderServer(Properties properties) {

    Integer port = Integer.parseInt(properties.getProperty("port"));
    BufferedReader stdInReader = null;
    ServerSocket socketServer = null;

    try {
      Annotate annotator = new Annotate(properties);
      System.out.println("Trying to listen port " + port);
      socketServer = new ServerSocket(port);
      System.out.println("Listening to port " + port);

      while (true) {
        Socket socketClient = socketServer.accept();
        InputStream dataInStream = socketClient.getInputStream();
        DataInput inFromClient = new DataInputStream(dataInStream);
        OutputStream dataOutStream = socketClient.getOutputStream();
        DataOutputStream outToClient = new DataOutputStream(dataOutStream);
        try {

          StringBuilder stdInStringBuilder = new StringBuilder();
          boolean EnOfInputFile = inFromClient.readBoolean();
          String line = "";
          while (!EnOfInputFile) {
            line = inFromClient.readUTF();
            stdInStringBuilder.append(line);
            stdInStringBuilder.append('\n');
            EnOfInputFile = inFromClient.readBoolean();
          }
          String stringFromClient = stdInStringBuilder.toString();
        

          stdInReader = new BufferedReader(new StringReader(stringFromClient));
          KAFDocument kaf = KAFDocument.createFromStream(stdInReader);
          annotator.annotateNEs(kaf);
           String kafToString = null;
           String outputFormat = properties.getProperty("outputFormat");
           if (outputFormat.equalsIgnoreCase("conll03")) {
           kafToString = annotator.annotateNEsToCoNLL2003(kaf);
           } else if (outputFormat.equalsIgnoreCase("conll02")) {
           kafToString = annotator.annotateNEsToCoNLL2002(kaf);
           } else if (outputFormat.equalsIgnoreCase("opennlp")) {
           kafToString = annotator.annotateNEsToOpenNLP(kaf);
           } else {
           kafToString = annotator.annotateNEsToKAF(kaf);
           }

          BufferedReader kafReader = new BufferedReader(new StringReader(kafToString));
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
