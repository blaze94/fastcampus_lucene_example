package fastcampus.lucene.example.search;

/**
루씬 스코어 예제
*/
import junit.framework.TestCase;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.core.WhitespaceAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.FieldType;
import org.apache.lucene.index.*;
import org.apache.lucene.search.*;
import org.apache.lucene.search.similarities.Similarity;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.RAMDirectory;

import java.io.IOException;
import java.util.Vector;

// 스코어 테스트
public class ScoreTest extends TestCase {
  private Directory directory;

  public void setUp() throws Exception {
    directory = new RAMDirectory();
  }

  public void tearDown() throws Exception {
    directory.close();
  }

  public void testSimple() throws Exception {

    FieldType textType = new FieldType();
    textType.setIndexOptions(IndexOptions.DOCS_AND_FREQS_AND_POSITIONS_AND_OFFSETS);
    textType.setStored(true);
    textType.setTokenized(true);

    indexSingleFieldDocs(new Field[] {new Field("contents", "x", textType)});


    IndexReader reader = DirectoryReader.open(directory);
    IndexSearcher searcher = new IndexSearcher(reader);
    searcher.setSimilarity(new SimpleSimilarity());

    Query query = new TermQuery(new Term("contents", "x"));
    Explanation explanation = searcher.explain(query, 0);
    System.out.println(explanation.toString());

    TopDocs matches = searcher.search(query, 10);
    assertEquals(1, matches.totalHits);

    assertEquals(1F, matches.scoreDocs[0].score, 0.0);

    reader.close();  //searcher는 close할 필요 없음
  }

  private void indexSingleFieldDocs(Field[] fields) throws Exception {
    Analyzer analyzer=new WhitespaceAnalyzer();
    IndexWriterConfig config = new IndexWriterConfig(analyzer);
    IndexWriter writer = new IndexWriter(directory,config);
    for (Field f : fields) {
      Document doc = new Document();
      doc.add(f);
      writer.addDocument(doc);
    }
    //writer.optimize(); writer의 optimize는 deprecated됨 (성능에 악영향)
    //http://blog.trifork.com/2011/11/21/simon-says-optimize-is-bad-for-you/ 참고
    writer.close();
  }

  /**
  와일드카드 검색 테스트
   */
  public void testWildcard() throws Exception {

    FieldType textType = new FieldType();
    textType.setIndexOptions(IndexOptions.DOCS_AND_FREQS_AND_POSITIONS_AND_OFFSETS);
    textType.setStored(true);
    textType.setTokenized(true);


    indexSingleFieldDocs(new Field[]
      { new Field("contents", "wild", textType),
        new Field("contents", "child", textType),
        new Field("contents", "mild", textType),
        new Field("contents", "mildew", textType) });

    IndexReader reader = DirectoryReader.open(directory);
    IndexSearcher searcher = new IndexSearcher(reader);

    Query query = new WildcardQuery(new Term("contents", "?ild*"));  //#A
    TopDocs matches = searcher.search(query, 10);
    assertEquals("child no match", 3, matches.totalHits);

    assertEquals("score the same", matches.scoreDocs[0].score,
                                   matches.scoreDocs[1].score, 0.0);
    assertEquals("score the same", matches.scoreDocs[1].score,
                                   matches.scoreDocs[2].score, 0.0);
    reader.close();
  }
  /**
    #A 퍼지검색 테스트
  */

  public void testFuzzy() throws Exception {

    FieldType textType = new FieldType();
    textType.setIndexOptions(IndexOptions.DOCS_AND_FREQS_AND_POSITIONS_AND_OFFSETS);
    textType.setStored(true);
    textType.setTokenized(true);

    indexSingleFieldDocs(new Field[] { new Field("contents",
                                                 "fuzzy",
            textType),
                                       new Field("contents",
                                                 "wuzzy",
                                               textType)
                                     });

    IndexReader reader = DirectoryReader.open(directory);
    IndexSearcher searcher = new IndexSearcher(reader);

    Query query = new FuzzyQuery(new Term("contents", "wuzza"));
    TopDocs matches = searcher.search(query, 10);
    assertEquals("both close enough", 2, matches.totalHits);

    assertTrue("wuzzy closer than fuzzy",
               matches.scoreDocs[0].score != matches.scoreDocs[1].score);

    Document doc = searcher.doc(matches.scoreDocs[0].doc);
    assertEquals("wuzza bear", "wuzzy", doc.get("contents"));
    reader.close();
  }

  public static class SimpleSimilarity extends Similarity {
    public float lengthNorm(String field, int numTerms) {
      return 1.0f;
    }
    public float queryNorm(float sumOfSquaredWeights) {
      return 1.0f;
    }

    @Override
    public long computeNorm(FieldInvertState fieldInvertState) {
      return 0;
    }

    @Override
    public SimWeight computeWeight(CollectionStatistics collectionStatistics, TermStatistics... termStatisticss) {
      return null;
    }

    @Override
    public SimScorer simScorer(SimWeight simWeight, LeafReaderContext leafReaderContext) throws IOException {
      return null;
    }

    public float tf(float freq) {
      return freq;
    }

    public float sloppyFreq(int distance) {
      return 2.0f;
    }

    public float idf(Vector terms, IndexSearcher searcher) {
      return 1.0f;
    }

    public float idf(int docFreq, int numDocs) {
      return 1.0f;
    }

    public float coord(int overlap, int maxOverlap) {
      return 1.0f;
    }
  }

}
