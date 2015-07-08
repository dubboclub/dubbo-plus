package net.dubboclub.akka.remoting;

import com.alibaba.dubbo.common.Constants;
import com.alibaba.dubbo.common.URL;
import com.alibaba.dubbo.remoting.Channel;
import com.alibaba.dubbo.remoting.ChannelHandler;
import com.alibaba.dubbo.remoting.RemotingException;
import com.alibaba.dubbo.remoting.transport.AbstractServer;
import net.dubboclub.akka.core.ActorSystemBootstrap;
import net.dubboclub.akka.core.AkkaContextHandler;

import java.net.InetSocketAddress;
import java.util.Collection;

/**
 * Created by bieber on 2015/7/8.
 */
public class AkkaServer extends AbstractServer {

    private ActorSystemBootstrap actorSystemBootstrap ;

    public AkkaServer(URL url, ChannelHandler handler) throws RemotingException {
        super(url, handler);
        actorSystemBootstrap = new ActorSystemBootstrap(getUrl().getParameter(Constants.APPLICATION_KEY));
        AkkaContextHandler.setActorSystemBootstrap(actorSystemBootstrap);
    }

    @Override
    protected void doOpen() throws Throwable {
        actorSystemBootstrap.start(getUrl());
    }

    @Override
    protected void doClose() throws Throwable {
        actorSystemBootstrap.shutdown();
        AkkaContextHandler.clean();
    }

    @Override
    public boolean isBound() {
        return actorSystemBootstrap.isStarted();
    }

    @Override
    public Collection<Channel> getChannels() {
        throw new UnsupportedOperationException("akka protocol is unsupport channel");
    }

    @Override
    public Channel getChannel(InetSocketAddress remoteAddress) {
        throw new UnsupportedOperationException("akka protocol is unsupport channel");
    }
}
