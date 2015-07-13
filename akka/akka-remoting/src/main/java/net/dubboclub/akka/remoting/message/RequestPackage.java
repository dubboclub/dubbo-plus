package net.dubboclub.akka.remoting.message;

import java.io.Serializable;

/**
 * Created by bieber on 2015/7/10.
 */
public class RequestPackage implements Serializable{

    private long mId;

    private byte[] requestMessage;

    public RequestPackage(long mid,byte[] requestMessage){
        this.requestMessage = requestMessage;
        this.mId=mid;
    }

    public byte[] getRequestMessage() {
        return requestMessage;
    }

    public long getmId() {
        return mId;
    }
}
