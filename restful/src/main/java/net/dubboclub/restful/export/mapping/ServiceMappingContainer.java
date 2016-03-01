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


    private static ConcurrentHashMap<String, ServiceHandler> SERVICE_MAPPING = new ConcurrentHashMap<String, ServiceHandler>();

    public void registerService(URL url, Class serviceType, Object impl) {
        SERVICE_MAPPING.putIfAbsent(url.getPath(),
                new ServiceHandler(url.getParameter(Constants.GROUP_KEY),
                        url.getParameter(Constants.VERSION_KEY),
                        serviceType, impl));
    }

    public ServiceHandler mappingService(String path, RequestEntity entity) {
        if (SERVICE_MAPPING.containsKey(path)) {//路径直接匹配，dubbo的默认path是接口全名(数字累加值)，如果配置了path属性，则是该属性值
            ServiceHandler serviceHandler = SERVICE_MAPPING.get(path);
            if (!serviceHandler.getServiceType().getName().equals(path)) {
                return SERVICE_MAPPING.get(path);
            }
        }
        //路径并没有匹配，那么说明这个服务存在多个版本实现，前端请求的路径是直接接口请求，所以需要判断接口信息以及版本和分组
        Collection<ServiceHandler> serviceHandlerCollection = SERVICE_MAPPING.values();
        for (ServiceHandler serviceHandler : serviceHandlerCollection) {
            //它的请求路径可能是类全名或者类名，这样都满足匹配要求，满足则进行版本和分组的校验
            if (serviceHandler.getServiceType().getName().equals(path)||serviceHandler.getServiceType().getSimpleName().equals(path)) {//请求的path是接口全名
                if (!StringUtils.isEmpty(serviceHandler.getVersion())
                        && !StringUtils.isEmpty(serviceHandler.getGroup())) {
                    if (serviceHandler.getVersion().equals(entity.getVersion())
                            && serviceHandler.getGroup().equals(entity.getGroup())) {
                        return serviceHandler;
                    }
                } else {
                    if (!StringUtils.isEmpty(serviceHandler.getVersion())) {
                        if (serviceHandler.getVersion().equals(entity.getVersion())) {
                            return serviceHandler;
                        }
                    } else if (!StringUtils.isEmpty(serviceHandler.getGroup())) {
                        if (serviceHandler.getGroup().equals(entity.getGroup())) {
                            return serviceHandler;
                        }
                    }else if(StringUtils.isEmpty(serviceHandler.getVersion())
                            && StringUtils.isEmpty(serviceHandler.getGroup())&&StringUtils.isEmpty(entity.getVersion())
                            && StringUtils.isEmpty(entity.getGroup())){
                        return serviceHandler;
                    }
                }

            }
        }
        throw new NotFoundServiceException(path, entity.getVersion(), entity.getGroup());
    }

    public void unregisterService(URL url) {
        SERVICE_MAPPING.remove(url.getAbsolutePath());
    }

}
