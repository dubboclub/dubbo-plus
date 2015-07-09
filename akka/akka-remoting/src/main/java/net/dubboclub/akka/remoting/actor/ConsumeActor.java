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
    @Override
    public void destroy() {
        
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
            Request request = new Request(message, ActorSystemBootstrap.CONSUME_SLIDE+serviceKey);
            return AkkaSystemContext.getActorSystemBootstrap().doRequest(request);
        }
        return null;
    }
}
