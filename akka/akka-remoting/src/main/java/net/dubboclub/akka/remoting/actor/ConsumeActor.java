package net.dubboclub.akka.remoting.actor;

import akka.routing.Router;
import com.alibaba.dubbo.remoting.exchange.ResponseFuture;
import com.alibaba.dubbo.rpc.Invocation;
import net.dubboclub.akka.remoting.AkkaSystemContext;
import net.dubboclub.akka.remoting.message.Request;

/**
 * Created by bieber on 2015/7/9.
 */
public class ConsumeActor implements BasicActor {

    private String serviceKey;

    private Router router;


    public ConsumeActor(String serviceKey, Router router) {
        this.serviceKey = serviceKey;
        this.router = router;
    }

    @Override
    public void destroy() {
        AkkaSystemContext.getActorSystemBootstrap(true).unRegisterActor(serviceKey);
    }

    @Override
    public void restart() {

    }

    @Override
    public Router getRouter() {
        return router;
    }


    @Override
    public ResponseFuture tell(Object message) {
        if(message instanceof Invocation){
            Request request = new Request(message, serviceKey);
            return AkkaSystemContext.getActorSystemBootstrap(true).doRequest(request);
        }
       throw new IllegalArgumentException("unsupported argument type "+message.getClass());
    }
}
