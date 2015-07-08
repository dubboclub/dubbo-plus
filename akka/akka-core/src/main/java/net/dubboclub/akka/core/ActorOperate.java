package net.dubboclub.akka.core;

/**
 * Created by bieber on 2015/7/9.
 */
public class ActorOperate {

    private String actorName;

    private Operate operation;

    public ActorOperate(String actorName, Operate operation) {
        this.actorName = actorName;
        this.operation = operation;
    }

    public String getActorName() {
        return actorName;
    }

    public Operate getOperation() {
        return operation;
    }

    public enum Operate {
        DESTROY,STATISTICS,STARTED
    }

}
