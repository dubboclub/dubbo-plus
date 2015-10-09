package net.dubboclub.dubbogenerator;

import com.alibaba.dubbo.common.utils.StringUtils;
import javassist.*;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Created by bieber on 2015/7/31.
 */
public class JavassistClassGenerator {

    private List<String> mFields,mMethods, mInterfaces;
    
    private static  final AtomicLong CLASS_COUNTER = new AtomicLong(0);

    private ClassPool classPool ;
    
    private CtClass ctClass;
    
    private String superClass;
    
    private String className;
    
    
    private static final ConcurrentHashMap<ClassLoader,ClassPool> LOADER_POOL = new ConcurrentHashMap<ClassLoader, ClassPool>();
    
    private JavassistClassGenerator(ClassPool classPool){
        this.classPool = classPool;
    }
    
    private static ClassLoader getCallerClassLoader(){
        return JavassistClassGenerator.class.getClassLoader();
    }
    public static JavassistClassGenerator newInstance(){
        return newInstance(getCallerClassLoader());
    }
    public static JavassistClassGenerator newInstance(ClassLoader classLoader){
        return new JavassistClassGenerator(getClassPool(classLoader));
    }
    
    public void setClassName(String className){
        this.className = className;
    }
    
    public void setSuperClass(String superClass){
        this.superClass = superClass;
    }
    
    public void addInterface(String interfaceName){
        if(mInterfaces ==null){
            mInterfaces = new ArrayList<String>();
        }
        if(!StringUtils.isEmpty(interfaceName)){
            mInterfaces.add(interfaceName);
        }
    }
    
    public void addField(String fieldCode){
        if(mFields==null){
            mFields = new ArrayList<String>();
        }
        if(!StringUtils.isEmpty(fieldCode)){
            mFields.add(fieldCode);
        }
    }
    
    public void addMethod(String methodCode){
        if(mMethods==null){
            mMethods = new ArrayList<String>();
        }
        if(!StringUtils.isEmpty(methodCode)){
            mMethods.add(methodCode);
        }
    }
    
    public Class<?> toClass(){
        if(ctClass!=null){
            ctClass.detach();
        }
        try {
            long id = CLASS_COUNTER.getAndIncrement();
            if(StringUtils.isEmpty(className)){
                className=(superClass==null?JavassistClassGenerator.class.getSimpleName():superClass+"$jcg")+id;
            }
            CtClass ctcs = superClass == null ? null : classPool.get(superClass);
            ctClass = classPool.makeClass(className);
            if(ctcs!=null){
                ctClass.setSuperclass(ctcs);
            }
            if(mInterfaces !=null){
                for(String mInterface:mInterfaces){
                    ctClass.addInterface(classPool.get(mInterface));
                }
            }
            if(mFields!=null){
                for(String mField:mFields){
                    ctClass.addField(CtField.make(mField,ctClass));
                }
            }
            if(mMethods!=null){
                for(String mMethod:mMethods){
                    ctClass.addMethod(CtNewMethod.make(mMethod,ctClass));
                }
            }
            ctClass.addConstructor(CtNewConstructor.defaultConstructor(ctClass));
            return ctClass.toClass(getCallerClassLoader(),null);
        }catch (RuntimeException e){
            throw e;
        }catch (NotFoundException e) {
            throw new RuntimeException(e.getMessage(),e);
        } catch (CannotCompileException e) {
            throw new RuntimeException(e.getMessage(),e);
        }finally {
            release();
        }
    }
    
    private void release(){
        if(ctClass!=null)ctClass.detach();
        if(mFields!=null)mFields.clear();
        if(mMethods!=null)mMethods.clear();
    }

    public static ClassPool getClassPool(ClassLoader loader)
    {
        if( loader == null )
            return ClassPool.getDefault();

        ClassPool pool = LOADER_POOL.get(loader);
        if( pool == null )
        {
            pool = new ClassPool(true);
            pool.appendClassPath(new LoaderClassPath(loader));
            LOADER_POOL.put(loader, pool);
        }
        return pool;
    }
    
    
}
