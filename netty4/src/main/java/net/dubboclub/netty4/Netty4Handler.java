package net.dubboclub.netty4;

import com.alibaba.dubbo.common.URL;
import com.alibaba.dubbo.common.utils.NetUtils;
import com.alibaba.dubbo.remoting.Channel;
import com.alibaba.dubbo.remoting.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import java.net.InetSocketAddress;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by bieber on 2015/10/8.
 */
public class Netty4Handler extends SimpleChannelInboundHandler {

    private final Map<String, Channel> channels = new ConcurrentHashMap<String, Channel>();

    private final URL url;

    private final ChannelHandler handler;

    public Netty4Handler(URL url, ChannelHandler handler) {
        if (url == null) {
            throw new IllegalArgumentException("url == null");
        }
        if (handler == null) {
            throw new IllegalArgumentException("handler == null");
        }
        this.url = url;
        this.handler = handler;
    }


    public Map<String, Channel> getChannels() {
        return channels;
    }


    @Override
    public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
        Netty4Channel channel = Netty4Channel.getOrAddChannel(ctx.channel(), url, handler);
        try {
            if (channel != null) {
                channels.put(NetUtils.toAddressString((InetSocketAddress) ctx.channel().remoteAddress()), channel);
            }
            handler.connected(channel);
        } finally {
            Netty4Channel.removeChannelIfDisconnected(ctx.channel());
        }
    }

    @Override
    public void channelUnregistered(ChannelHandlerContext ctx) throws Exception {
        Netty4Channel channel = Netty4Channel.getOrAddChannel(ctx.channel(), url, handler);
        try {
            channels.remove(NetUtils.toAddressString((InetSocketAddress) ctx.channel().remoteAddress()));
            handler.disconnected(channel);
        } finally {
            Netty4Channel.removeChannelIfDisconnected(ctx.channel());
        }
    }
    
    

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        Netty4Channel channel = Netty4Channel.getOrAddChannel(ctx.channel(), url, handler);
        try {
            handler.caught(channel, cause.getCause());
        } finally {
            Netty4Channel.removeChannelIfDisconnected(ctx.channel());
        }
    }

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, Object o) throws Exception {
        Netty4Channel channel = Netty4Channel.getOrAddChannel(channelHandlerContext.channel(), url, handler);
        try {
            handler.received(channel, o);
        } finally {
            Netty4Channel.removeChannelIfDisconnected(channelHandlerContext.channel());
        }
    }

}
