package es.ehu.si.ixa.pipe.nerc.train;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import opennlp.tools.util.InputStreamFactory;

/**
 * Implement an InputStreamFactory to avoid stream reset issue in opennlp.
 * @author ragerri
 * @version 2014-07-08
 */
public class DefaultInputStreamFactory implements InputStreamFactory {

  /**
   * The input stream.
   */
  private InputStream inputStream;

  /**
   * Construct a default input stream factory.
   * @param aInputStream the input stream
   * @throws FileNotFoundException throw exception if file not found
   */
  public DefaultInputStreamFactory(final InputStream aInputStream) throws FileNotFoundException {
    this.inputStream = aInputStream;
  }

  /* (non-Javadoc)
   * @see opennlp.tools.util.InputStreamFactory#createInputStream()
   */
  public final InputStream createInputStream() throws IOException {
    return inputStream;
  }
}
