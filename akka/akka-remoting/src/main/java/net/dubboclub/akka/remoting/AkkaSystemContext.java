package net.dubboclub.akka.remoting;

import com.alibaba.dubbo.common.URL;
/**
 * Created by bieber on 2015/7/8.
 */
public class AkkaSystemContext {

    private volatile static ActorSystemBootstrap clientActorSystemBootstrap;

    private volatile static ActorSystemBootstrap serverActorSystemBootstrap;
    
    public static synchronized ActorSystemBootstrap initActorSystem(URL url,boolean isClient){
        if(isClient){
            if(clientActorSystemBootstrap==null){
                clientActorSystemBootstrap = new ActorSystemBootstrap(true);
                clientActorSystemBootstrap.start(url);
            }
            return clientActorSystemBootstrap;
        }else{
            if(serverActorSystemBootstrap==null){
                serverActorSystemBootstrap = new ActorSystemBootstrap(false);
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
