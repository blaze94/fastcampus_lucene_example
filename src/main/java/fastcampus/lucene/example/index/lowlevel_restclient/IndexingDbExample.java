package fastcampus.lucene.example.index.lowlevel_restclient;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.RequestLine;
import org.apache.http.entity.ContentType;
import org.apache.http.message.BasicHeader;
import org.apache.http.nio.entity.NStringEntity;
import org.apache.http.util.EntityUtils;
import org.elasticsearch.client.Response;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.Collections;
import java.util.Map;


public class IndexingDbExample {

    private IndexingDbExample() {
    }

    /**
     * 루씬 색인 예제 파일
     */
    public static void main(String[] args) throws IOException {

        //기본 설정
        RestClient restClient = RestClient.builder(
                new HttpHost("localhost", 9200, "http"),
                new HttpHost("localhost", 9200, "http")).build();


        RestClientBuilder builder = RestClient.builder(new HttpHost("localhost", 9200, "http"));
        Header[] defaultHeaders = new Header[]{new BasicHeader("header", "value")};
        builder.setDefaultHeaders(defaultHeaders);
        builder.setMaxRetryTimeoutMillis(10000);


        //실패했을때 실행되는 리스너
        builder.setFailureListener(new RestClient.FailureListener() {
            @Override
            public void onFailure(HttpHost host) {

            }
        });



        //일반적으로 1개의 데이터를 입력시
        Map<String, String> params = Collections.emptyMap();
        String jsonString = "{" +
                "\"user\":\"kimchy\"," +
                "\"postDate\":\"2013-01-30\"," +
                "\"message\":\"trying out Elasticsearch\"" +
                "}";
        HttpEntity entity = new NStringEntity(jsonString, ContentType.APPLICATION_JSON);
        Response response = restClient.performRequest("PUT", "/news/article/1", params, entity);


        //응답받은 요청 받기
        RequestLine requestLine = response.getRequestLine();
        HttpHost host = response.getHost();
        int statusCode = response.getStatusLine().getStatusCode();
        Header[] headers = response.getHeaders();
        String responseBody = EntityUtils.toString(response.getEntity());



        restClient.close();

    }
}
