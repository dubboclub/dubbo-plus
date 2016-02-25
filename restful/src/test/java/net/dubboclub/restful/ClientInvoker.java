package net.dubboclub.restful;

import com.alibaba.fastjson.JSON;
import net.dubboclub.restful.api.FirstRestfulService;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.junit.Test;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * @date: 2016/2/25.
 * @author:bieber.
 * @project:dubbo-plus.
 * @package:net.dubboclub.restful.
 * @version:1.0.0
 * @fix:
 * @description: 描述功能
 */
public class ClientInvoker {

    @Test
    public void invokeSayHello(){
        CloseableHttpClient httpclient = HttpClients.createDefault();
        HttpPost httpPost = new HttpPost("http://localhost:8080/"+ FirstRestfulService.class.getName()+"/sayHello");
        Map<String,String> requestEntity = new HashMap<String,String>();
        requestEntity.put("arg1","Bieber");
        HttpEntity httpEntity = new ByteArrayEntity(JSON.toJSONBytes(requestEntity));
        httpPost.setEntity(httpEntity);
        try {
            CloseableHttpResponse response =  httpclient.execute(httpPost);
            System.out.println(response.getStatusLine());
            HttpEntity entity2 = response.getEntity();
            // do something useful with the response body
            // and ensure it is fully consumed
            System.out.println(EntityUtils.toString(entity2));
            response.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
