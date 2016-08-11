package net.dubboclub.tracing.client.support.rocketmq;

import net.dubboclub.tracing.api.TracingCollector;
import net.dubboclub.tracing.client.TracingCollectorFactory;

/**
 * RocketMqTracingCollectorFactory
 * Created by bieber.bibo on 16/8/11
 */

public class RocketMqTracingCollectorFactory implements TracingCollectorFactory {

    @Override
    public TracingCollector getTracingCollector() {
        return new RocketMqTracingCollector();
    }

}
