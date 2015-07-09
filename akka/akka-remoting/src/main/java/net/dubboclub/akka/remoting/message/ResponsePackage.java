package net.dubboclub.akka.remoting.message;

import java.io.Serializable;

/**
 * Created by bieber on 2015/7/10.
 */
public class ResponsePackage implements Serializable{

    private long id;

    private byte[] responseMessage;

    public ResponsePackage(long id,byte[] responseMessage) {
        this.responseMessage = responseMessage;
    }

    public long getId() {
        return id;
    }

    public byte[] getResponseMessage() {
        return responseMessage;
    }

}
