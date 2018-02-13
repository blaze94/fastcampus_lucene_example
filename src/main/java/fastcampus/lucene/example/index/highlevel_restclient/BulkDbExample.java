package fastcampus.lucene.example.index.highlevel_restclient;

import fastcampus.lucene.example.database.MysqlConnect;
import org.apache.http.HttpHost;
import org.elasticsearch.action.ActionListener;
import org.elasticsearch.action.DocWriteResponse;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkRequestBuilder;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.support.WriteRequest;
import org.elasticsearch.action.support.replication.ReplicationResponse;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.elasticsearch.common.xcontent.XContentType;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;


public class BulkDbExample {

    private BulkDbExample() {
    }

    /**
     * 루씬 색인 예제 파일
     */
    public static void main(String[] args) throws IOException {

        //기본 설정
        RestHighLevelClient client = new RestHighLevelClient(
                RestClient.builder(
                        new HttpHost("localhost", 9200, "http"),
                        new HttpHost("localhost", 9200, "http")));  //high level 클라이언트는 low level client 인스턴스를 받는다



        //데이터 로딩
        MysqlConnect mysqlConnect = new MysqlConnect();
        ResultSet resultSet = null;

        String sql = "SELECT * FROM  NewsData LIMIT 1000";
        try {
            PreparedStatement statement = mysqlConnect.connect().prepareStatement(sql);
            resultSet = statement.executeQuery();
            if (resultSet != null) {

                BulkRequest request = new BulkRequest();
                //옵션들
                request.timeout("2m");
                request.setRefreshPolicy(WriteRequest.RefreshPolicy.WAIT_UNTIL);

                long bulkBuilderLength = 0;
                String index = "news";
                String type = "article";
                String _id = null;
                JSONObject articleInfo = null;

                while (resultSet.next()) {
                    //데이터 입력
                    articleInfo = new JSONObject();
                    articleInfo.put("title", resultSet.getString("title"));
                    articleInfo.put("link", resultSet.getString("link"));
                    articleInfo.put("description", resultSet.getString("description"));
                    articleInfo.put("author", resultSet.getString("author"));
                    articleInfo.put("media", resultSet.getString("media"));
                    articleInfo.put("category", resultSet.getString("category"));

                    request.add(new IndexRequest(index, type, resultSet.getString("id"))
                            .source(articleInfo));

                    // 삭제나 수정 요청도 같이 넣을 수 있음
                    // request.add(new DeleteRequest("posts", "doc", "3"));
                    //request.add(new UpdateRequest("posts", "doc", "2").doc(XContentType.JSON,"other", "test"));

                    bulkBuilderLength++;
                }

                //실행방법 : 동기 실행
                BulkResponse bulkResponse = client.bulk(request);

                if(bulkResponse.hasFailures()) {
                    System.err.println(bulkResponse.buildFailureMessage());
                } else {
                    System.out.println("Bulk indexing succeeded.");
                }

            }

        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            mysqlConnect.disconnect();
        }

        client.close();


    }
}
