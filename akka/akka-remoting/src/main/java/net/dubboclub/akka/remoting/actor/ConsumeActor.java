package net.dubboclub.akka.remoting.actor;

import akka.actor.ActorRef;
import com.alibaba.dubbo.remoting.exchange.ResponseFuture;
import com.alibaba.dubbo.rpc.Invocation;
import net.dubboclub.akka.remoting.ActorSystemBootstrap;
import net.dubboclub.akka.remoting.AkkaSystemContext;
import net.dubboclub.akka.remoting.message.Request;

/**
 * Created by bieber on 2015/7/9.
 */
public class ConsumeActor implements BasicActor {

    private String serviceKey;

    private ActorRef parent;

    public ConsumeActor(String serviceKey, ActorRef parent) {
        this.serviceKey = serviceKey;
        this.parent = parent;
    }

    @Override
    public void destroy() {
        AkkaSystemContext.getActorSystemBootstrap(true).unRegisterActor(serviceKey);
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
        if(message instanceof Invocation){
            Request request = new Request(message, serviceKey);
            return AkkaSystemContext.getActorSystemBootstrap(true).doRequest(request);
        }
        return null;
    }
}
