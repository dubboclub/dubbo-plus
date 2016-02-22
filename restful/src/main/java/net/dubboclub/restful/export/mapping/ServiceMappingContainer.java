package net.dubboclub.restful.export.mapping;

import com.alibaba.dubbo.common.Constants;
import com.alibaba.dubbo.common.URL;
import net.dubboclub.restful.exception.NotFoundServiceException;
import org.apache.commons.lang.StringUtils;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @date: 2016/2/22.
 * @author:bieber.
 * @project:dubbo-plus.
 * @package:net.dubboclub.restful.export.container.
 * @version:1.0.0
 * @fix:
 * @description: 描述功能
 */
public class ServiceMappingContainer {


    private static ConcurrentHashMap<String,ServiceHandler> SERVICE_MAPPING = new ConcurrentHashMap<String,ServiceHandler>();




    public void registerService(URL url,Class serviceType,Object impl){
        SERVICE_MAPPING.putIfAbsent(url.getAbsolutePath(),
                new ServiceHandler(url.getParameter(Constants.GROUP_KEY),
                        url.getParameter(Constants.VERSION_KEY),
                        serviceType, impl));
    }

    public ServiceHandler mappingService(String path,RequestEntity entity){
        if(SERVICE_MAPPING.containsKey(path)){//路径直接匹配
            return SERVICE_MAPPING.get(path);
        }else{//路径并没有匹配，那么说明这个服务存在多个版本实现，前端请求的路径是直接接口请求，所以需要判断接口信息以及版本和分组
            Collection<ServiceHandler> serviceHandlerCollection = SERVICE_MAPPING.values();
            for(ServiceHandler serviceHandler:serviceHandlerCollection){
                if(serviceHandler.getServiceType().getName().equals(path)){
                    if(StringUtils.isEmpty(serviceHandler.getVersion())
                            &&StringUtils.isEmpty(entity.getVersion())){//版本都没有设置，那么他们的版本必须设置，否则会回到上面的逻辑
                        if(serviceHandler.getGroup().equals(entity.getGroup())){
                            return serviceHandler;
                        }
                    }else if(StringUtils.isEmpty(serviceHandler.getGroup())
                            &&StringUtils.isEmpty(entity.getGroup())){
                        if(serviceHandler.getVersion().equals(entity.getVersion())){
                            return serviceHandler;
                        }
                    }else{

                    }
                }
            }
        }
        throw new NotFoundServiceException(path,entity.getVersion(),entity.getGroup());
    }

    public void unregisterService(URL url){
        SERVICE_MAPPING.remove(url.getAbsolutePath());
    }

}
