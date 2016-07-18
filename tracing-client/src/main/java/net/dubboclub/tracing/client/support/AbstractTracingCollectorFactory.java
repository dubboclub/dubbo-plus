package net.dubboclub.tracing.client.support;

import com.alibaba.dubbo.common.Constants;
import com.alibaba.dubbo.common.URL;
import com.alibaba.dubbo.registry.Registry;
import com.alibaba.dubbo.registry.support.AbstractRegistryFactory;
import com.alibaba.dubbo.rpc.Invoker;
import com.alibaba.dubbo.rpc.Protocol;
import com.alibaba.dubbo.rpc.ProxyFactory;
import net.dubboclub.tracing.api.TracingCollector;
import net.dubboclub.tracing.client.TracingCollectorFactory;
import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * AbstractTracingCollectorFactory
 * Created by bieber.bibo on 16/7/18
 */

public abstract class AbstractTracingCollectorFactory implements TracingCollectorFactory {



    @Override
    public TracingCollector getTracingCollector() {
        Collection<Registry> registries =  AbstractRegistryFactory.getRegistries();
        List<URL> urls = new ArrayList<URL>();
        for(Registry registry:registries){
            URL url = registry.getUrl();
            String protocolName = url.getProtocol();
            url=url.setProtocol(Constants.REGISTRY_PROTOCOL);
            url=url.addParameter(Constants.REGISTRY_KEY,protocolName);
            url=url.setPath(TracingCollector.class.getName());
            url=url.addParameter(Constants.INTERFACE_KEY,TracingCollector.class.getName());
            url=url.addParameter(Constants.REFERENCE_FILTER_KEY,"-dst");
            urls.add(url);
        }
        return createTracingCollector(urls);
    }

    /**
     *
     * @param urls registry://zkip:port/
     * @return
     */
    protected abstract TracingCollector createTracingCollector(List<URL> urls);


}
