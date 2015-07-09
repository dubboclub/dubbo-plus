package net.dubboclub.akka.remoting.message;

import java.io.Serializable;

/**
 * Created by bieber on 2015/7/9.
 */
public class Response implements Serializable{
    
    private long id;
    
    private Object result;

    public Response(long id, Object result) {
        this.id = id;
        this.result = result;
    }

    public long getId() {
        return id;
    }

    public Object getResult() {
        return result;
    }
}
