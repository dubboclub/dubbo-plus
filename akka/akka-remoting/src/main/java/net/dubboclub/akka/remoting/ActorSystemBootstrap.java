package net.dubboclub.akka.remoting;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import com.alibaba.dubbo.common.URL;
import com.alibaba.dubbo.rpc.Invoker;
import com.alibaba.dubbo.rpc.support.ProtocolUtils;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import net.dubboclub.akka.remoting.actor.AkkaFuture;
import net.dubboclub.akka.remoting.actor.ConsumeServiceLookup;
import net.dubboclub.akka.remoting.actor.ServiceProvider;
import net.dubboclub.akka.remoting.message.ActorOperate;
import net.dubboclub.akka.remoting.message.RegisterActorWrapper;
import net.dubboclub.akka.remoting.message.Request;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Created by bieber on 2015/7/8.
 */
public class ActorSystemBootstrap {

    private ActorSystem system;

    private String systemName;

    private ActorRef supervisorActor;

    private volatile  boolean isStarted =false;

    private boolean isClient;
    

    public ActorSystemBootstrap(String name,boolean isClient){
        systemName=name;
        this.isClient = isClient;
    }

    public synchronized void start(URL url){
        if(!isStarted){
            Config config =null;
            if(!this.isClient){
                Map<String,Object> mapConfig = new HashMap<String,Object>();
                mapConfig.put("akka.actor.provider","akka.remote.RemoteActorRefProvider");
                mapConfig.put("akka.remote.netty.tcp.hostname",url.getHost());
                mapConfig.put("akka.remote.netty.tcp.port",url.getPort());
                config = ConfigFactory.load(ConfigFactory.parseMap(mapConfig));
            }else{
                config = ConfigFactory.load();
            }
            system = ActorSystem.create(systemName,config);
            isStarted=true;
            supervisorActor = system.actorOf(Props.create(SupervisorActor.class),SupervisorActor.AKKA_ROOT_SUPERVISOR_ACTOR_NAME);
            Runtime.getRuntime().addShutdownHook(new Thread(){
                @Override
                public void run() {
                    ActorSystemBootstrap.this.destroy();
                }
            });
        }
    }
    
    public AkkaFuture doRequest(Request request){
        AkkaFuture future = new AkkaFuture(request.getRequestId());
        ActorOperate actorOperate = new ActorOperate(request.getActorName(), ActorOperate.Operate.REQUEST);
        actorOperate.attachment(request);
        tellSupervisor(actorOperate);
        return future;
    }
    
    public  void destroy(){
        if(isStarted){
            system.stop(ActorRef.noSender());
        }else{
            throw new IllegalStateException("ActorSystemBootstrap has already shutdown!");
        }
    }

    public void registerService(Invoker<?> invoker){
        String actorName = ProtocolUtils.serviceKey(invoker.getUrl());
        RegisterActorWrapper wrapper = new RegisterActorWrapper(ServiceProvider.class,new Object[]{invoker},actorName);
        supervisorActor.tell(wrapper, ActorRef.noSender());
    }

    public void unRegisterActor(String actorName){
        ActorOperate operate = new ActorOperate(actorName, ActorOperate.Operate.DESTROY);
        tellSupervisor(operate);
    }

    private void tellSupervisor(ActorOperate actorOperate){
        supervisorActor.tell(actorOperate, ActorRef.noSender());
    }

    public ActorRef getSupervisorActor(){
        return supervisorActor;
    }
    
    public void registerClient(Class<?> type,URL url){
        String actorName = ProtocolUtils.serviceKey(url);
        RegisterActorWrapper wrapper = new RegisterActorWrapper(ConsumeServiceLookup.class,new Object[]{type,url},actorName);
        supervisorActor.tell(wrapper,ActorRef.noSender());
    }

    
    
    public boolean isStarted(){
        return isStarted;
    }

    public void shutdown(){
        system.shutdown();
        isStarted=false;
    }

}
