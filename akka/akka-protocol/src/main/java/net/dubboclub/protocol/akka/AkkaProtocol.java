package net.dubboclub.protocol.akka;

import akka.actor.Actor;
import com.alibaba.dubbo.common.Constants;
import com.alibaba.dubbo.common.URL;
import com.alibaba.dubbo.common.extension.ExtensionLoader;
import com.alibaba.dubbo.remoting.exchange.ExchangeServer;
import com.alibaba.dubbo.rpc.Exporter;
import com.alibaba.dubbo.rpc.Invoker;
import com.alibaba.dubbo.rpc.RpcException;
import com.alibaba.dubbo.rpc.protocol.AbstractProtocol;
import net.dubboclub.akka.remoting.ActorExchanger;

import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by bieber on 2015/7/8.
 */
public class AkkaProtocol extends AbstractProtocol {
    
    private static final String AKKA_TRANSPORTER_NAME="akka";
    
    private static final int DEFAULT_PORT=2552;
    
    private static volatile ActorExchanger exchanger = null;

    private static ConcurrentHashMap<String ,ExchangeServer> serverMap = new ConcurrentHashMap<String, ExchangeServer>();


    @Override
    public int getDefaultPort() {
        return DEFAULT_PORT;
    }

    @Override
    public <T> Exporter<T> export(Invoker<T> invoker) throws RpcException {
        URL url = invoker.getUrl();
        return new AkkaExporter<T>(invoker,getExchanger().bind(invoker));
    }
    
    private ActorExchanger  getExchanger(){
        if(!ExtensionLoader.getExtensionLoader(ActorExchanger.class).hasExtension(AKKA_TRANSPORTER_NAME)){
            throw new RpcException("Not found "+AKKA_TRANSPORTER_NAME+" ActorExchanger extension in ExtensionLoader");
        }
        if(exchanger ==null){
            exchanger = ExtensionLoader.getExtensionLoader(ActorExchanger.class).getExtension(AKKA_TRANSPORTER_NAME);
        }
        return exchanger;
    }

    @Override
    public <T> Invoker<T> refer(Class<T> type, URL url) throws RpcException {
        return new AkkaInvoker<T>(getExchanger().connect(type,url),url,type);
    }

}
