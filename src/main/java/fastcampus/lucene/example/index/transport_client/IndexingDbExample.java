package fastcampus.lucene.example.index.transport_client;

import fastcampus.lucene.example.database.MysqlConnect;


import org.elasticsearch.action.bulk.BulkRequestBuilder;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.TransportAddress;
import org.elasticsearch.transport.client.PreBuiltTransportClient;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;


public class IndexingDbExample {

    private IndexingDbExample() {
    }

    /**
     * 루씬 색인 예제 파일
     */
    public static void main(String[] args) throws UnknownHostException {

        //기본 설정
        Settings settings = Settings.builder()
                .put("cluster.name", "my-application")
                .build();
        TransportClient client = new PreBuiltTransportClient(settings)
                .addTransportAddress(new TransportAddress(InetAddress.getByName("localhost"), 9300))
                .addTransportAddress(new TransportAddress(InetAddress.getByName("localhost"), 9300));


        //데이터 로딩
        MysqlConnect mysqlConnect = new MysqlConnect();
        ResultSet resultSet = null;

        String sql = "SELECT * FROM  NewsData LIMIT 1000";
        try {
            PreparedStatement statement = mysqlConnect.connect().prepareStatement(sql);
            resultSet = statement.executeQuery();
            if (resultSet != null) {
                BulkRequestBuilder bulkBuilder = client.prepareBulk();  //벌크 인서트 빌더
                JSONParser parser = new JSONParser();

                long bulkBuilderLength = 0;
                String index = "news";
                String type = "article";
                String _id = null;
                JSONObject articleInfo = null;

                while (resultSet.next()) {

                    articleInfo = new JSONObject();
                    _id = String.valueOf(resultSet.getString("id"));    //id값을 명시적으로 입력

                    //데이터 입력
                    articleInfo.put("title", resultSet.getString("title"));
                    articleInfo.put("link", resultSet.getString("link"));
                    articleInfo.put("description", resultSet.getString("description"));
                    articleInfo.put("author", resultSet.getString("author"));
                    articleInfo.put("media", resultSet.getString("media"));
                    articleInfo.put("category", resultSet.getString("category"));


                    bulkBuilder.add(client.prepareIndex(index, type, String.valueOf(_id)).setSource(articleInfo));   //벌크 인서트 1개 row를 입력
                    bulkBuilderLength++;


                    try {
                        if(bulkBuilderLength % 100== 0){    //100개씩 분할해서
                            System.out.println("#####" + bulkBuilderLength + " data indexed.");
                            //프로그램 실행
                            BulkResponse bulkRes = bulkBuilder.execute().actionGet();
                            if(bulkRes.hasFailures()){
                                System.out.println("##### Bulk Request failure with error:" + bulkRes.buildFailureMessage());

                            }
                            bulkBuilder = client.prepareBulk();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
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
