package net.dubboclub.akka.core;

import akka.actor.Actor;
import net.dubboclub.akka.core.exception.AkkaSystemException;

/**
 * Created by bieber on 2015/7/8.
 */
public class AkkaContextHandler {

    private volatile static ActorSystemBootstrap actorSystemBootstrap;

    public static synchronized void setActorSystemBootstrap(ActorSystemBootstrap bootstrap){
        if(actorSystemBootstrap==null){
            actorSystemBootstrap = bootstrap;
        }else{
            throw new AkkaSystemException("Actor system had started,actor system must be unique in the application context");
        }
    }

    public static ActorSystemBootstrap getActorSystemBootstrap(){
        return actorSystemBootstrap;
    }

    public static void clean(){
         actorSystemBootstrap = null;
    }
}
