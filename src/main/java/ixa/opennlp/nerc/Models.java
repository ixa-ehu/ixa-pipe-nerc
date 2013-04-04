package ixa.opennlp.nerc;

import java.io.InputStream;

public class Models {

  private InputStream nerModel;

  public InputStream getNERModel(String cmdOption) {

    if (cmdOption.equals("en")) {
      nerModel = getClass().getResourceAsStream(
          "/en-nerc-500-0-testa-perceptron.bin");
    }

    if (cmdOption.equals("es")) {
      nerModel = getClass().getResourceAsStream("/es-nerc-500-4-testa.bin");
    }
    return nerModel;
  }

}
