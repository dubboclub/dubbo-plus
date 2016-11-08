package net.dubboclub.restful.exception;

import org.apache.commons.lang.StringUtils;

/**
 * @date: 2016/2/22.
 * @author:bieber.
 * @project:dubbo-plus.
 * @package:net.dubboclub.restful.exception.
 * @version:1.0.0
 * @fix:
 * @description: 没有找到对应的请求服务实体
 */
public class NotFoundServiceException extends RuntimeException {

    private String path;

    private String version;

    private String group;

    public NotFoundServiceException(String path, String version, String group) {
        this.path = path;
        this.version = version;
        this.group = group;
    }

    @Override
    public String getMessage() {
        return "Not found service for path:["+path+"] " +
                "version:["+ (StringUtils.isEmpty(version)?"*":version)+"] " +
                "group ["+(StringUtils.isEmpty(group)?"*":group)+"].";
    }
}
