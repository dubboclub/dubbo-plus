package net.dubboclub.akka.remoting;

import com.alibaba.dubbo.common.Constants;
import com.alibaba.dubbo.common.extension.Adaptive;
import com.alibaba.dubbo.common.extension.SPI;
import com.alibaba.dubbo.rpc.Invoker;
import net.dubboclub.akka.remoting.actor.BasicActor;

import java.net.URL;

/**
 * Created by bieber on 2015/7/9.
 */
@SPI("akka")
public interface ActorExchanger {
    
    @Adaptive({Constants.TRANSPORTER_KEY})
    public BasicActor bind(Invoker<?> invoker);

    @Adaptive({Constants.TRANSPORTER_KEY})
    public BasicActor connect(Class<?> type,URL url);
}
