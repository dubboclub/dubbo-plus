package net.dubboclub.akka.remoting.message;

import akka.actor.UntypedActor;

import java.io.Serializable;
import java.util.Arrays;

/**
 * Created by bieber on 2015/7/9.
 */
public class RegisterActorWrapper  implements Serializable{


    private Class<? extends UntypedActor> actorClass;

    private Object[] constructorArgs;

    private String actorName;

    public RegisterActorWrapper(Class<? extends UntypedActor> actorClass, Object[] constructorArgs,String actorName) {
        this.actorClass = actorClass;
        this.constructorArgs = constructorArgs;
        this.actorName = actorName;
    }

    public Class<? extends UntypedActor> getActorClass() {
        return actorClass;
    }

    public Object[] getConstructorArgs() {
        return constructorArgs;
    }

    public String getActorName() {
        return actorName;
    }

    @Override
    public String toString() {
        return "RegisterActorWrapper{" +
                "actorClass=" + actorClass +
                ", constructorArgs=" + Arrays.toString(constructorArgs) +
                ", actorName='" + actorName + '\'' +
                '}';
    }
}
