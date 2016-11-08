package net.dubboclub.tracing.client;

import com.alibaba.dubbo.common.URL;
import com.alibaba.dubbo.common.extension.SPI;
import net.dubboclub.tracing.api.TracingCollector;

/**
 * TracingCollectorFactory
 * Created by bieber.bibo on 16/7/18
 */
@SPI(DstConstants.DEFAULT_COLLECTOR_TYPE)
public interface TracingCollectorFactory {

    /**
     * 监控链路的数据同步器
     * @return
     */
    public TracingCollector getTracingCollector();

}
