package net.dubboclub.akka.remoting.actor.dispatcher;

import akka.actor.PoisonPill;
import akka.actor.Props;
import akka.actor.UntypedActor;
import akka.routing.ActorRefRoutee;
import akka.routing.Routee;
import akka.routing.Router;
import akka.routing.SmallestMailboxRoutingLogic;
import net.dubboclub.akka.remoting.message.ActorOperate;
import scala.collection.immutable.IndexedSeq;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by bieber on 2015/7/16.
 */
public class ClientDispatcher extends DispatchActor {

    private static final Map<String,Router> CLIENT_CACHE = new HashMap<String,Router>();

    private static final int DEFAULT_CLIENT_SIZE=3;

    @Override
    protected void generateChildActor(Class<? extends UntypedActor> actor, Object[] constructorArgs, String actorName) {
        Router router = CLIENT_CACHE.get(actorName);
        if(router==null){
            router=new Router(new SmallestMailboxRoutingLogic());
            CLIENT_CACHE.put(actorName,router);
            for(int i=0;i<DEFAULT_CLIENT_SIZE;i++){
                router = router.addRoutee(new ActorRefRoutee(getContext().actorOf(Props.create(actor,constructorArgs))));
            }
            CLIENT_CACHE.put(actorName,router);
        }
    }

    @Override
    protected void invokeChild(String actorName, ActorOperate operate) {
        if(CLIENT_CACHE.containsKey(actorName)){
           Router router =  CLIENT_CACHE.get(actorName);
            switch (operate.getOperate()){
                case DESTROY:{
                    //想某个子Actor发送销毁事件
                    IndexedSeq<Routee> routeeIterator = router.routees();
                    for(int i=0;i<routeeIterator.size();i++){
                        routeeIterator.apply(i).send(PoisonPill.getInstance(),getSelf());
                    }
                    break;
                }
                case REQUEST:{
                    //对某个actor进行请求
                    router.route (operate.getAttachment(), getSelf());
                    break;
                }
                default:{
                    unhandled(operate);
                    loggingAdapter.debug("unknow operation for actor {}",router);
                    break;
                }
            }
        }else{
            throw new IllegalStateException("not found client actor ref  for "+actorName);
        }

    }


}
