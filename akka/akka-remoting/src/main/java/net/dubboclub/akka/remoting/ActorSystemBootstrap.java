package net.dubboclub.akka.remoting;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import com.alibaba.dubbo.common.URL;
import com.alibaba.dubbo.rpc.Invoker;
import com.alibaba.dubbo.rpc.support.ProtocolUtils;
import net.dubboclub.akka.remoting.actor.AkkaFuture;
import net.dubboclub.akka.remoting.actor.ServiceProvider;
import net.dubboclub.akka.remoting.message.ActorOperate;
import net.dubboclub.akka.remoting.message.ActorRequestWrapper;
import net.dubboclub.akka.remoting.message.RegisterActorWrapper;
import net.dubboclub.akka.remoting.message.Request;

/**
 * Created by bieber on 2015/7/8.
 */
public class ActorSystemBootstrap {

    private ActorSystem system;

    private String systemName;

    private ActorRef supervisorActor;

    private volatile  boolean isStarted =false;
    
    public  static final String SERVICE_SLIDE="service";

    public static final String CONSUME_SLIDE="consume";

    public ActorSystemBootstrap(String name){
        systemName=name;
    }

    public synchronized void start(URL url){
        if(!isStarted){
            system = ActorSystem.create(systemName);
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
        ActorRequestWrapper requestWrapper = new ActorRequestWrapper(request,future);

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
        String serviceKey = SERVICE_SLIDE+ProtocolUtils.serviceKey(invoker.getUrl());
        RegisterActorWrapper wrapper = new RegisterActorWrapper(ServiceProvider.class,new Object[]{invoker},serviceKey);
        supervisorActor.tell(wrapper, ActorRef.noSender());
    }

    public void unRegisterActor(String actorName){
        ActorOperate operate = new ActorOperate(actorName, ActorOperate.Operate.DESTROY);
        supervisorActor.tell(operate, ActorRef.noSender());
    }

    public ActorRef getSupervisorActor(){
        return supervisorActor;
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
