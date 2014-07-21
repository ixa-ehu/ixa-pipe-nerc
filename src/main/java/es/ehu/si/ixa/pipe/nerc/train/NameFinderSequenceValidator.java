package es.ehu.si.ixa.pipe.nerc.train;

import opennlp.tools.util.SequenceValidator;

public class NameFinderSequenceValidator implements
SequenceValidator<String> {

public boolean validSequence(int i, String[] inputSequence,
  String[] outcomesSequence, String outcome) {

// outcome is formatted like "cont" or "sometype-cont", so we
// can check if it ends with "cont".
if (outcome.endsWith(NameClassifier.CONTINUE)) {
  
  int li = outcomesSequence.length - 1;
  
  if (li == -1) {
    return false;
  } else if (outcomesSequence[li].endsWith(NameClassifier.OTHER)) {
    return false;
  } else if (outcomesSequence[li].endsWith(NameClassifier.CONTINUE)) {
    // if it is continue, we have to check if previous match was of the same type 
    String previousNameType = NameClassifier.extractNameType(outcomesSequence[li]);
    String nameType = NameClassifier.extractNameType(outcome);
    if( previousNameType != null || nameType != null ) {
      if( nameType != null ) {
        if( nameType.equals(previousNameType) ){
          return true;
        }
      }
      return false; // outcomes types are not equal
    }
  }
}
return true;
}
}
