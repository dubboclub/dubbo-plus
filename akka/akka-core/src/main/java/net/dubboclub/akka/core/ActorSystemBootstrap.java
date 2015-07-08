package net.dubboclub.akka.core;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import com.alibaba.dubbo.common.URL;
import com.alibaba.dubbo.rpc.Invoker;
import com.alibaba.dubbo.rpc.support.ProtocolUtils;
import net.dubboclub.akka.core.actor.ServiceProvider;

/**
 * Created by bieber on 2015/7/8.
 */
public class ActorSystemBootstrap {

    private ActorSystem system;

    private String systemName;

    private ActorRef supervisorActor;

    private volatile  boolean isStarted =false;

    public ActorSystemBootstrap(String name){
        systemName=name;
    }

    public synchronized void start(URL url){
        if(!isStarted){
            system = ActorSystem.create(systemName);
            isStarted=true;
            supervisorActor = system.actorOf(Props.create(SupervisorActor.class),SupervisorActor.AKKA_ROOT_SUPERVISOR_ACTOR_NAME);

        }
    }

    public void registerService(Invoker<?> invoker){
        String serviceKey = ProtocolUtils.serviceKey(invoker.getUrl());
        RegisterActorWrapper wrapper = new RegisterActorWrapper(ServiceProvider.class,new Object[]{invoker},serviceKey);
        supervisorActor.tell(wrapper, ActorRef.noSender());
    }

    public void unRegisterActor(String actorName){
        ActorOperate operate = new ActorOperate(actorName, ActorOperate.Operate.DESTROY);
        supervisorActor.tell(operate, ActorRef.noSender());
    }

    public void registerClient(){

    }





    public boolean isStarted(){
        return isStarted;
    }

    public void shutdown(){
        system.shutdown();
        isStarted=false;
    }

}
