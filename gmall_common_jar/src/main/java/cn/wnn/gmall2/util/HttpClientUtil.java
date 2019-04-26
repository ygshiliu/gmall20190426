package cn.wnn.gmall2.util;

import org.apache.http.HttpEntity;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import java.io.IOException;

/**
 * Created by Administrator on 2018/5/17 0017.
 */
public class HttpClientUtil {

    public static String doGet(String url){
        CloseableHttpClient httpClient = HttpClients.createDefault();
        HttpGet httpGet = new HttpGet(url);

        try {
            CloseableHttpResponse response = httpClient.execute(httpGet);
            if(response.getStatusLine().getStatusCode()== HttpStatus.SC_OK){
                HttpEntity entity =response.getEntity();
                String str = EntityUtils.toString(entity, "utf-8");
                System.out.println("httputils==========="+str);
                EntityUtils.consume(entity);
                httpClient.close();
                return str;
            }
                httpClient.close();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
            return null;
    }
}
