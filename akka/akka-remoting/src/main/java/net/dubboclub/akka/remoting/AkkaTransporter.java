package net.dubboclub.akka.remoting;

import com.alibaba.dubbo.common.URL;
import com.alibaba.dubbo.remoting.*;

/**
 * Created by bieber on 2015/7/8.
 */
public class AkkaTransporter implements Transporter {
    @Override
    public Server bind(URL url, ChannelHandler handler) throws RemotingException {
        return new AkkaServer(url,handler);
    }

    @Override
    public Client connect(URL url, ChannelHandler handler) throws RemotingException {
        return new AkkaClient(url,handler);
    }
}
