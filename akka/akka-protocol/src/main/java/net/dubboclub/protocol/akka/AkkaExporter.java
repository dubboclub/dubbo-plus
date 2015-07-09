package net.dubboclub.protocol.akka;

import com.alibaba.dubbo.rpc.Invoker;
import com.alibaba.dubbo.rpc.protocol.AbstractExporter;
import net.dubboclub.akka.remoting.actor.BasicActor;

/**
 * Created by bieber on 2015/7/8.
 */
public class AkkaExporter<T> extends AbstractExporter<T> {

    private BasicActor actor;

    public AkkaExporter(Invoker<T> invoker,BasicActor actor) {
        super(invoker);
        this.actor = actor;
    }

    @Override
    public void unexport() {
        super.unexport();
        this.actor.destroy();
    }
}
