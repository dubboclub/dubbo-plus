package net.dubboclub.akka.remoting.actor.dispatcher;

import akka.actor.ActorRef;
import akka.actor.PoisonPill;
import akka.actor.Props;
import akka.actor.UntypedActor;
import akka.routing.Routee;
import net.dubboclub.akka.remoting.message.ActorOperate;
import scala.collection.immutable.IndexedSeq;

/**
 * Created by bieber on 2015/7/16.
 */
public class ServiceRegistry extends DispatchActor {
    @Override
    protected void generateChildActor(Class<? extends UntypedActor> actor, Object[] constructorArgs, String actorName) {
        getContext().actorOf(Props.create(actor,constructorArgs),actorName);
    }

    @Override
    protected void invokeChild(String actorName, ActorOperate operate) {
        ActorRef ref = getContext().getChild(actorName);
        if(ref!=null){
            switch (operate.getOperate()){
                case DESTROY:{
                    //想某个子Actor发送销毁事件
                    ref.tell(PoisonPill.getInstance(),getSelf());
                    break;
                }
                case REQUEST:{
                    //对某个actor进行请求
                    ref.tell(operate.getAttachment(), getSelf());
                    break;
                }
                default:{
                    unhandled(operate);
                    loggingAdapter.debug("unknow operation for actor {}",ref);
                    break;
                }
            }
        }else{
            throw new IllegalStateException("not found client actor ref  for "+actorName);
        }

    }
}
