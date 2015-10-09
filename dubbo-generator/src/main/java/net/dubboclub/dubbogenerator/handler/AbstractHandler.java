package net.dubboclub.dubbogenerator.handler;

import com.alibaba.fastjson.JSON;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by bieber on 2015/10/8.
 */
public abstract class AbstractHandler implements InvokeHandler{
    

    protected static final Logger LOGGER = LoggerFactory.getLogger(InvokeHandler.LOGGER_NAME);

    protected String parseArgsToJson(Object[] args){
        StringBuilder jsonContent = new StringBuilder();
        if(args==null||args.length==0){
            return "empty";
        }else{
            for(Object arg:args){
                jsonContent.append(parseObject2Json(arg)).append(",");
            }
            jsonContent.setLength(jsonContent.length()-1);
        }
        return jsonContent.toString();
    }

    protected String parseObject2Json(Object obj){
        if(obj==null){
            return "null";
        }else{
            return JSON.toJSONString(obj);
        }
    }
}
