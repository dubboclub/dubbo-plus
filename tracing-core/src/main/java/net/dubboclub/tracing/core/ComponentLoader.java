package net.dubboclub.tracing.core;

import net.dubboclub.tracing.core.exception.TracingException;

import java.io.IOException;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

/**
 * ComponentLoader
 * Created by bieber.bibo on 16/11/3
 */

public class ComponentLoader {

    private static ConcurrentHashMap<String,Properties> componentConfigure = new ConcurrentHashMap<String, Properties>();

    private static ConcurrentHashMap<String,Map<String,Object>> componentCache = new ConcurrentHashMap<String, Map<String, Object>>();

    public static <T extends Object> T getComponent(String componentKey,Class<T> componentType){
        String componentName = componentType.getName();
        if(componentCache.containsKey(componentName)){
            Map<String,Object> components = componentCache.get(componentName);
            if(components.containsKey(componentKey)){
                return (T) components.get(componentKey);
            }
        }
        if(componentConfigure.containsKey(componentType.getName())){
            Properties properties = componentConfigure.get(componentType.getName());
            Object component = createComponent(properties,componentKey,componentType);
            Map<String,Object> cache;
            if(componentCache.containsKey(componentName)){
                cache = componentCache.get(componentName);
            }else{
                cache = new ConcurrentHashMap<String, Object>();
                Map<String,Object> old  = componentCache.putIfAbsent(componentName,cache);
                if(old!=null){
                    cache = old;
                }
            }
            cache.put(componentName,component);
            return (T) component;
        }else{
            Properties properties = new Properties();
            try {
                properties.load(ComponentLoader.class.getResourceAsStream("/META-INF/dubbo/"+componentName));
                componentConfigure.putIfAbsent(componentName,properties);
            } catch (IOException e) {
                throw new TracingException("fail to load component ["+componentName+"] configure.",e);
            }
            Object component = createComponent(properties,componentKey,componentType);
            Map<String,Object> cache;
            if(componentCache.containsKey(componentName)){
                 cache = componentCache.get(componentName);
            }else{
                 cache = new ConcurrentHashMap<String, Object>();
                 Map<String,Object> old  = componentCache.putIfAbsent(componentName,cache);
                if(old!=null){
                    cache = old;
                }
            }
            cache.put(componentName,component);
            return (T) component;
        }
    }

    private static Object createComponent(Properties properties,String componentKey,Class componentType){
        Object componentImplClass = properties.get(componentKey);
        if(componentImplClass==null){
            throw new TracingException("not found ["+componentKey+"] component.");
        }
        try {
            Class componentImpl = ComponentLoader.class.getClassLoader().loadClass(componentImplClass.toString());
            return componentImpl.newInstance();
        } catch (Exception e) {
            throw new TracingException("fail to create ["+componentKey+"] component,type ["+componentType.getName()+"].",e);
        }
    }
}
