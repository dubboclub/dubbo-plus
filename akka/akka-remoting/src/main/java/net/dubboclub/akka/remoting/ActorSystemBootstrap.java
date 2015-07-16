package net.dubboclub.akka.remoting;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.routing.*;
import com.alibaba.dubbo.common.URL;
import com.alibaba.dubbo.common.utils.NetUtils;
import com.alibaba.dubbo.rpc.Invoker;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import net.dubboclub.akka.remoting.actor.AkkaFuture;
import net.dubboclub.akka.remoting.actor.ClientInvoker;
import net.dubboclub.akka.remoting.actor.ServiceProvider;
import net.dubboclub.akka.remoting.actor.dispatcher.ClientDispatcher;
import net.dubboclub.akka.remoting.actor.dispatcher.DispatchActor;
import net.dubboclub.akka.remoting.actor.dispatcher.ServiceRegistry;
import net.dubboclub.akka.remoting.message.ActorOperate;
import net.dubboclub.akka.remoting.message.RegisterActorWrapper;
import net.dubboclub.akka.remoting.message.Request;
import net.dubboclub.akka.remoting.utils.Utils;

import java.util.*;

/**
 * Created by bieber on 2015/7/8.
 */
public class ActorSystemBootstrap {

    private ActorSystem system;

    private String systemName;

    private volatile  boolean isStarted =false;

    private boolean isClient;
    
    private Router supervisorRouter ;

    public static final String SYSTEM_NAME="DUBBO_AKKA";

    private static final int DEFAULT_CLIENT_ROOT_ACTOR_SIZE=Runtime.getRuntime().availableProcessors()*10;

    public ActorSystemBootstrap(boolean isClient){
        systemName=SYSTEM_NAME+"_"+(isClient?"CONSUMER":"PROVIDER");
        this.isClient = isClient;
    }

    public Router getSupervisorRouter() {
        return supervisorRouter;
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
                system = ActorSystem.create(systemName,config);
                supervisorRouter = new Router(new SmallestMailboxRoutingLogic());
                supervisorRouter=supervisorRouter.addRoutee(new ActorRefRoutee(system.actorOf(Props.create(ServiceRegistry.class), DispatchActor.AKKA_ROOT_SUPERVISOR_ACTOR_NAME)));
            }else{
                Map<String,Object> mapConfig = new HashMap<String,Object>();
                mapConfig.put("akka.actor.provider","akka.remote.RemoteActorRefProvider");
                mapConfig.put("akka.remote.netty.tcp.hostname","127.0.0.1");
                mapConfig.put("akka.remote.netty.tcp.port", NetUtils.getAvailablePort());
                config = ConfigFactory.load(ConfigFactory.parseMap(mapConfig));
                system = ActorSystem.create(systemName,config);
                List<Routee> routees = new ArrayList<Routee>();
                for(int i=0;i<DEFAULT_CLIENT_ROOT_ACTOR_SIZE;i++){
                    routees.add(new ActorRefRoutee(system.actorOf(Props.create(ClientDispatcher.class))));
                }
                supervisorRouter = new Router(new SmallestMailboxRoutingLogic(),routees);
            }
            isStarted=true;
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
        String actorName = invoker.getUrl().getServiceKey();
        RegisterActorWrapper wrapper = new RegisterActorWrapper(ServiceProvider.class,new Object[]{invoker}, Utils.formatActorName(actorName));
        supervisorRouter.route(wrapper, ActorRef.noSender());
    }

    public void unRegisterActor(String actorName){
        ActorOperate operate = new ActorOperate(Utils.formatActorName(actorName), ActorOperate.Operate.DESTROY);
        tellSupervisor(operate);
    }

    private void tellSupervisor(ActorOperate actorOperate){
        supervisorRouter.route(actorOperate, ActorRef.noSender());
    }


    public void registerClient(Class<?> type,URL url){
        String actorName = url.getServiceKey();
        RegisterActorWrapper wrapper = new RegisterActorWrapper(ClientInvoker.class,new Object[]{type,url},Utils.formatActorName(actorName));
        supervisorRouter.route(wrapper,ActorRef.noSender());
    }

    
    
    public boolean isStarted(){
        return isStarted;
    }

    public void shutdown(){
        system.shutdown();
        isStarted=false;
    }

}
