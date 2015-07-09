package net.dubboclub.akka.remoting.actor;

import akka.actor.Actor;
import akka.actor.ActorRef;
import com.alibaba.dubbo.remoting.RemotingException;
import com.alibaba.dubbo.remoting.exchange.ResponseFuture;
import net.dubboclub.akka.remoting.ActorSystemBootstrap;
import net.dubboclub.akka.remoting.AkkaSystemContext;

/**
 * Created by bieber on 2015/7/8.
 */
public class ServiceActor implements BasicActor{
    
    private String serviceKey;

    private ActorRef parent;
    
    public ServiceActor(String serviceKey,ActorRef parent){
        this.parent = parent;
        this.serviceKey = serviceKey;
    }
    @Override
    public void destroy() {
        AkkaSystemContext.getActorSystemBootstrap().unRegisterActor(ActorSystemBootstrap.SERVICE_SLIDE+serviceKey);
    }

    @Override
    public void restart() {

    }

    @Override
    public ActorRef getParent() {
        return parent;
    }

    @Override
    public ResponseFuture tell(Object message) {
        return null;
    }

}
