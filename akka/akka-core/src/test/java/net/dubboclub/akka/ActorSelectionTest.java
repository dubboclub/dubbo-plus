package net.dubboclub.akka;

import akka.actor.*;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import net.dubboclub.akka.core.ActorOperate;

/**
 * Created by bieber on 2015/7/9.
 */
public class ActorSelectionTest {


    public static void main(String[] args){
        ActorSystem actorSystem = ActorSystem.create("helloworld");
        ActorRef root = actorSystem.actorOf(Props.create(RootActor.class), "root");
        root.tell(new Object(), ActorRef.noSender());
        root.tell("select", ActorRef.noSender());
    }


    public static  class RootActor extends UntypedActor{

        LoggingAdapter loggingAdapter = Logging.getLogger(getContext().system(),this);

        @Override
        public void onReceive(Object o) throws Exception {
            loggingAdapter.debug("receive message {} from {}",o,getSender());
            if(o instanceof  String){
                getContext().actorSelection("hello").tell(new Identify(ActorOperate.Operate.DESTROY), getSelf());
            }else if(o instanceof ActorIdentity){

            }else if(o instanceof Object){
                getContext().actorOf(Props.create(ChildActor.class),"hello");
            }
        }
    }


    public static  class ChildActor extends UntypedActor{

        LoggingAdapter loggingAdapter = Logging.getLogger(getContext().system(),this);
        @Override
        public void onReceive(Object o) throws Exception {
            loggingAdapter.debug("receive message {} from {}",o,getSender());
        }
    }
}
