package net.dubboclub.akka.remoting;

import com.alibaba.dubbo.common.Constants;
import com.alibaba.dubbo.common.URL;
import net.dubboclub.akka.remoting.exception.AkkaSystemException;

/**
 * Created by bieber on 2015/7/8.
 */
public class AkkaSystemContext {

    public  static final String SERVICE_SLIDE="service";

    public static final String CONSUME_SLIDE="consume";

    private volatile static ActorSystemBootstrap clientActorSystemBootstrap;

    private volatile static ActorSystemBootstrap serverActorSystemBootstrap;
    
    public static synchronized ActorSystemBootstrap initActorSystem(URL url,boolean isClient){
        if(isClient){
            if(clientActorSystemBootstrap==null){
                String key = url.getParameter(Constants.APPLICATION_KEY);
                clientActorSystemBootstrap = new ActorSystemBootstrap(CONSUME_SLIDE+"-"+key);
                clientActorSystemBootstrap.start(url);
            }
            return clientActorSystemBootstrap;
        }else{
            if(serverActorSystemBootstrap==null){
                String key = url.getParameter(Constants.APPLICATION_KEY);
                serverActorSystemBootstrap = new ActorSystemBootstrap(SERVICE_SLIDE+"-"+key);
                serverActorSystemBootstrap.start(url);
            }
            return serverActorSystemBootstrap;
        }
    }
    
    public static ActorSystemBootstrap getActorSystemBootstrap(boolean isClient){
        if(isClient){
            return clientActorSystemBootstrap;
        }else{
            return serverActorSystemBootstrap;
        }
    }

    public static void clean(boolean isClient){
        if(isClient){
             clientActorSystemBootstrap = null;
        }else{
             serverActorSystemBootstrap = null;
        }
    }
}
