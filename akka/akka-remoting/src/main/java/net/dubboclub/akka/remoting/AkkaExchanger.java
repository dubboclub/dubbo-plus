package net.dubboclub.akka.remoting;

import com.alibaba.dubbo.rpc.Invoker;
import com.alibaba.dubbo.rpc.support.ProtocolUtils;
import net.dubboclub.akka.remoting.actor.BasicActor;
import net.dubboclub.akka.remoting.actor.ConsumeActor;
import net.dubboclub.akka.remoting.actor.ServiceActor;

import com.alibaba.dubbo.common.URL;


/**
 * Created by bieber on 2015/7/9.
 */
public class AkkaExchanger implements ActorExchanger {
    @Override
    public BasicActor bind(Invoker<?> invoker) {
        AkkaSystemContext.initActorSystem(invoker.getUrl(),false);
        String serviceKey = ProtocolUtils.serviceKey(invoker.getUrl());
        AkkaSystemContext.getActorSystemBootstrap(false).registerService(invoker);
        return new ServiceActor(serviceKey,AkkaSystemContext.getActorSystemBootstrap(false).getSupervisorActor());
    }

    @Override
    public BasicActor connect(Class<?> type, URL url) {
        AkkaSystemContext.initActorSystem(url,true);
        String serviceKey = ProtocolUtils.serviceKey(url);
        AkkaSystemContext.getActorSystemBootstrap(true).registerClient(type,url);
        return new ConsumeActor(serviceKey,AkkaSystemContext.getActorSystemBootstrap(true).getSupervisorActor());
    }
}
