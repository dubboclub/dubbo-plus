package net.dubboclub.protocol.akka;

import com.alibaba.dubbo.common.URL;
import com.alibaba.dubbo.rpc.Exporter;
import com.alibaba.dubbo.rpc.Invoker;
import com.alibaba.dubbo.rpc.RpcException;
import com.alibaba.dubbo.rpc.protocol.AbstractProtocol;

/**
 * Created by bieber on 2015/7/8.
 */
public class AkkaProtocol extends AbstractProtocol {
    
    private static final String AKKA_TRANSPORTER_NAME="akka";
    
    private static final int DEFAULT_PORT=2552;
    
    @Override
    public int getDefaultPort() {
        return DEFAULT_PORT;
    }

    @Override
    public <T> Exporter<T> export(Invoker<T> invoker) throws RpcException {
        return null;
    }

    @Override
    public <T> Invoker<T> refer(Class<T> type, URL url) throws RpcException {
        return null;
    }
    
    
    
    
}
