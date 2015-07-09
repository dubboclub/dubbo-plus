package net.dubboclub.akka.remoting.utils;

import com.alibaba.dubbo.common.Constants;
import com.alibaba.dubbo.common.URL;
import net.dubboclub.akka.remoting.AkkaSystemContext;

/**
 * Created by bieber on 2015/7/10.
 */
public class Utils {

    public static String generateRemoteActorPath(URL url,String actorName){
        StringBuilder path = new StringBuilder();
        path.append("akka.tcp://").append(AkkaSystemContext.SERVICE_SLIDE+"-"+url.getParameter(Constants.APPLICATION_KEY)).append("@");
        path.append(url.getAddress());
        path.append("/user").append(actorName);
        return path.toString();
    }
}
