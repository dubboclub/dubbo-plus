package net.dubboclub.akka.remoting.actor;

import akka.actor.Actor;
import akka.actor.ActorRef;
import akka.routing.Router;
import com.alibaba.dubbo.remoting.RemotingException;
import com.alibaba.dubbo.remoting.exchange.ResponseFuture;
import net.dubboclub.akka.remoting.ActorSystemBootstrap;
import net.dubboclub.akka.remoting.AkkaSystemContext;

/**
 * Created by bieber on 2015/7/8.
 */
public class ServiceActor implements BasicActor{
    
    private String serviceKey;

    private Router router;
    
    public ServiceActor(String serviceKey,Router router){
        this.router = router;
        this.serviceKey = serviceKey;
    }
    @Override
    public void destroy() {
        AkkaSystemContext.getActorSystemBootstrap(false).unRegisterActor(serviceKey);
    }

    @Override
    public void restart() {

    }


    public Router getRouter() {
        return router;
    }

    @Override
    public ResponseFuture tell(Object message) {
        throw new UnsupportedOperationException("Service Actor unsupported tell operate");
    }

}
