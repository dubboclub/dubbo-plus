package net.dubboclub.akka.remoting.message;

import net.dubboclub.akka.remoting.actor.AkkaFuture;

import java.io.Serializable;

/**
 * Created by bieber on 2015/7/9.
 */
public class ActorRequestWrapper  implements Serializable{
    
    private Request request;
    
    private AkkaFuture future;

    public ActorRequestWrapper(Request request, AkkaFuture future) {
        this.request = request;
        this.future = future;
    }

    public Request getRequest() {
        return request;
    }

    public AkkaFuture getFuture() {
        return future;
    }
}
