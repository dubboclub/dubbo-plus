package net.dubboclub.akka.remoting.actor;

import akka.actor.ActorRef;
import akka.actor.Props;
import akka.actor.UntypedActor;
import akka.japi.Creator;
import akka.routing.ActorRefRoutee;
import com.alibaba.dubbo.rpc.*;
import net.dubboclub.akka.remoting.codec.AkkaCodec;
import net.dubboclub.akka.remoting.message.Request;
import net.dubboclub.akka.remoting.message.RequestPackage;
import net.dubboclub.akka.remoting.message.Response;
import net.dubboclub.akka.remoting.message.ResponsePackage;

/**
 * Created by bieber on 2015/7/8.
 */
public class ServiceProvider extends RouterActor {

    private Invoker<?> invoker;

    private volatile int currentWorkerSize = 0;

    public ServiceProvider(Invoker<?> invoker){
        super(invoker.getUrl());
        this.invoker = invoker;
    }

    @Override
    public void preStart() throws Exception {
        super.preStart();
        logging.info("start service {}", getSelf().path());
    }

    @Override
    public void onReceive(Object o) throws Exception {
        if(o instanceof RequestPackage){
            if(currentWorkerSize>=MAX_WORKER_SIZE){
                router.route(o,getSender());
            }else{
                ActorRef worker = getContext().actorOf(Props.create(new WorkerCreator(invoker)));
                router=router.addRoutee(new ActorRefRoutee(worker));
                currentWorkerSize++;
                worker.tell(o,getSender());
            }
        }else{
            unhandled(o);
        }
    }

    private static void wrapErrorResponse(byte status, Throwable e, Response response){
        response.setStatus(Response.SERVER_ERROR);
        RpcResult result = new RpcResult();
        result.setException(e);
        response.setResult(result);
    }

    static class WorkerCreator implements Creator<Worker>{

        private Invoker invoker;

        public WorkerCreator(Invoker invoker) {
            this.invoker = invoker;
        }

        @Override
        public Worker create() throws Exception {
            return new Worker(invoker);
        }
    }

    static class Worker extends UntypedActor{
        private Invoker<?> invoker;

        protected Worker(Invoker<?> invoker){
            this.invoker = invoker;
        }
        @Override
        public void onReceive(Object o) throws Exception {
            RequestPackage requestPackage = (RequestPackage) o;
            Response response = new Response(requestPackage.getmId());
            Request request = null;
            try{
                request = (Request) AkkaCodec.decode(invoker.getUrl(),requestPackage.getRequestMessage());
            }catch (Exception e){
                wrapErrorResponse(Response.SERVER_ERROR,e,response);
            }
            Object data = request.getmData();
            if(data instanceof Invocation){
                Invocation invocation = (Invocation) request.getmData();
                try{
                    Result result = invoker.invoke(invocation);
                    response.setResult(result);
                    response.setStatus(Response.OK);
                }catch (Throwable e){
                    wrapErrorResponse(Response.SERVICE_ERROR,e,response);
                }
            }else{
                RpcException e = new RpcException("unsupported request "+data);
                wrapErrorResponse(Response.SERVER_ERROR,e,response);
            }
            ResponsePackage responsePackage = new ResponsePackage(response.getId(),AkkaCodec.encode(invoker.getUrl(),response).array());
            getSender().tell(responsePackage,getSelf());
        }

    }


}
