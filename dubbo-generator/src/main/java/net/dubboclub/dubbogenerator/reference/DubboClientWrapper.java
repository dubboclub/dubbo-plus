package net.dubboclub.dubbogenerator.reference;


import com.alibaba.dubbo.common.utils.ConfigUtils;
import com.alibaba.dubbo.common.utils.StringUtils;
import com.alibaba.dubbo.config.ReferenceConfig;
import net.dubboclub.dubbogenerator.InvokeTargetException;
import net.dubboclub.dubbogenerator.JavassistClassGenerator;
import net.dubboclub.dubbogenerator.handler.DefaultInvokeHandler;
import net.dubboclub.dubbogenerator.handler.InvokeHandler;

import java.lang.reflect.Method;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Created by bieber on 2015/7/31.
 * 动态加载封装Dubbo客户端调用
 */
public class DubboClientWrapper {
    
    //缓存已经生成的包装对象
    private static final ConcurrentHashMap<Class<?>,Object> WRAPPER_CACHE = new ConcurrentHashMap<Class<?>, Object>();
    
    private static final ConcurrentHashMap<Class<?>,Object> HANDLER_CACHE = new ConcurrentHashMap<Class<?>, Object>();
    
    private static final String DEFAULT_WRAPPER_HANDLER_KEY="dubbo.wrapper.default.handler";
    
    private static final String INVOKE_HANDLER_KEY_SUFFIX=".handler";
    
    private static final String DUBBO_WRAPPER_KEY_PREFIX="dubbo.wrapper.";
    
    //包装计数器
    private static final AtomicLong WRAPPER_COUNTER = new AtomicLong(0);
    
    
    private static Class<? extends InvokeHandler> DEFAULT_HANDLER = DefaultInvokeHandler.class;
    
    private static volatile boolean isShutdown = false;
    
    private static volatile boolean hadSetDefaultHandler = false;
    
    //动态类的标记接口
    public interface DCW{

    }//mark dynamic facade wrapper
    
    static {
        Runtime.getRuntime().addShutdownHook(new Thread(){
            @Override
            public void run() {
                isShutdown=true;
                WRAPPER_CACHE.clear();
            }
        });
        String handlerClassName = ConfigUtils.getProperty(DEFAULT_WRAPPER_HANDLER_KEY);
        if(!StringUtils.isEmpty(handlerClassName)){
            DEFAULT_HANDLER = generateHandler(handlerClassName);
        }
    }


    public static<T extends Object> T getWrapper(Class<T> clientType,String id){
        return getWrapper(clientType,id, null);
    }

    public static<T extends Object> T getWrapper(Class<T> clientType,Class<? extends InvokeHandler> handler){
        return getWrapper(clientType,null,handler);
    }
    /**
     * 对外提供的接口，获取指定类型的dubbo客户端引用
     * 如果之前创建过，则直接从缓存中获取，不必再次创建
     * @param clientType
     * @param <T>
     * @return
     */
    public static<T extends Object> T getWrapper(Class<T> clientType){
        return getWrapper(clientType,generateClientId(clientType));
    }
    
    public static<T extends Object> T getWrapper(Class<T> clientType,String id,Class<? extends InvokeHandler> handler){
        if(isShutdown){
            throw new IllegalStateException("JVM had shutdown,can not generate wrapper!");
        }
        T clientInstance = null;
        if(StringUtils.isEmpty(id)){
            id = generateClientId(clientType);
        }
        if(handler==null){
            handler = getInvokeHandler(clientType,id);
        }
        if(WRAPPER_CACHE.containsKey(clientType)){
            clientInstance =  (T) WRAPPER_CACHE.get(clientType);
        }else{
            clientInstance = (T) makeClientWrapper(clientType,id,handler);
            Object oldInstance = WRAPPER_CACHE.putIfAbsent(clientType,clientInstance);
            if(oldInstance!=null){
                clientInstance= (T) oldInstance;
            }
        }
        return clientInstance;
    }


    
    public static synchronized void setDefaultHandler(Class<? extends InvokeHandler> handler){
        if(hadSetDefaultHandler){
            throw new IllegalStateException("had already set default InvokeHandler ["+DEFAULT_HANDLER.getName()+"].");
        }
        hadSetDefaultHandler=true;
        DEFAULT_HANDLER=handler;
    }

    /**
     * 判断当前类是不是包装类
     * @param type
     * @return
     */
    public static boolean isWrapped(Class<?> type){
        return DCW.class.isAssignableFrom(type);
    }
    
    private static String generateClientId(Class<?> clientType){
        return "["+clientType.getName()+"]";
    }

    private static Class<? extends InvokeHandler> getInvokeHandler(Class<?> clientType,String id){
        //dubbo.wrapper.[clientfacadefullname].handler
        String handlerName = ConfigUtils.getProperty(DUBBO_WRAPPER_KEY_PREFIX + id + INVOKE_HANDLER_KEY_SUFFIX);
        if(StringUtils.isEmpty(handlerName)){
            return DEFAULT_HANDLER;
        }
        return generateHandler(handlerName);
        
    }

    private static Class<? extends InvokeHandler> generateHandler(String handlerClassName){
        try {
            Class<?> handlerClass = Class.forName(handlerClassName);
            if(!InvokeHandler.class.isAssignableFrom(handlerClass)){
                throw new IllegalArgumentException("Class ["+handlerClassName+"] must implements InvokerHandler or extends sub class,please check property [default.dubbo.wrapper.handler]");
            }
            return (Class<? extends InvokeHandler>) handlerClass;
        } catch (ClassNotFoundException e) {
            throw new IllegalArgumentException("Class ["+handlerClassName+"] not found ,please check property [default.dubbo.wrapper.handler]",e);
        }
    }
    /**
     * 构造一个新的指定类型的Dubbo客户端引用
     * @param clientType 该参数只能是接口类型，不支持其他类型
     * @return
     */
    private static Object makeClientWrapper(Class<?> clientType,String clientId,Class<? extends InvokeHandler> handlerClass){
        String clientName = clientType.getSimpleName();
        long id = WRAPPER_COUNTER.getAndIncrement();
        if(clientType==DCW.class){
            throw new IllegalArgumentException("not support generate wrapper for interface "+clientName);
        }
        if(!clientType.isInterface()){
            throw new IllegalArgumentException("only support interface to generate wrapper,but type "+clientName+" is not interface");
        }
        JavassistClassGenerator generator = JavassistClassGenerator.newInstance(clientType.getClassLoader());
        generator.addInterface(clientType.getName());
        generator.addInterface(DCW.class.getName());
        StringBuilder className = new StringBuilder();
        className.append(clientType.getSimpleName()).append("$DCW").append(id);
        generator.setClassName(className.toString());
        Method[] methods = clientType.getDeclaredMethods();
        StringBuilder methodCode = new StringBuilder();
        for(Method method:methods){
            methodCode.append("public ");
            Class<?> returnType = method.getReturnType();
            Class<?>[] argsTypes = method.getParameterTypes();
            if(returnType== Void.TYPE){
                methodCode.append("void ");
            }else{
                methodCode.append(returnType.getName()).append(" ");
            }
            methodCode.append(method.getName()).append("(");
            //append arguments
            if(argsTypes.length!=0){
                for(int i=0;i<argsTypes.length;i++){
                    methodCode.append(argsTypes[i].getName()).append(" arg").append(i).append(",");
                }
                methodCode.setLength(methodCode.length()-1);
            }
            Class<?>[] exceptionTypes = method.getExceptionTypes();
            methodCode.append(")");
            if(exceptionTypes.length!=0){
                methodCode.append("throws ");
                for(Class<?> exceptionType:exceptionTypes){
                    methodCode.append(exceptionType.getName()).append(",");
                }
                methodCode.setLength(methodCode.length()-1);
            }
            methodCode.append("{");
            methodCode.append("try{");
            //append method body
            //handle invoke before
            if(argsTypes.length==0){
                methodCode.append("invokeHandler.beforeInvoke(clientType,\"" + method.getName() + "\",null);");
            }else{
                methodCode.append("invokeHandler.beforeInvoke(clientType,\"" + method.getName() + "\",$args);");
            }
            //handle invoke complete
            if(returnType== Void.TYPE){
                methodCode.append("(($w)clientRef).").append(method.getName()).append("($$);");
                if(argsTypes.length==0){
                    methodCode.append("invokeHandler.completeInvoke(clientType,\"" + method.getName() + "\",null,null);");
                }else{
                    methodCode.append("invokeHandler.completeInvoke(clientType,\"" + method.getName() + "\",null,$args);");
                }
            }else{
                methodCode.append("Object result=(($w)clientRef).").append(method.getName()).append("($$);");
                if(argsTypes.length==0){
                    methodCode.append("invokeHandler.completeInvoke(clientType,\"" + method.getName() + "\",(Object)result,null);");
                }else{
                    methodCode.append("invokeHandler.completeInvoke(clientType,\"" + method.getName() + "\",(Object)result,$args);");
                }
                methodCode.append("return ($r)result;");
            }
            methodCode.append("}catch(Throwable t){");
            //handle invoke exception
            if(argsTypes.length==0){
                methodCode.append("invokeHandler.caughtException(clientType,\"" + method.getName() + "\",t,null);");
            }else{
                methodCode.append("invokeHandler.caughtException(clientType,\"" + method.getName() + "\",t,$args);");
            }

            methodCode.append("throw new ").append(InvokeTargetException.class.getName()).append("(clientType,\""+method.getName()+"\",t);");
            methodCode.append("}");
            methodCode.append("}");
            generator.addMethod(methodCode.toString());
            methodCode.setLength(0);
        }
        generator.addField("public static "+clientType.getName()+" clientRef;");
        generator.addField("public static "+InvokeHandler.class.getName()+" invokeHandler;");
        generator.addField("public static "+Class.class.getName()+" clientType;");
        Class<?> clazz = generator.toClass();
        try {
            ReferenceConfig config = new ReferenceConfig();
            config.setId(clientId);
            config.setCheck(false);
            config.setInterface(clientType.getName());
            clazz.getField("clientRef").set(null,config.get());
            if(!HANDLER_CACHE.containsKey(handlerClass)){
                HANDLER_CACHE.putIfAbsent(handlerClass,handlerClass.newInstance());
            }
            clazz.getField("invokeHandler").set(null,HANDLER_CACHE.get(handlerClass));
            clazz.getField("clientType").set(null,clientType);
            return clazz.newInstance();
        } catch (InstantiationException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        } catch (NoSuchFieldException e) {
            throw new RuntimeException(e);
        }
    }
}
