package net.dubboclub.akka.remoting.message;

import java.io.Serializable;

/**
 * Created by bieber on 2015/7/9.
 */
public class ActorOperate {

    private String actorName;

    private Operate operate;
    
    private Serializable attachment;

    public ActorOperate(String actorName, Operate operate) {
        this.actorName = actorName;
        this.operate = operate;
    }

    public void attachment(Serializable attachment){
        this.attachment = attachment;
    }

    public Serializable getAttachment() {
        return attachment;
    }

    public String getActorName() {
        return actorName;
    }

    public Operate getOperate() {
        return operate;
    }

    public enum Operate {
        DESTROY,STATISTICS,STARTED,REQUEST
    }

}
