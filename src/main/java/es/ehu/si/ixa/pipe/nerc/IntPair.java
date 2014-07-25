package es.ehu.si.ixa.pipe.nerc;

public class IntPair implements Comparable<IntPair> {

  final int[] elements = new int[2];
  
  public IntPair(int src, int trgt) {
    elements[0] = src;
    elements[1] = trgt;
  }

  public int get(int num) {
    return elements[num];
  }

  /**
   * Return the first element of the pair
   */
  public int getSource() {
    return get(0);
  }

  /**
   * Return the second element of the pair
   */
  public int getTarget() {
    return get(1);
  }

  public int length() {
    return elements.length;
  }

  public int compareTo(IntPair o) {
    int commonLen = Math.min(o.length(), length());
    for (int i = 0; i < commonLen; i++) {
      int a = get(i);
      int b = o.get(i);
      if (a < b)
        return -1;
      if (b < a)
        return 1;
    }
    if (o.length() == length()) {
      return 0;
    } else {
      return (length() < o.length()) ? -1 : 1;
    }
  }

  public IntPair getCopy() {
    return new IntPair(elements[0], elements[1]);
  }

  @Override
  public boolean equals(Object iO) {
    if (!(iO instanceof IntPair)) {
      return false;
    }
    IntPair i = (IntPair) iO;
    return elements[0] == i.get(0) && elements[1] == i.get(1);
  }

  @Override
  public int hashCode() {
    return elements[0] * 17 + elements[1];
  }

}
