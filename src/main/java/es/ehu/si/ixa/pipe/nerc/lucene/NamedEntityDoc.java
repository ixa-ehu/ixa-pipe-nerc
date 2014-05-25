package es.ehu.si.ixa.pipe.nerc.lucene;

public class NamedEntityDoc {

  public static enum Field {
    NAME(0), TYPE(1);
    private int index;

    private Field(int index) {
      this.index = index;
    }

    private int getIndex() {
      return this.index;
    }
  }
  
  private String name = null;
  private String type = null;

  private final static int SIZE = 2;
  private final static String SEPARATOR = ";";

  public NamedEntityDoc(String line) {
    String[] fields = line.split(SEPARATOR);
    if (fields.length == SIZE) {
      this.name = parseString(fields, Field.NAME.getIndex());
      this.type = parseString(fields, Field.TYPE.getIndex());
      //System.err.println(name);
    }
  }
  
  private String parseString(String[] fields, int index) {
    String text = fields[index].trim().toLowerCase();
    return text.trim().replaceAll(" ", "_");
  }

  public String getName() {
    return this.name;
  }

  public String getType() {
    return this.type;
  }
  
  public boolean isEmpty() {
    return this.name == null;
}
  
}
