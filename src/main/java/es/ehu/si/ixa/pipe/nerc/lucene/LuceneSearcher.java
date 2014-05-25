package es.ehu.si.ixa.pipe.nerc.lucene;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.core.KeywordAnalyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.WildcardQuery;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;

import es.ehu.si.ixa.pipe.nerc.Name;
import es.ehu.si.ixa.pipe.nerc.NameFactory;
import es.ehu.si.ixa.pipe.nerc.lucene.NamedEntityDoc.Field;

public class LuceneSearcher {

  private String indexDir;
  private NameFactory nameFactory;
  public boolean exactMatch = true;

  public LuceneSearcher(String aIndexDir, NameFactory aNameFactory) {
    this.nameFactory = aNameFactory;
    this.indexDir = aIndexDir;
  }

  public List<Name> searchNames(String queryString) {
    List<Name> foundNames = new ArrayList<Name>();
    IndexReader indexReader;
    try {
      indexReader = DirectoryReader.open(FSDirectory.open(new File(indexDir)));
      IndexSearcher indexSearcher = new IndexSearcher(indexReader);
      //Analyzer keywordAnalyzer = new KeywordAnalyzer();
      Analyzer standardAnalyzer = new StandardAnalyzer(Version.LUCENE_48);
      QueryParser queryParser = new QueryParser(Version.LUCENE_48,
          NamedEntityDoc.Field.NAME.toString(), standardAnalyzer);
      queryParser.setAllowLeadingWildcard(true);
      queryParser.setLowercaseExpandedTerms(true);
      Query query = createQuery(queryParser, queryString);
      ScoreDoc[] names = indexSearcher.search(query, 10).scoreDocs;
      for (ScoreDoc name : names) {
        Document doc = indexSearcher.doc(name.doc);
        System.err.println(doc.get(Field.NAME.toString()) + " " + doc.get(Field.TYPE.toString()));
        Name nameObject = nameFactory.createName(doc.get(Field.NAME.toString()), doc.get(Field.TYPE.toString()));
        foundNames.add(nameObject);
      }
    } catch (IOException e) {
      e.printStackTrace();
    } catch (ParseException parseException) {
      parseException.printStackTrace();
    }
    return foundNames;
  }

  private Query createQuery(QueryParser queryParser, String queryString) throws ParseException {
    String query = new String();
    if (!exactMatch) {
      queryString = WildcardQuery.WILDCARD_STRING + queryString
          + WildcardQuery.WILDCARD_STRING;
    }
    query += Field.NAME.toString() + ":" + queryString;
    Query q = queryParser.parse(query);
    System.err.println(q.toString());
    return q;
  }

}
