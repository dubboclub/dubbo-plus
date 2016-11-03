package net.dubboclub.tracing.core.config;

import net.dubboclub.tracing.core.exception.TracingException;
import org.apache.commons.lang.StringUtils;

import java.io.*;
import java.util.Properties;

/**
 * Config
 * Created by bieber.bibo on 16/11/3
 */

public class Config {

    //日志收集器类型
    public static final String COLLECTOR_KEY="tracing.collector";

    //异步队列大小
    public static final String SYNC_QUEUE_SIZE="tracing.sync.queue.size";

    //每次写的大小
    public static final String SYNC_FLUSH_SIZE="tracing.sync.flush.size";

    //链路筛选
    public static final String TRACING_SAMPLER_RATIO="tracing.sampler.ratio";

    //配置采样的具体实现
    public static final String TRACING_SAMPLER_KEY="tracing.sampler";

    //默认的采样实现
    public static final String DEFAULT_TRACING_SAMPLER="ratio";

    //异步队列大小默认值
    public static final Integer DEFAULT_SYNC_QUEUE_SIZE=1024;
    //每次写大小默认值
    public static final Integer DEFAULT_FLUSH_SIZE=128;

    public static final String DEFAULT_COLLECTOR="default";

    public static final String DEFAULT_DUBBO_COLLECTOR_FACTORY="default";

    //默认采样比例
    public static final int DEFAULT_SAMPLER_RATIO=20;

    //默认配置文件地址
    private static final String DEFAULT_CONFIG_FILE="/tracing.properties";

    private static final String CONFIG_FILE="tracing.config";

    private static Properties properties = new Properties();

    static {
        String configFile = System.getProperty(CONFIG_FILE);
        InputStream propertiesInputStream=null;
        if(StringUtils.isNotBlank(configFile)){
            if(configFile.startsWith("file:")){
                try {
                    propertiesInputStream = new FileInputStream(new File(configFile.substring(5)));
                } catch (FileNotFoundException e) {
                    throw new TracingException("not found config file ["+configFile+"].");
                }
            }
        }else{
            configFile=DEFAULT_CONFIG_FILE;
            propertiesInputStream = Config.class.getResourceAsStream(configFile);
        }
        if(propertiesInputStream!=null){
            try {
                properties.load(propertiesInputStream);
            } catch (IOException e) {
                throw new TracingException("fail to load config properties.",e);
            }finally {
                try {
                    propertiesInputStream.close();
                } catch (IOException e) {
                    throw new TracingException("fail to close properties inputstream.",e);
                }
            }
        }
    }


    public static Integer getProperty(String key,Integer defaultValue){
        String property = getProperty(key);
        if(StringUtils.isBlank(property)){
            return defaultValue;
        }
        return Integer.parseInt(property.toString());
    }


    public static String getProperty(String key,String defaultValue){
        String property = getProperty(key);
        if(StringUtils.isBlank(property)){
            return defaultValue;
        }
        return property;
    }

    public static String getProperty(String key){
        Object property = properties.get(key);
        if(property==null){
            return null;
        }
        return property.toString();
    }








}
