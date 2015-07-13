package net.dubboclub.akka.remoting;

import com.alibaba.dubbo.rpc.Invoker;
import com.alibaba.dubbo.rpc.support.ProtocolUtils;
import net.dubboclub.akka.remoting.actor.BasicActor;
import net.dubboclub.akka.remoting.actor.ConsumeActor;
import net.dubboclub.akka.remoting.actor.ServiceActor;

import com.alibaba.dubbo.common.URL;
import net.dubboclub.akka.remoting.utils.Utils;


/**
 * Created by bieber on 2015/7/9.
 */
public class AkkaExchanger implements ActorExchanger {
    @Override
    public BasicActor bind(Invoker<?> invoker) {
        AkkaSystemContext.initActorSystem(invoker.getUrl(),false);
        String serviceKey = invoker.getUrl().getServiceKey();
        AkkaSystemContext.getActorSystemBootstrap(false).registerService(invoker);
        return new ServiceActor(Utils.formatActorName(serviceKey),AkkaSystemContext.getActorSystemBootstrap(false).getSupervisorActor());
    }

    @Override
    public BasicActor connect(Class<?> type, URL url) {
        AkkaSystemContext.initActorSystem(url,true);
        String serviceKey = url.getServiceKey();
        AkkaSystemContext.getActorSystemBootstrap(true).registerClient(type,url);
        return new ConsumeActor(Utils.formatActorName(serviceKey),AkkaSystemContext.getActorSystemBootstrap(true).getSupervisorActor());
    }
}
