package fastcampus.lucene.example.index;

import fastcampus.lucene.example.database.MysqlConnect;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.*;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.index.Term;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;


public class IndexingDbExample {

    private IndexingDbExample() {
    }

    /**
     * 루씬 색인 예제 파일
     */
    public static void main(String[] args) {
        String usage = "java org.apache.lucene.demo.IndexFiles"
                + " [-index INDEX_PATH] [-docs DOCS_PATH] [-update]\n\n"
                + "인덱스 경로  문서 경로 업데이트 유무 "
                + "SearchFiles 로 검색";

        String indexPath = "./index";         //기본 index 패스
        String docsPath = null;
        boolean create = true;                //생성모드인지 추가 모드인지

        for (int i = 0; i < args.length; i++) {
            if ("-index".equals(args[i])) {
                indexPath = args[i + 1];
                i++;
            } else if ("-docs".equals(args[i])) {
                docsPath = args[i + 1];
                i++;
            } else if ("-update".equals(args[i])) {
                create = false;
            }
        }

        if (docsPath == null) {
            System.err.println("Usage: " + usage);
            System.exit(1);
        }

        final Path docDir = Paths.get(docsPath);
        if (!Files.isReadable(docDir)) {
            System.out.println("Document directory '" + docDir.toAbsolutePath() + "' does not exist or is not readable, please check the path");
            System.exit(1);
        }

        Date start = new Date();
        try {
            System.out.println("인덱스 디렉토리 문서를 색인 합니다. '" + indexPath + "'...");

            Directory dir = FSDirectory.open(Paths.get(indexPath));
            Analyzer analyzer = new StandardAnalyzer();                 //기본 스탠다드분석기를 사용함
            IndexWriterConfig indexWriterConfig = new IndexWriterConfig(analyzer);  //인덱스Writer의 설정을 지정하는 클래스

            if (create) {
                // 새로운 인덱스를 생성하고 기존의 문서를 삭제함
                indexWriterConfig.setOpenMode(OpenMode.CREATE);
            } else {
                // 기존 인덱스에 새도큐먼트를 추가함
                indexWriterConfig.setOpenMode(OpenMode.CREATE_OR_APPEND);
            }

            //indexWriterConfig.setUseCompoundFile(false); //다중 파일 색인 생성시  !!!!!!!

            // 생인성능을 위해 램버퍼를 지정할수 있음
            // 많은 수의 문서를 색인 할 경우 램 버퍼값을 추가해 주면 좋음
            // 단 힙사이즈는 넘기면 안됨
            // indexWriterConfig.setRAMBufferSizeMB(256.0);

            IndexWriter writer = new IndexWriter(dir, indexWriterConfig);
            indexDatabase(writer);  //데이터 베이스에서 색인


            //색인 성능을 위해서 색인 종료후
            // 강제 병합을 할 수 있음
            // writer.forceMerge(1);

            writer.close();

            Date end = new Date();
            System.out.println(end.getTime() - start.getTime() + " total milliseconds");

        } catch (IOException e) {
            System.out.println(" caught a " + e.getClass() +
                    "\n with message: " + e.getMessage());
        }
    }


    /**데이터 베이스에서 직접 색인 함
    *샘플 데이터베이스 구조
    *-----------------------------------------------------------
    *| id |title|link|description|author|media|category|modDate|
    *-----------------------------------------------------------
    **/
    static void indexDatabase(final IndexWriter writer) throws IOException {
        MysqlConnect mysqlConnect = new MysqlConnect();
        ResultSet resultSet = null;

        String sql = "SELECT * FROM  NewsData LIMIT 1000";
        try {
            PreparedStatement statement = mysqlConnect.connect().prepareStatement(sql);

            resultSet = statement.executeQuery();
            if (resultSet != null) {
                while(resultSet.next()){
                    try {
                        Document doc = new Document();
                        doc.add(new StringField("id", resultSet.getString("id"), Field.Store.YES));
                        doc.add(new StringField("title", resultSet.getString("title"), Field.Store.YES));
                        doc.add(new StringField("link", resultSet.getString("link"), Field.Store.YES));
                        doc.add(new StringField("description", resultSet.getString("description"), Field.Store.YES));
                        doc.add(new StringField("author", resultSet.getString("author"), Field.Store.YES));
                        doc.add(new StringField("media", resultSet.getString("media"), Field.Store.YES));
                        doc.add(new StringField("category", resultSet.getString("category"), Field.Store.YES));

                        if (writer.getConfig().getOpenMode() == OpenMode.CREATE) {
                            // 새로운 색인의 경우 새로 색인을 생성함
                            System.out.println("adding " + resultSet.getString("id"));
                            writer.addDocument(doc);
                        } else {
                            //이미 색인이 존재하고 업데이트인 경우 색인을 추가함
                            System.out.println("updating " + resultSet.getString("id"));
                            writer.updateDocument(new Term("path", resultSet.getString("id")), doc);
                        }

                    } catch(IllegalArgumentException ex) {
                        //ex.printStackTrace();
                    }
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            mysqlConnect.disconnect();
        }
    }
}
