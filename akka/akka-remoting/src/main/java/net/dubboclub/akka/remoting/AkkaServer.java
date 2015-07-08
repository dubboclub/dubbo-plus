package net.dubboclub.akka.remoting;

import com.alibaba.dubbo.common.URL;
import com.alibaba.dubbo.remoting.Channel;
import com.alibaba.dubbo.remoting.ChannelHandler;
import com.alibaba.dubbo.remoting.RemotingException;
import com.alibaba.dubbo.remoting.transport.AbstractServer;

import java.net.InetSocketAddress;
import java.util.Collection;

/**
 * Created by bieber on 2015/7/8.
 */
public class AkkaServer extends AbstractServer {
    public AkkaServer(URL url, ChannelHandler handler) throws RemotingException {
        super(url, handler);
    }

    @Override
    protected void doOpen() throws Throwable {

    }

    @Override
    protected void doClose() throws Throwable {

    }

    @Override
    public boolean isBound() {
        return false;
    }

    @Override
    public Collection<Channel> getChannels() {
        return null;
    }

    @Override
    public Channel getChannel(InetSocketAddress remoteAddress) {
        return null;
    }
}
