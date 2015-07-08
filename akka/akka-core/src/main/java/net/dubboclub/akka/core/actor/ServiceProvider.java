package net.dubboclub.akka.core.actor;

import akka.actor.UntypedActor;
import com.alibaba.dubbo.rpc.Invocation;
import com.alibaba.dubbo.rpc.Invoker;

import java.io.Serializable;

/**
 * Created by bieber on 2015/7/8.
 */
public class ServiceProvider extends UntypedActor {

    private Invoker<?> invoker;

    public ServiceProvider(Invoker<?> invoker){
        this.invoker = invoker;
    }

    @Override
    public void onReceive(Object o) throws Exception {
        if(o instanceof Invocation){
            getSelf().forward(o,getContext());
        }
    }


    class Worker extends UntypedActor{

        @Override
        public void onReceive(Object o) throws Exception {

        }

    }

    class RequestWrapper implements Serializable{

        private Invoker<?> invoker;

        private Invocation invocation;

        public RequestWrapper(Invoker<?> invoker, Invocation invocation) {
            this.invoker = invoker;
            this.invocation = invocation;
        }

        public Invoker<?> getInvoker() {
            return invoker;
        }

        public Invocation getInvocation() {
            return invocation;
        }
    }


}
