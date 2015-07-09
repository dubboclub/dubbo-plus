package net.dubboclub.akka.remoting.message;

import java.io.Serializable;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Created by bieber on 2015/7/9.
 */
public class Request implements Serializable{
    
    private long requestId;
    
    private Object mData;
    
    private String actorName;

    private boolean broken=false;
    
    private static final AtomicLong REQUEST_SEQUENCE= new AtomicLong(0);

    public Request(Object mData,String actorName) {
        this.requestId = REQUEST_SEQUENCE.getAndIncrement();
        this.mData = mData;
        this.actorName = actorName;
    }

    public Request(long requestId){
        this.requestId = requestId;
    }

    public void setBroken(boolean broken) {
        this.broken = broken;
    }

    public void setData(Object mData) {
        this.mData = mData;
    }

    public boolean isBroken() {
        return broken;
    }

    public long getRequestId() {
        
        return requestId;
    }

    public Object getmData() {
        return mData;
    }

    public String getActorName() {
        return actorName;
    }
}
