package net.dubboclub.akka.remoting.actor;

import com.alibaba.dubbo.remoting.RemotingException;
import com.alibaba.dubbo.remoting.exchange.ResponseCallback;
import com.alibaba.dubbo.remoting.exchange.ResponseFuture;
import net.dubboclub.akka.remoting.message.Response;

import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by bieber on 2015/7/9.
 */
public class AkkaFuture implements ResponseFuture {
    
    private static final ConcurrentHashMap<Long,AkkaFuture> FUTURES = new ConcurrentHashMap<Long, AkkaFuture>();
    
    private volatile Response response;
    
    private volatile  boolean done = false;
    
    private volatile  boolean send=false;
    
    private ResponseCallback callback;
    
    public void doSend(Long id){
         send=true;
    }
    
    public static void receive(Response response){
        if(FUTURES.containsKey(response.getId())){
            AkkaFuture future = FUTURES.get(response.getId());
            future.response = response;
            future.done=true;
        }
    }
    
    
    public AkkaFuture(Long id){
        FUTURES.put(id,this);
    }
    
    
    
    @Override
    public Object get() throws RemotingException {
        while(!isDone()){
            //waiting
        }
        return response.getResult();
    }

    @Override
    public Object get(int timeoutInMillis) throws RemotingException {
        return null;
    }

    @Override
    public void setCallback(ResponseCallback callback) {
        if(isDone()){

        }else{
            this.callback = callback;
        }
    }

    @Override
    public boolean isDone() {
        return done;
    }
}
