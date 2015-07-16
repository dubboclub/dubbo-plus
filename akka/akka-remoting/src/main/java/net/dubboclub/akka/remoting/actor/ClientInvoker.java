package net.dubboclub.akka.remoting.actor;

import akka.actor.*;
import akka.japi.Creator;
import akka.japi.Procedure;
import akka.routing.*;
import com.alibaba.dubbo.common.Constants;
import com.alibaba.dubbo.common.URL;
import com.alibaba.dubbo.rpc.Result;
import com.alibaba.dubbo.rpc.RpcException;
import com.alibaba.dubbo.rpc.RpcResult;
import net.dubboclub.akka.remoting.codec.AkkaCodec;
import net.dubboclub.akka.remoting.message.Request;
import net.dubboclub.akka.remoting.message.RequestPackage;
import net.dubboclub.akka.remoting.message.Response;
import net.dubboclub.akka.remoting.message.ResponsePackage;
import net.dubboclub.akka.remoting.utils.Utils;
import scala.concurrent.duration.Duration;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Created by bieber on 2015/7/8.
 * 每个服务的URL都会对应一个ClientInvoker
 */
public class ClientInvoker extends RouterActor {

    private Class<?> type;

    private URL url;

    private volatile ActorRef remoteActorRef;

    private String remotePath;

    private static final int INIT=0;

    private volatile boolean connected=false;

    private List<Request> tempRequestList = new ArrayList<Request>();

    public ClientInvoker(Class<?> type, URL url) {
        super(url);
        this.type = type;
        this.url = url;
        try {
            this.remotePath = Utils.generateRemoteActorPath(url,URLEncoder.encode(url.getServiceKey(),"UTF-8"));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void preStart() throws Exception {
        super.preStart();
        boolean isLazy = url.getParameter(Constants.LAZY_CONNECT_KEY,Constants.DEFAULT_LAZY_CONNECT_INITIAL_STATE);
        if(!isLazy){
            connect(INIT);
        }
    }

    private void connect(Object attachment){
        connected=true;
        getContext().actorSelection(remotePath).tell(new Identify(attachment), getSelf());
        getContext().system().scheduler().scheduleOnce(Duration.create(url.getParameter(Constants.CONNECT_TIMEOUT_KEY,Constants.DEFAULT_CONNECT_TIMEOUT), TimeUnit.MILLISECONDS),getSelf(),ReceiveTimeout.getInstance(),getContext().dispatcher(),null);
    }

    private void responseServiceNotFound(Request request){
        Response response = new Response(request.getRequestId());
        response.setStatus(Response.SERVICE_NOT_FOUND);
        RpcResult result = new RpcResult();
        result.setException(new RpcException("SERVICE NOT FOUND"));
        response.setResult(result);
        AkkaFuture.receive(response);
    }

    private void responseConnectedTimeout(Request request){
        Response response = new Response(request.getRequestId());
        response.setStatus(Response.CLIENT_TIMEOUT);
        RpcResult result = new RpcResult();
        result.setException(new RpcException("Connected timeout"));
        response.setResult(result);
        AkkaFuture.receive(response);
    }

    @Override
    public void onReceive(Object o) throws Exception {
        if(o instanceof ActorIdentity){
            System.out.println("received......");
            getContext().become(requestProcedure);
            ActorIdentity identity = (ActorIdentity) o;
            if(identity.getRef()==null){
                if(tempRequestList.size()>0){
                    Iterator<Request> requestIterator = tempRequestList.iterator();
                    while(requestIterator.hasNext()){
                        responseServiceNotFound(requestIterator.next());
                        requestIterator.remove();
                    }
                }
                if(identity.correlationId() instanceof Request){
                    responseServiceNotFound((Request) identity.correlationId());
                }
                logging.debug("not found remote service {}",remotePath);
            }else{
                remoteActorRef = identity.getRef();
                if(identity.correlationId() instanceof Request){
                    requestRemote((Request) identity.correlationId());
                }
                if(tempRequestList.size()>0){
                    Iterator<Request> requestIterator = tempRequestList.iterator();
                    while(requestIterator.hasNext()){
                        requestRemote(requestIterator.next());
                        requestIterator.remove();
                    }
                }
            }
        }else if(o instanceof Request){
            Request request = (Request) o;
            if(!connected){
                System.out.println("connecting.......");
                connect(o);
            }else{
                tempRequestList.add(request);
            }
        }else if(o instanceof ReceiveTimeout){
            connected=false;
            Iterator<Request> requestIterator = tempRequestList.iterator();
            while(requestIterator.hasNext()){
                responseConnectedTimeout(requestIterator.next());
                requestIterator.remove();
            }
            logging.debug("connect remoute {} timeout", remotePath);
        }else{
            unhandled(o);
        }
    }

    private void requestRemote(Request request){
        try{
            RequestPackage requestPackage = new RequestPackage(request.getRequestId(),AkkaCodec.encode(url,request).array());
            remoteActorRef.tell(requestPackage,getSelf());
        }catch (IOException e){
            Response response = new Response(request.getRequestId());
            response.setStatus(Response.CLIENT_ERROR);
            RpcResult result = new RpcResult();
            result.setException(e);
            response.setResult(result);
            AkkaFuture.receive(response);
        }
    }

    private Procedure<Object> requestProcedure = new Procedure<Object>() {
        @Override
        public void apply(Object o) throws Exception {
            if(o instanceof Request){
                Request request = (Request) o;
                if(remoteActorRef==null){
                    responseServiceNotFound(request);
                }else{
                    requestRemote(request);
                }
            }else if(o instanceof ResponsePackage){
                ResponsePackage responsePackage = (ResponsePackage) o;
                try{
                    Response response = (Response) AkkaCodec.decode(url, responsePackage.getResponseMessage());
                    AkkaFuture.receive(response);
                }catch (IOException e){
                    Response response = new Response(responsePackage.getId());
                    response.setStatus(Response.CLIENT_ERROR);
                    RpcResult result = new RpcResult();
                    result.setException(e);
                    response.setResult(result);
                    AkkaFuture.receive(response);
                }
            }else{
                unhandled(o);
            }
        }
    };
}
