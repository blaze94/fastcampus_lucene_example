package fastcampus.lucene.example.index;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;

import fastcampus.lucene.example.database.MysqlConnect;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.*;
import org.apache.lucene.index.*;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;


public class IndexingExample {

    private IndexingExample() {
    }

    /**
     * 루씬 색인 예제 파일
     */
    public static void main(String[] args) {

        String indexPath = "./index";         //기본 index 패스
        String docsPath = "./data/books.csv";
        boolean create = false;                //생성모드인지 추가 모드인지

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
                indexWriterConfig.setOpenMode(OpenMode.APPEND);
            }
            indexWriterConfig.setUseCompoundFile(false); //다중 파일 색인 생성시  !!!!!!!


//            MergePolicy mergePolicy = new TieredMergePolicy();
//            mergePolicy.setMaxCFSSegmentSizeMB(300);
//            indexWriterConfig.setMergePolicy(mergePolicy);

            // 생인성능을 위해 램버퍼를 지정할수 있음
            // 많은 수의 문서를 색인 할 경우 램 버퍼값을 추가해 주면 좋음
            // 단 힙사이즈는 넘기면 안됨
            // indexWriterConfig.setRAMBufferSizeMB(256.0);

            IndexWriter writer = new IndexWriter(dir, indexWriterConfig);


            indexDocs(writer, docDir); //문서를 색인함
            //indexDatabase(writer);  //데이터 베이스에서 색인


            //색인 성능을 위해서 색인 종료후
            // 강제 병합을 할 수 있음
            // writer.forceMerge(1);
            //writer.commit();
            //writer.close();

            Date end = new Date();
            System.out.println(end.getTime() - start.getTime() + " total milliseconds");

        } catch (IOException e) {
            System.out.println(" caught a " + e.getClass() +
                    "\n with message: " + e.getMessage());
        }
    }


    //data 경로에서 파일을 찾아내어 색인 함
    static void indexDocs(final IndexWriter writer, Path path) throws IOException {
        if (Files.isDirectory(path)) {
            Files.walkFileTree(path, new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    try {
                        indexDocForCsv(writer, file, attrs.lastModifiedTime().toMillis());
                    } catch (IOException ignore) {
                        // don't index files that can't be read.
                    }
                    return FileVisitResult.CONTINUE;
                }
            });
        } else {
            indexDoc(writer, path, Files.getLastModifiedTime(path).toMillis());
        }
    }

    /**
     * 문서하나를 하나의 Doc으로 보면서 색인
     */
    static void indexDoc(IndexWriter writer, Path file, long lastModified) throws IOException {
        try (InputStream stream = Files.newInputStream(file)) {
            // 빈 새로운 문서 생성
            Document doc = new Document();

            // 파일 경로 저장
            Field pathField = new StringField("path", file.toString(), Field.Store.YES);
            doc.add(pathField);

            //파일 최종 수정일자 저장
            doc.add(new LongPoint("modified", lastModified));

            // 파일 내용 저장
            doc.add(new TextField("contents", new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8))));

            if (writer.getConfig().getOpenMode() == OpenMode.CREATE) {
                // 새로운 색인의 경우 새로 색인을 생성함
                System.out.println("adding " + file);
                writer.addDocument(doc);
            } else {
                //이미 색인이 존재하고 업데이트인 경우 색인을 추가함
                System.out.println("updating " + file);
                writer.updateDocument(new Term("path", file.toString()), doc);
            }
        }
    }

    /**
     * 하나의 문서안에 1줄을 1개 Doc으로 생각하고 생인
     * CSV파일 구조
     * *---------------------------------------------------------
     *| id |cat|name|price|inStock|author|series|sequence| genre|
     *-----------------------------------------------------------
    */
    static void indexDocForCsv(IndexWriter writer, Path file, long lastModified) throws IOException {
        String csvFile ="./data/books.csv";
        String line = "";
        String cvsSplitBy = ",";

        try (BufferedReader br = new BufferedReader(new FileReader(csvFile))) {
            while ((line = br.readLine()) != null) {
                Document doc = new Document();
                // use comma as separator

                String[] data = line.split(cvsSplitBy);
                Field idField = new StringField("id", data[0], Field.Store.YES);
                Field catField = new StringField("cat", data[1], Field.Store.YES);
                Field nameField = new StringField("name", data[2], Field.Store.YES);
                Field priceField = new FloatDocValuesField("price",Float.parseFloat(data[3]));
                Field inStockField = new StringField("instock", data[4], Field.Store.YES);
                Field authorField = new StringField("author", data[5], Field.Store.YES);
                Field seriesField = new StringField("series", data[6], Field.Store.YES);
                Field genreField = new StringField("genre", data[8], Field.Store.YES);

                doc.add(idField);
                doc.add(catField);
                doc.add(nameField);
                doc.add(priceField);
                doc.add(inStockField);
                doc.add(authorField);
                doc.add(seriesField);
                doc.add(genreField);

                if (writer.getConfig().getOpenMode() == OpenMode.CREATE) {
                    // 새로운 색인의 경우 새로 색인을 생성함
                    System.out.println("adding " + file);
                    writer.addDocument(doc);
                } else {
                    //이미 색인이 존재하고 업데이트인 경우 색인을 추가함
                    System.out.println("updating " + file);
                    writer.updateDocument(new Term("path", file.toString()), doc);
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }


    }

}
