package net.dubboclub.restful.export.mapping;

import java.io.Serializable;

/**
 * @date: 2016/2/22.
 * @author:bieber.
 * @project:dubbo-plus.
 * @package:net.dubboclub.restful.export.mapping.
 * @version:1.0.0
 * @fix:
 * @description: 请求的实体
 */
public class RequestEntity implements Serializable {

    private String group;

    private String version;

    private String arg;

    private String method;


    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getArg() {
        return arg;
    }

    public void setArg(String arg) {
        this.arg = arg;
    }


    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    @Override
    public String toString() {
        return "RequestEntity{" +
                "group='" + group + '\'' +
                ", version='" + version + '\'' +
                ", arg='" + arg + '\'' +
                ", method='" + method + '\'' +
                '}';
    }
}
