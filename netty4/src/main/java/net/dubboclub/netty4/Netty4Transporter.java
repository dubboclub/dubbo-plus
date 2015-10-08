package net.dubboclub.netty4;

import com.alibaba.dubbo.common.URL;
import com.alibaba.dubbo.remoting.*;

/**
 * Created by bieber on 2015/10/8.
 */
public class Netty4Transporter implements Transporter {


    public static final String NAME = "NETTY4";
    
    @Override
    public Server bind(URL url, ChannelHandler handler) throws RemotingException {
        return new Netty4Server(url,handler);
    }

    @Override
    public Client connect(URL url, ChannelHandler handler) throws RemotingException {
        return new Netty4Client(url,handler);
    }
}
