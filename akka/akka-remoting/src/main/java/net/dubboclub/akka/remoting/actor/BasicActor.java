package net.dubboclub.akka.remoting.actor;

import akka.actor.ActorRef;
import akka.routing.Router;
import com.alibaba.dubbo.remoting.exchange.ResponseFuture;

/**
 * Created by bieber on 2015/7/9.
 */
public interface BasicActor {
    
    public void destroy();
    
    public void restart();
    
    public Router getRouter();
    
    public ResponseFuture tell(Object message);
    
}
