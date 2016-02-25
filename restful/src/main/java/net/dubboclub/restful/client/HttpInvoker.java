package net.dubboclub.restful.client;

import com.alibaba.dubbo.rpc.RpcException;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicHeader;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.util.Map;

/**
 * @date: 2016/2/26.
 * @author:bieber.
 * @project:dubbo-plus.
 * @package:net.dubboclub.restful.http.
 * @version:1.0.0
 * @fix:
 * @description: HTTP调用代理
 */
public class HttpInvoker {

    private static final CloseableHttpClient httpclient = HttpClients.createDefault();

    public static byte[] post(String url,byte[] requestContent,Map<String,String> headerMap) throws IOException {
        HttpPost httpPost = new HttpPost(url);
        if(requestContent!=null){
            HttpEntity httpEntity = new ByteArrayEntity(requestContent);
            httpPost.setEntity(httpEntity);
        }
        if(headerMap!=null){
            Header[] headers = new Header[headerMap.size()];
            int index=0;
            for(Map.Entry<String,String> entry:headerMap.entrySet()){
                Header header = new BasicHeader("protocol_"+entry.getKey(),entry.getValue());
                headers[index]=header;
                index++;
            }
            httpPost.setHeaders(headers);
        }
        CloseableHttpResponse response =  httpclient.execute(httpPost);
        int responseCode = response.getStatusLine().getStatusCode();
        if(responseCode==200){
            HttpEntity responseEntity = response.getEntity();
            if(responseEntity!=null){
                return EntityUtils.toByteArray(responseEntity);
            }
        }else if(responseCode==404){
            throw new RpcException(RpcException.UNKNOWN_EXCEPTION,"not found service for url ["+url+"]");
        }else if(responseCode==500){
            throw new RpcException(RpcException.NETWORK_EXCEPTION,"occur an exception at server end.");
        }
        return null;
    }
}
