package net.dubboclub.akka.remoting;

import com.alibaba.dubbo.rpc.Invoker;
import com.alibaba.dubbo.rpc.support.ProtocolUtils;
import net.dubboclub.akka.remoting.actor.BasicActor;
import net.dubboclub.akka.remoting.actor.ServiceActor;

import java.net.URL;

/**
 * Created by bieber on 2015/7/9.
 */
public class AkkaExchanger implements ActorExchanger {
    @Override
    public BasicActor bind(Invoker<?> invoker) {
        AkkaSystemContext.initActorSystem(invoker.getUrl());
        String serviceKey = ProtocolUtils.serviceKey(invoker.getUrl());
        AkkaSystemContext.getActorSystemBootstrap().registerService(invoker);
        return new ServiceActor(serviceKey,AkkaSystemContext.getActorSystemBootstrap().getSupervisorActor());
    }

    @Override
    public BasicActor connect(Class<?> type, URL url) {
        return null;
    }
}
