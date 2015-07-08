package net.dubboclub.protocol.akka;

import com.alibaba.dubbo.common.URL;
import com.alibaba.dubbo.common.extension.ExtensionLoader;
import com.alibaba.dubbo.remoting.Channel;
import com.alibaba.dubbo.remoting.RemotingException;
import com.alibaba.dubbo.remoting.Transporter;
import com.alibaba.dubbo.remoting.exchange.ExchangeChannel;
import com.alibaba.dubbo.remoting.exchange.ExchangeHandler;
import com.alibaba.dubbo.remoting.exchange.ExchangeServer;
import com.alibaba.dubbo.remoting.exchange.Exchangers;
import com.alibaba.dubbo.rpc.Exporter;
import com.alibaba.dubbo.rpc.Invoker;
import com.alibaba.dubbo.rpc.RpcException;
import com.alibaba.dubbo.rpc.protocol.AbstractProtocol;

import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by bieber on 2015/7/8.
 */
public class AkkaProtocol extends AbstractProtocol {
    
    private static final String AKKA_TRANSPORTER_NAME="akka";
    
    private static final int DEFAULT_PORT=2552;

    private static ConcurrentHashMap<String ,ExchangeServer> serverMap = new ConcurrentHashMap<String, ExchangeServer>();



    private static ExchangeHandler requestHandler = new ExchangeHandler() {
        @Override
        public Object reply(ExchangeChannel channel, Object request) throws RemotingException {

            return null;
        }

        @Override
        public void connected(Channel channel) throws RemotingException {

        }

        @Override
        public void disconnected(Channel channel) throws RemotingException {

        }

        @Override
        public void sent(Channel channel, Object message) throws RemotingException {

        }

        @Override
        public void received(Channel channel, Object message) throws RemotingException {

        }

        @Override
        public void caught(Channel channel, Throwable exception) throws RemotingException {

        }

        @Override
        public String telnet(Channel channel, String message) throws RemotingException {
            return null;
        }
    };

    
    @Override
    public int getDefaultPort() {
        return DEFAULT_PORT;
    }

    @Override
    public <T> Exporter<T> export(Invoker<T> invoker) throws RpcException {
        URL url = invoker.getUrl();
        openServer(url);
        return new AkkaExporter<T>(invoker);
    }

    private void openServer(URL url){
        String key = url.getAddress();
        if(!ExtensionLoader.getExtensionLoader(Transporter.class).hasExtension(AKKA_TRANSPORTER_NAME)){
            throw new RpcException("Not support current service type "+AKKA_TRANSPORTER_NAME+" current supported service types "+ExtensionLoader.getExtensionLoader(Transporter.class).getSupportedExtensions());
        }
        ExchangeServer server = serverMap.get(key);
        if(server==null){
            try {
                serverMap.putIfAbsent(key, Exchangers.bind(url, requestHandler));
            } catch (RemotingException e) {
                throw new RpcException("Failed to open akka server",e);
            }
        }else{
            server.reset(url);
        }
    }



    @Override
    public <T> Invoker<T> refer(Class<T> type, URL url) throws RpcException {
        return null;
    }
    
    
    
    
}
