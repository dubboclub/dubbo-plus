package net.dubboclub.akka.remoting.utils;

import com.alibaba.dubbo.common.URL;
import net.dubboclub.akka.remoting.ActorSystemBootstrap;
import net.dubboclub.akka.remoting.actor.dispatcher.DispatchActor;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.Socket;
import java.net.URLEncoder;

/**
 * Created by bieber on 2015/7/10.
 */
public class Utils {

    public static String generateRemoteActorPath(URL url,String actorName){
        StringBuilder path = new StringBuilder();
        path.append("akka.tcp://").append(ActorSystemBootstrap.SYSTEM_NAME+"_PROVIDER").append("@");
        path.append(url.getAddress());
        path.append("/user/").append(DispatchActor.AKKA_ROOT_SUPERVISOR_ACTOR_NAME).append("/").append(actorName);
        return path.toString();
    }

    public static String formatActorName(String actorName){
        try {
            return URLEncoder.encode(actorName,"UTF-8");
        } catch (UnsupportedEncodingException e) {
            return actorName;
        }
    }

    public static int selectUnbindPort(){
        int start = 2999;
        while(true){
            try {
                Socket socket = new Socket("localhost",start);
                start++;
            } catch (IOException e) {
                return start;
            }
        }
    }
}
