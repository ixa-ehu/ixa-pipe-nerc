package es.ehu.si.ixa.pipe.nerc.features;

import java.util.ArrayList;
import java.util.List;

import es.ehu.si.ixa.pipe.nerc.dict.Dictionary;

public class BrownTokenClasses {
  
  public static final int[] pathLengths = { 4, 6, 10, 20 };
  
  public static List<String> getWordClasses(String token, Dictionary brownLexicon) {

    if (brownLexicon.getDict().get(token) == null) {
      return new ArrayList<String>(0);
    } else {
      String distSim = brownLexicon.getDict().get(token);
      List<String> pathLengthsList = new ArrayList<String>();
      pathLengthsList.add(distSim.substring(0,
          Math.min(distSim.length(), pathLengths[0])));
      for (int i = 1; i < pathLengths.length; i++) {
        if (pathLengths[i - 1] < distSim.length()) {
          pathLengthsList.add(distSim.substring(0,
              Math.min(distSim.length(), pathLengths[i])));
        }
      }
      return pathLengthsList;
    }
  }
  
  public static void printList(List<String> classList) {
    for (int i = 0; i < classList.size(); i++) {
      System.out.print(" " + classList.get(i));
    }
    System.out.println("");
  }

}
