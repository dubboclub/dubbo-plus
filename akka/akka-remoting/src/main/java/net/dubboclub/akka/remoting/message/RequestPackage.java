package net.dubboclub.akka.remoting.message;

import java.io.Serializable;

/**
 * Created by bieber on 2015/7/10.
 */
public class RequestPackage implements Serializable{

    private byte[] requestMessage;

    public RequestPackage(byte[] requestMessage){
        this.requestMessage = requestMessage;
    }

}
