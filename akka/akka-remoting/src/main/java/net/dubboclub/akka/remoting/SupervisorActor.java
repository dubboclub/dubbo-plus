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

    private Map<Class<? extends UntypedActor>,AtomicLong> actorStatistics = new HashMap<Class<? extends UntypedActor>, AtomicLong>();

    private Map<String,Class<? extends UntypedActor>> actorMap = new HashMap<String, Class<? extends UntypedActor>>();



    @Override
    public void onReceive(Object o) throws Exception {
        if(o instanceof RegisterActorWrapper){//将某个制定的子Actor注册到当前Actor中
            RegisterActorWrapper wrapper = (RegisterActorWrapper) o;
            ActorRef ref = getContext().watch(getContext().actorOf(Props.create(wrapper.getActorClass(), wrapper.getConstructorArgs()), wrapper.getActorName()));
            AtomicLong atomicLong = actorStatistics.get(wrapper.getActorClass());
            actorMap.put(ref.path().toString(),wrapper.getActorClass());
            if(atomicLong==null){
                atomicLong = new AtomicLong(0);
                actorStatistics.put(wrapper.getActorClass(),atomicLong);
            }
            atomicLong.getAndIncrement();
        }else if(o instanceof Terminated){//某个监控的子Actor死亡信息
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
            //对当前的Actor进行相关操作
            ActorOperate.Operate operate = (ActorOperate.Operate) o;
            switch (operate){
                case STATISTICS:{//统计当前Actor的子Actor的状态
                    for(Map.Entry<Class<? extends UntypedActor>,AtomicLong> entry:actorStatistics.entrySet()){
                        loggingAdapter.debug("{} actor type current had {} actor instants",entry.getKey(),entry.getValue().get());
                    }
                    break;
                }
                case STARTED:{//启动初始化当前Actor
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
                ActorOperate operate = (ActorOperate) actorIdentity.correlationId();
                switch (operate.getOperate()){
                    case DESTROY:{
                        //想某个子Actor发送销毁事件
                        actorIdentity.getRef().tell(PoisonPill.getInstance(),getSelf());
                        break;
                    }
                    case REQUEST:{
                        //对某个actor进行请求
                        actorIdentity.getRef().tell(operate.getAttachment(),getSelf());
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
            //对子Actor的操作统一入口
            ActorOperate operate = (ActorOperate) o;
            getContext().actorSelection(operate.getActorName()).tell(new Identify(operate),getSelf());
        }else{
            unhandled(o);
        }
    }

}
