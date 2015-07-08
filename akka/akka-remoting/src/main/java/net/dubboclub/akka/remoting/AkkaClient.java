package net.dubboclub.akka.remoting;

import com.alibaba.dubbo.common.URL;
import com.alibaba.dubbo.remoting.Channel;
import com.alibaba.dubbo.remoting.ChannelHandler;
import com.alibaba.dubbo.remoting.RemotingException;
import com.alibaba.dubbo.remoting.transport.AbstractClient;

/**
 * Created by bieber on 2015/7/8.
 */
public class AkkaClient extends AbstractClient {
    
    public AkkaClient(URL url, ChannelHandler handler) throws RemotingException {
        super(url, handler);
    }

    @Override
    protected void doOpen() throws Throwable {

    }

    @Override
    protected void doClose() throws Throwable {

    }

    @Override
    protected void doConnect() throws Throwable {

    }

    @Override
    protected void doDisConnect() throws Throwable {

    }

    @Override
    protected Channel getChannel() {
        return null;
    }
}
