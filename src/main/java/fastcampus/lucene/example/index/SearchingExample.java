package fastcampus.lucene.example.index;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Date;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.FSDirectory;

/** Simple command-line based search demo. */
public class SearchingExample {

  private SearchingExample() {
  }

  /**
   * Simple command-line based search demo.
   */
  public static void main(String[] args) throws Exception {

    String index = "index"; //기본 인덱스 위치
    String field = "title";
    String queries = null;


    int repeat = 0;
    boolean raw = false;
    String queryString = null;
    int hitsPerPage = 10;


    //인덱스를 읽어들임
    IndexReader reader = DirectoryReader.open(FSDirectory.open(Paths.get(index)));

    //서처를 생성함
    IndexSearcher searcher = new IndexSearcher(reader);

    //기본분석기를 사용함
    Analyzer analyzer = new StandardAnalyzer();

    BufferedReader in = null;
    if (queries != null) {
      in = Files.newBufferedReader(Paths.get(queries), StandardCharsets.UTF_8);
    } else {
      in = new BufferedReader(new InputStreamReader(System.in, StandardCharsets.UTF_8));
    }
    QueryParser parser = new QueryParser(field, analyzer);
    while (true) {
      if (queries == null && queryString == null) {                        // prompt the user
        System.out.println("Enter query: ");
      }

      String keyword = in.readLine();

      if (keyword == null || keyword.length() == -1) {
        break;
      }

      keyword = keyword.trim();
      if (keyword.length() == 0) {
        break;
      }

      Query query = parser.parse(keyword);
      System.out.println("Searching for: " + query.toString());

      TopDocs hits = searcher.search(query, 100);
      int num = 0;
      for (ScoreDoc sd : hits.scoreDocs) {
        Document d = searcher.doc(sd.doc);
        System.out.println(String.format("#%d: %s (rating=%s)", ++num, d.get("id"), d.get("title")));
      }

    }
    reader.close();
  }

}



























































