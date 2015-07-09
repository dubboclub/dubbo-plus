package net.dubboclub.akka.remoting;

import akka.actor.*;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import net.dubboclub.akka.remoting.message.ActorOperate;
import net.dubboclub.akka.remoting.message.RegisterActorWrapper;
import scala.concurrent.duration.Duration;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;


/**
 * Created by bieber on 2015/7/8.
 * 该类是actor的监控主类，所有的service或者client的actor都是通过它来初始化，均有它来进行监控
 */
public class SupervisorActor extends UntypedActor {

    private LoggingAdapter loggingAdapter = Logging.getLogger(getContext().system(),this);

    public static final String AKKA_ROOT_SUPERVISOR_ACTOR_NAME="root";

    private AtomicLong childrenCount = new AtomicLong(0);

    private Map<Class<? extends UntypedActor>,AtomicLong> actorStatistics = new HashMap<Class<? extends UntypedActor>, AtomicLong>();

    private Map<String,Class<? extends UntypedActor>> actorMap = new HashMap<String, Class<? extends UntypedActor>>();



    @Override
    public void onReceive(Object o) throws Exception {
        if(o instanceof RegisterActorWrapper){
            RegisterActorWrapper wrapper = (RegisterActorWrapper) o;
            ActorRef ref = getContext().watch(getContext().actorOf(Props.create(wrapper.getActorClass(), wrapper.getConstructorArgs()), wrapper.getActorName()));
            AtomicLong atomicLong = actorStatistics.get(wrapper.getActorClass());
            actorMap.put(ref.path().toString(),wrapper.getActorClass());
            if(atomicLong==null){
                atomicLong = new AtomicLong(0);
                actorStatistics.put(wrapper.getActorClass(),atomicLong);
            }
            atomicLong.getAndIncrement();
        }else if(o instanceof Terminated){
            Terminated terminated = (Terminated) o;
            ActorRef actorRef = terminated.getActor();
            Class<? extends  UntypedActor> actorClass = actorMap.get(actorRef.path().toString());
            if(actorClass!=null){
                AtomicLong atomicLong = actorStatistics.get(actorClass);
                if(atomicLong!=null){
                    atomicLong.getAndDecrement();
                }
            }
        }else if (o instanceof ActorOperate.Operate){
            ActorOperate.Operate operate = (ActorOperate.Operate) o;
            switch (operate){
                case STATISTICS:{
                    for(Map.Entry<Class<? extends UntypedActor>,AtomicLong> entry:actorStatistics.entrySet()){
                        loggingAdapter.debug("{} actor type current had {} actor instants",entry.getKey(),entry.getValue().get());
                    }
                    break;
                }
                case STARTED:{
                    getContext().system().scheduler().schedule(Duration.create(1, TimeUnit.SECONDS), Duration.create(1, TimeUnit.MINUTES),getSelf(), ActorOperate.Operate.STATISTICS,getContext().dispatcher(),null);
                    break;
                }
                default:{
                    unhandled(o);
                    break;
                }
            }
        }else if(o instanceof ActorIdentity){
            ActorIdentity actorIdentity = (ActorIdentity) o;
            if(actorIdentity.getRef()==null){
                loggingAdapter.debug("not found actor for  {} operate",actorIdentity.correlationId());
            }else{
                ActorOperate.Operate operate = (ActorOperate.Operate) actorIdentity.correlationId();
                switch (operate){
                    case DESTROY:{
                        actorIdentity.getRef().tell(PoisonPill.getInstance(),getSelf());
                        break;
                    }
                    default:{
                        unhandled(o);
                        loggingAdapter.debug("unknow operation for actor {}",actorIdentity.getRef());
                        break;
                    }
                }
            }
        }else if(o instanceof ActorOperate){
            ActorOperate operate = (ActorOperate) o;
            getContext().actorSelection("./"+operate.getActorName()).tell(new Identify(operate.getOperation()),getSelf());
        }else{
            unhandled(o);
        }
    }

}
