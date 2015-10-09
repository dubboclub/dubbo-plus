package net.dubboclub.netty4;

import com.alibaba.dubbo.common.Constants;
import com.alibaba.dubbo.common.URL;
import com.alibaba.dubbo.remoting.ChannelHandler;
import com.alibaba.dubbo.remoting.Codec2;
import com.alibaba.dubbo.remoting.buffer.ChannelBuffer;
import com.alibaba.dubbo.remoting.buffer.ChannelBuffers;
import com.alibaba.dubbo.remoting.buffer.DynamicChannelBuffer;
import com.alibaba.dubbo.rpc.RpcContext;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandler;
import io.netty.channel.ChannelOutboundHandler;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.MessageToByteEncoder;

import java.io.IOException;

/**
 * Created by bieber on 2015/10/8.
 */
public class Netty4CodecAdapter {

    private final ChannelOutboundHandler encoder = new InternalEncoder();

    private final ChannelInboundHandler  decoder = new InternalDecoder();

    private final Codec2 codec;

    private final URL url;

    private final int    bufferSize;

    private final ChannelHandler handler;

    public Netty4CodecAdapter(Codec2 codec, URL url, ChannelHandler handler) {
        this.codec = codec;
        this.url = url;
        this.handler = handler;
        int b = url.getPositiveParameter(Constants.BUFFER_KEY, Constants.DEFAULT_BUFFER_SIZE);
        this.bufferSize = b >= Constants.MIN_BUFFER_SIZE && b <= Constants.MAX_BUFFER_SIZE ? b : Constants.DEFAULT_BUFFER_SIZE;
    }
    public io.netty.channel.ChannelHandler getEncoder() {
        return encoder;
    }

    public io.netty.channel.ChannelHandler getDecoder() {
        return decoder;
    }
    
    private class InternalEncoder extends MessageToByteEncoder<ChannelBuffer>{
        //encode值是将ChannelBuffer复制到netty的bytebuf中
        @Override
        protected void encode(ChannelHandlerContext channelHandlerContext, ChannelBuffer message, ByteBuf byteBuf) throws Exception {
            byteBuf.writeBytes(message.toByteBuffer());
        }
    }

    private class InternalDecoder extends SimpleChannelInboundHandler{

        private com.alibaba.dubbo.remoting.buffer.ChannelBuffer buffer =
                com.alibaba.dubbo.remoting.buffer.ChannelBuffers.EMPTY_BUFFER;


        @Override
        protected void channelRead0(ChannelHandlerContext channelHandlerContext, Object o) throws Exception {
            if (! (o instanceof ByteBuf)) {
                channelHandlerContext.fireChannelRead(o);
                return;
            }
            ByteBuf input = (ByteBuf) o;
            int readable = input.readableBytes();
            if (readable <= 0) {
                return;
            }
            byte[] bytes= new byte[readable];
            input.readBytes(bytes);
            ChannelBuffer message;
            if (buffer.readable()) {
                if (buffer instanceof DynamicChannelBuffer) {
                    buffer.writeBytes(bytes);
                    message = buffer;
                } else {
                    int size = buffer.readableBytes() + input.readableBytes();
                    message = ChannelBuffers.dynamicBuffer(
                            size > bufferSize ? size : bufferSize);
                    message.writeBytes(buffer, buffer.readableBytes());
                    message.writeBytes(bytes);
                }
            } else {
                message = ChannelBuffers.wrappedBuffer(
                        bytes);
            }
            Netty4Channel channel = Netty4Channel.getOrAddChannel(channelHandlerContext.channel(), url, handler);
            Object msg;
            int saveReaderIndex;

            try {
                // decode object.
                do {
                    saveReaderIndex = message.readerIndex();
                    try {
                        msg = codec.decode(channel, message);
                    } catch (IOException e) {
                        buffer = ChannelBuffers.EMPTY_BUFFER;
                        throw e;
                    }
                    if (msg == Codec2.DecodeResult.NEED_MORE_INPUT) {
                        message.readerIndex(saveReaderIndex);
                        break;
                    } else {
                        if (saveReaderIndex == message.readerIndex()) {
                            buffer = ChannelBuffers.EMPTY_BUFFER;
                            throw new IOException("Decode without read data.");
                        }
                        if (msg != null) {
                            //Channels.fireMessageReceived(ctx, msg, event.getRemoteAddress());
                            channelHandlerContext.fireChannelRead(msg);
                        }
                    }
                } while (message.readable());
            } finally {
                if (message.readable()) {
                    message.discardReadBytes();
                    buffer = message;
                } else {
                    buffer = com.alibaba.dubbo.remoting.buffer.ChannelBuffers.EMPTY_BUFFER;
                }
                Netty4Channel.removeChannelIfDisconnected(channelHandlerContext.channel());
            }
        }
    }
}
