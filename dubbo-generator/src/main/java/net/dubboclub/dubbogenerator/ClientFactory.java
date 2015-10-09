package net.dubboclub.dubbogenerator;

import org.springframework.beans.factory.FactoryBean;

/**
 * Created by bieber on 2015/8/28.
 */
public class ClientFactory<T> implements FactoryBean {
    
    private Class<T> type;
    

    @Override
    public Object getObject() throws Exception {
        return DubboClientWrapper.getWrapper(type);
    }

    @Override
    public Class<?> getObjectType() {
        return type;
    }

    @Override
    public boolean isSingleton() {
        return true;
    }

    public void setType(Class<T> type) {
        this.type = type;
    }
}
