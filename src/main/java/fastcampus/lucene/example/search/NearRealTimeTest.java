package fastcampus.lucene.example.search;


import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.util.Version;
import org.apache.lucene.store.*;
import org.apache.lucene.index.*;
import org.apache.lucene.search.*;
import org.apache.lucene.document.*;
import org.apache.lucene.analysis.standard.StandardAnalyzer;

import junit.framework.TestCase;

// From chapter 3
public class NearRealTimeTest extends TestCase {
  public void testNearRealTime() throws Exception {
    Directory dir = new RAMDirectory();
    Analyzer analyzer = new StandardAnalyzer();
    IndexWriterConfig indexWriterConfig = new IndexWriterConfig(analyzer);  //인덱스Writer의 설정을 지정하는 클래스

    IndexWriter writer = new IndexWriter(dir,indexWriterConfig);
    for(int i=0;i<10;i++) {
      Document doc = new Document();

      //Field.Index.NOT_ANALYZED_NO_NORMS deprecated
      FieldType textType = new FieldType();
      textType.setIndexOptions(IndexOptions.DOCS_AND_FREQS_AND_POSITIONS_AND_OFFSETS);
      textType.setStored(true);
      textType.setTokenized(true);

      doc.add(new Field("id", ""+i, textType));
      doc.add(new Field("text", "aaa", textType));
      writer.addDocument(doc);
    }

    /**
     * 주의 사항
     * LUCENE-2691: The near-real-time API has moved from IndexWriter to DirectoryReader. Instead of IndexWriter.getReader(), call DirectoryReader.open(IndexWriter) or DirectoryReader.openIfChanged(IndexWriter).
     */
    //IndexReader reader = reader.open(writer);

    DirectoryReader reader = DirectoryReader.open(writer);            //#1 근실시간 리더 생성
    IndexSearcher searcher = new IndexSearcher(reader);           //#A 검색서처에서 리더를 래핑

    Query query = new TermQuery(new Term("text", "aaa"));
    TopDocs docs = searcher.search(query, 1);
    assertEquals(10, docs.totalHits);                        //#B 검색 결과 10개 리턴됨

    writer.deleteDocuments(new Term("id", "7"));             //#2 문서 1개 삭제

    Document doc = new Document();                           // #3 문서 1개 추가
    FieldType textType = new FieldType();
    textType.setIndexOptions(IndexOptions.DOCS_AND_FREQS_AND_POSITIONS_AND_OFFSETS);
    textType.setStored(true);
    textType.setTokenized(true);

    doc.add(new Field("id",                            // #3 문서 1개 추가
                      "11",textType));                 // #3 문서 1개 추가
    doc.add(new Field("text",                          // #3 문서 1개 추가
                      "bbb",textType));                // #3 문서 1개 추가
    writer.addDocument(doc);

    /**주의 사항
    IndexReader.reopen has been renamed to DirectoryReader.openIfChanged (a static method), and now returns null
     (instead of the old reader) if there are no changes to the index, to prevent the common pitfall of accidentally
     closing the old reader.
     **/
    //IndexReader newReader = reader.reopen();                        // #4 리더를 재오픈함
    IndexReader newReader = DirectoryReader.openIfChanged(reader);
    assertFalse(reader == newReader);                        //#5 현재 리더가 새로운 리더임
    reader.close();                                                   // #6 옛리더는 닫음
    searcher = new IndexSearcher(newReader);              

    TopDocs hits = searcher.search(query, 10);                    // #7 9개 리턴됨
    assertEquals(9, hits.totalHits);                         // #79개 리턴됨

    query = new TermQuery(new Term("text", "bbb"));          //  #8 새로운 문서 확인
    hits = searcher.search(query, 1);                              //  #8 새로운 문서 확인
    assertEquals(1, hits.totalHits);                         //  #8 새로운 문서 확인

    newReader.close();
    writer.close();
  }
}

