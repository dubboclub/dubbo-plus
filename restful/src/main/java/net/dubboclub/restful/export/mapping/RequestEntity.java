package net.dubboclub.restful.export.mapping;

import com.alibaba.fastjson.JSONObject;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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


    private final  static Pattern argPattern = Pattern.compile("^(arg[1-9]{1}[0-9]{0,})$");
    private final  static Pattern argIndexPattern = Pattern.compile("[1-9]{1}[0-9]{0,}");

    private String group;

    private String version;

    private String[] args;

    private String method;

    public RequestEntity(){

    }

    public RequestEntity(JSONObject jsonObject){
         if(jsonObject!=null){
             this.setGroup(jsonObject.getString("group"));
             this.setVersion(jsonObject.getString("version"));
             this.setMethod(jsonObject.getString("method"));
             Set<String> keys = jsonObject.keySet();
             for(String key:keys){
                 Matcher matcher = argPattern.matcher(key);
                 if(matcher.matches()){
                     matcher = argIndexPattern.matcher(key);
                     matcher.find();
                     int index = Integer.parseInt(matcher.group());
                     setArg(index,jsonObject.getString(key));
                 }
             }
         }
    }

    /**
     * 注意此时的index是从1开始，而非0
     * @param index
     * @param arg
     */
    public void setArg(int index,String arg){
        if(args==null){
            args = new String[index];
        }else if(args.length<index){
            String[] newArgs  = new String[index];
            System.arraycopy(args,0,newArgs,0,args.length);
            args = newArgs;
        }
        args[index-1]=arg;
    }

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

    public String[] getArgs() {
        return args;
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
                ", args=" + Arrays.toString(args) +
                ", method='" + method + '\'' +
                '}';
    }
}
