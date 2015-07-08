package net.dubboclub.protocol.akka;

import com.alibaba.dubbo.rpc.Exporter;
import com.alibaba.dubbo.rpc.Invoker;
import com.alibaba.dubbo.rpc.protocol.AbstractExporter;
import com.alibaba.dubbo.rpc.support.ProtocolUtils;
import net.dubboclub.akka.core.AkkaContextHandler;

/**
 * Created by bieber on 2015/7/8.
 */
public class AkkaExporter<T> extends AbstractExporter<T> {


    public AkkaExporter(Invoker<T> invoker) {
        super(invoker);
        AkkaContextHandler.getActorSystemBootstrap().registerService(invoker);
    }

    @Override
    public void unexport() {
        super.unexport();
        String serviceKey = ProtocolUtils.serviceKey(getInvoker().getUrl());
        AkkaContextHandler.getActorSystemBootstrap().unRegisterActor(serviceKey);
    }
}
