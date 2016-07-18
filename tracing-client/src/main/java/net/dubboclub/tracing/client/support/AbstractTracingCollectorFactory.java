package net.dubboclub.tracing.client.support;

import com.alibaba.dubbo.common.Constants;
import com.alibaba.dubbo.common.URL;
import com.alibaba.dubbo.rpc.Invoker;
import com.alibaba.dubbo.rpc.Protocol;
import com.alibaba.dubbo.rpc.ProxyFactory;
import net.dubboclub.tracing.api.TracingCollector;
import net.dubboclub.tracing.client.TracingCollectorFactory;
import org.apache.commons.lang.StringUtils;

/**
 * AbstractTracingCollectorFactory
 * Created by bieber.bibo on 16/7/18
 */

public abstract class AbstractTracingCollectorFactory implements TracingCollectorFactory {



    @Override
    public TracingCollector getTracingCollector(URL url) {
        String protocolName = url.getProtocol();
        String root = url.getPath();
        url=url.setProtocol(Constants.REGISTRY_PROTOCOL);
        url=url.addParameter(Constants.REGISTRY_KEY,protocolName);
        if(StringUtils.isNotEmpty(root)){
            url=url.addParameter(Constants.GROUP_KEY,root);
        }
        url=url.setPath(TracingCollector.class.getName());
        url=url.addParameter(Constants.INTERFACE_KEY,TracingCollector.class.getName());
        url=url.addParameter(Constants.REFERENCE_FILTER_KEY,"-dst");
        return createTracingCollector(url);
    }

    /**
     *
     * @param url registry://zkip:port/
     * @return
     */
    protected abstract TracingCollector createTracingCollector(URL url);


}
