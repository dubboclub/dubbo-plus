package net.dubboclub.akka.remoting.message;

import com.alibaba.dubbo.rpc.Result;

import java.io.Serializable;

/**
 * Created by bieber on 2015/7/9.
 */
public class Response implements Serializable{


    /**
     * ok.
     */
    public static final byte OK                = 20;

    /**
     * clien side timeout.
     */
    public static final byte CLIENT_TIMEOUT    = 30;

    /**
     * server side timeout.
     */
    public static final byte SERVER_TIMEOUT    = 31;

    /**
     * request format error.
     */
    public static final byte BAD_REQUEST       = 40;

    /**
     * response format error.
     */
    public static final byte BAD_RESPONSE      = 50;

    /**
     * service not found.
     */
    public static final byte SERVICE_NOT_FOUND = 60;

    /**
     * service error.
     */
    public static final byte SERVICE_ERROR     = 70;

    /**
     * internal server error.
     */
    public static final byte SERVER_ERROR      = 80;

    /**
     * internal server error.
     */
    public static final byte CLIENT_ERROR      = 90;


    private long id;
    
    private Result result;

    private byte status;


    public Response(long id) {
        this.id = id;
    }

    public void setResult(Result result) {
        this.result = result;
    }

    public void setStatus(byte status) {
        this.status = status;
    }

    public long getId() {
        return id;
    }

    public Result getResult() {
        return result;
    }

    public byte getStatus(){
        return status;
    }
}
