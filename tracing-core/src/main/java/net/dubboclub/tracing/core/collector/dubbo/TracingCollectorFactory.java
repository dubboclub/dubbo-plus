package net.dubboclub.tracing.core.collector.dubbo;

import com.alibaba.dubbo.common.extension.SPI;
import net.dubboclub.tracing.core.collector.TracingCollector;

/**
 * TracingCollectorFactory
 * Created by bieber.bibo on 16/7/18
 */
@SPI("default")
public interface TracingCollectorFactory {

    /**
     * 监控链路的数据同步器
     * @return
     */
    public TracingCollector getTracingCollector();

}
