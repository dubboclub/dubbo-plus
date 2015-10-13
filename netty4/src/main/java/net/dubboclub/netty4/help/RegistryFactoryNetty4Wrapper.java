package net.dubboclub.netty4.help;

import com.alibaba.dubbo.common.Constants;
import com.alibaba.dubbo.common.URL;
import com.alibaba.dubbo.registry.NotifyListener;
import com.alibaba.dubbo.registry.Registry;
import com.alibaba.dubbo.registry.RegistryFactory;

import java.util.List;


/**
 * Created by bieber on 2015/10/13.
 */
public class RegistryFactoryNetty4Wrapper implements RegistryFactory {

    private RegistryFactory registryFactory;

    public RegistryFactoryNetty4Wrapper(RegistryFactory registryFactory) {
        this.registryFactory = registryFactory;
    }

    @Override
    public Registry getRegistry(URL url) {
        return new RegistryWrapper(registryFactory.getRegistry(url));
    }
    
    class RegistryWrapper implements Registry {
        private Registry originRegistry;


        private URL removeTransporterParameter(URL url){
            url = url.removeParameter(Constants.SERVER_KEY);
            url = url.removeParameter(Constants.TRANSPORTER_KEY);
            url = url.removeParameter(Constants.DEFAULT_KEY_PREFIX+ Constants.TRANSPORTER_KEY);
            url = url.removeParameter(Constants.DEFAULT_KEY_PREFIX+ Constants.SERVER_KEY);
            return url;
        }
        
        public RegistryWrapper(Registry originRegistry) {
            this.originRegistry = originRegistry;
        }

        @Override
        public URL getUrl() {
            return originRegistry.getUrl();
        }

        @Override
        public boolean isAvailable() {
            return originRegistry.isAvailable();
        }

        @Override
        public void destroy() {
            originRegistry.destroy();
        }

        @Override
        public void register(URL url) {
            originRegistry.register(removeTransporterParameter(url));
        }

        @Override
        public void unregister(URL url) {
            originRegistry.unregister(removeTransporterParameter(url));
        }

        @Override
        public void subscribe(URL url, NotifyListener listener) {
            originRegistry.subscribe(removeTransporterParameter(url),listener);
        }

        @Override
        public void unsubscribe(URL url, NotifyListener listener) {
            originRegistry.unsubscribe(removeTransporterParameter(url),listener);
        }

        @Override
        public List<URL> lookup(URL url) {
            return originRegistry.lookup(removeTransporterParameter(url));
        }
    }
}
