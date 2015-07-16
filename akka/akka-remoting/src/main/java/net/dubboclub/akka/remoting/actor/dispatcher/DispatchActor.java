package net.dubboclub.akka.remoting.actor.dispatcher;

import akka.actor.*;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import akka.routing.Router;
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
public abstract class DispatchActor extends UntypedActor {

    protected LoggingAdapter loggingAdapter = Logging.getLogger(getContext().system(),this);

    public static final String AKKA_ROOT_SUPERVISOR_ACTOR_NAME="root";

    protected int childrenSize = 0;

    protected abstract void generateChildActor(Class<? extends UntypedActor> actor,Object[] constructorArgs,String actorName);

    protected abstract void invokeChild(String actorName,ActorOperate operate);

    @Override
    public void onReceive(Object o) throws Exception {
        if(o instanceof RegisterActorWrapper){//将某个制定的子Actor注册到当前Actor中
            RegisterActorWrapper wrapper = (RegisterActorWrapper) o;
            generateChildActor(wrapper.getActorClass(),wrapper.getConstructorArgs(),wrapper.getActorName());
            childrenSize++;
        }else if(o instanceof Terminated){//某个监控的子Actor死亡信息
            Terminated terminated = (Terminated) o;
            ActorRef actorRef = terminated.getActor();
            loggingAdapter.debug("Actor {} had terminated ",actorRef);
        }else if(o instanceof ActorOperate){
            //对子Actor的操作统一入口
            ActorOperate operate = (ActorOperate) o;
            invokeChild(operate.getActorName(),operate);
        }else{
            unhandled(o);
        }
    }

}
