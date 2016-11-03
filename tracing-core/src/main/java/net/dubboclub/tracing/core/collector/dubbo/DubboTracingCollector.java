package net.dubboclub.tracing.core.collector.dubbo;

import com.alibaba.dubbo.common.extension.ExtensionLoader;
import net.dubboclub.tracing.core.SpanBean;
import net.dubboclub.tracing.core.collector.TracingCollector;
import net.dubboclub.tracing.core.config.Config;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * DubboTracingCollector
 * Created by bieber.bibo on 16/11/2
 */

public class DubboTracingCollector implements TracingCollector {

    private static TracingCollectorFactory tracingCollectorFactory ;


    private TracingCollector tracingCollector ;

    private AtomicBoolean init=new AtomicBoolean(false);

    public DubboTracingCollector() {
        tracingCollectorFactory = ExtensionLoader.getExtensionLoader(TracingCollectorFactory.class).getExtension(Config.DEFAULT_DUBBO_COLLECTOR_FACTORY);
    }

    @Override
    public void push(List<SpanBean> spanList) {
        if(init.compareAndSet(false,true)){
            tracingCollector = tracingCollectorFactory.getTracingCollector();
        }
        tracingCollector.push(spanList);
    }
}
