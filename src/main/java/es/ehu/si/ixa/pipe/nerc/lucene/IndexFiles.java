package es.ehu.si.ixa.pipe.nerc.lucene;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;

import es.ehu.si.ixa.pipe.nerc.lucene.NamedEntityDoc.Field;

public class IndexFiles {

  boolean create = true;
  private int countDocs;

  public IndexFiles(String options) {
    if (options.equalsIgnoreCase("update")) {
      create = false;
    }
  }

  public void createIndex(String docsDirPath, String indexDirPath, String options) throws IOException {
    File dictDir = new File(docsDirPath);
    File indexDir = new File(indexDirPath);
    IndexFiles indexer = new IndexFiles(options);
    Directory dir = FSDirectory.open(indexDir);
    Analyzer analyzer = new StandardAnalyzer(Version.LUCENE_48);
    IndexWriterConfig indexWriterConfig = new IndexWriterConfig(
        Version.LUCENE_48, analyzer);
    if (create) {
      indexWriterConfig.setOpenMode(OpenMode.CREATE);
    } else {
      indexWriterConfig.setOpenMode(OpenMode.CREATE_OR_APPEND);
    }
    IndexWriter indexWriter = new IndexWriter(dir, indexWriterConfig);
    indexWriter.commit();
    indexer.indexDocs(indexWriter, dictDir);
    System.err.println("documents added by indexer");
    

  }

  private void indexDocs(IndexWriter indexWriter, File docsDirPath) throws IOException {
    
    if (docsDirPath.isDirectory()) {
      String[] files = docsDirPath.list();
      // an IO error could occur
      if (files != null) {
        for (int i = 0; i < files.length; i++) {
          indexDocs(indexWriter, new File(docsDirPath, files[i]));
        }
      }
    }
    else {
    List<String> documents = FileUtils.readLines(docsDirPath);
    int countedDocs = 0;
    for (String document : documents) {
      NamedEntityDoc namedEntityDoc = new NamedEntityDoc(document);
      if (!namedEntityDoc.isEmpty()) {
        addDocToIndex(indexWriter, namedEntityDoc);
        //the rest is just counters
        this.countDocs++;
        if (this.countDocs % 10 == 0) {
          System.err.print(".");
          if (this.countDocs % 1000 == 0) {
            System.err.println();
            System.err.println(String.format("Added indexes [%s...%s]",
                countedDocs, this.countDocs));
            countedDocs = this.countDocs;
          }
        }
      }
    }
    indexWriter.close();
    }
  }

  private static void addDocToIndex(IndexWriter indexWriter,
      NamedEntityDoc namedEntityDoc) throws IOException {
    Document document = new Document();
    document.add(new StringField(Field.NAME.toString(), namedEntityDoc
        .getName(), Store.YES));
    document.add(new StringField(Field.TYPE.toString(), namedEntityDoc
        .getType(), Store.YES));
    indexWriter.addDocument(document);
    // this.commit();
  }

}
