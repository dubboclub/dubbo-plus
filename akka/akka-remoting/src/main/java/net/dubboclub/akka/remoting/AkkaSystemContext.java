package net.dubboclub.akka.remoting;

import com.alibaba.dubbo.common.Constants;
import com.alibaba.dubbo.common.URL;
import net.dubboclub.akka.remoting.exception.AkkaSystemException;

/**
 * Created by bieber on 2015/7/8.
 */
public class AkkaSystemContext {

    private volatile static ActorSystemBootstrap actorSystemBootstrap;
    
    public static synchronized ActorSystemBootstrap initActorSystem(URL url){
        if(actorSystemBootstrap==null){
            String key = url.getParameter(Constants.APPLICATION_KEY);
            actorSystemBootstrap = new ActorSystemBootstrap(key);
            actorSystemBootstrap.start(url);
        }
        return actorSystemBootstrap;
    }
    
    public static ActorSystemBootstrap getActorSystemBootstrap(){
        return actorSystemBootstrap;
    }

    public static void clean(){
         actorSystemBootstrap = null;
    }
}
