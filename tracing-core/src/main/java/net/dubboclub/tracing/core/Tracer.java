package net.dubboclub.tracing.core;

import net.dubboclub.tracing.core.config.Config;
import net.dubboclub.tracing.core.sampler.Sampler;
import net.dubboclub.tracing.core.transfer.DefaultSyncTransfer;
import net.dubboclub.tracing.core.transfer.SyncTransfer;

/**
 * Tracer
 * Created by bieber.bibo on 16/11/2
 */

public class Tracer {

    private static SyncTransfer syncTransfer = new DefaultSyncTransfer();

    private static Sampler sampler = ComponentLoader.getComponent(Config.getProperty(Config.TRACING_SAMPLER_KEY,Config.DEFAULT_TRACING_SAMPLER),Sampler.class);

    public static final String TRACE_ID_KEY="traceId";

    public static final String RPC_ID_KEY="rpcId";

    static {
        syncTransfer.start();
    }

    /**
     * 开始链路追踪,当当前链路是包含在其他链路里面
     * @param traceId
     */
    public static void startTracing(String traceId){
        TracingContext.setTraceId(traceId);
    }

    /**
     * 开始链路追踪,该链路是初始链路
     */
    public static void startTracing(){
        //do nothing
    }


    /**
     * 开启一个新的跨度
     * @param spanType
     * @return
     */
    public static Span startSpan(Span.SpanType spanType,String spanName,String rpcId){
        Span span;
        if(!sampler.next(TracingContext.getTraceId(true))){
            span = new NullSpan(spanType,spanName,rpcId);
        }else{
            span = new Span(spanType,spanName,rpcId);
        }
        return span;
    }

    /**
     * 开启一个新的跨度
     * @param spanType
     * @return
     */
    public static Span startSpan(Span.SpanType spanType,String spanName){
        Span span = startSpan(spanType,spanName,null);
        return span;
    }




    public static void setRpcId(String rpcId){
        Span span = TracingContext.getCurrentSpan();
        if(span!=null){
            span.setRpcId(rpcId);
        }
    }

    /**
     * 结束一个跨度
     */
    public static void stopSpan(){
        Span span =  TracingContext.popSpan();
        if(span!=null&&!(span instanceof NullSpan)){
            span.stop();
            SpanBean spanBean = new SpanBean(span);
            syncTransfer.syncSend(spanBean);
        }
    }

    public static String getTraceId(){
        return TracingContext.getTraceId(false);
    }


    public static String generateNextRpcId(){
        Span span = TracingContext.getCurrentSpan();
        if(span!=null){
            return span.nextRPCId();
        }
        return null;
    }

    public static String getRpcId(){
        return TracingContext.getCurrentRpcId();
    }

    /**
     * 在当前的跨度中添加申明信息
     * @param key
     * @param value
     */
    public static void addAnnotation(String key,String value){
        Span span = TracingContext.getCurrentSpan();
        span.addAnnotation(key,value);
    }

    public static void addErrorAnnotation(Throwable throwable){
        addAnnotation("error",getCause(throwable).getClass().getName());
    }

    public static void addHostAnnotation(String ip){
        addAnnotation("ip",ip);
    }

    public static void addRemoteHostAnnotation(String ip){
        addAnnotation("remote",ip);
    }

    public static void addPortAnnotation(int port){
        addAnnotation("port",port+"");
    }


    private static Throwable getCause(Throwable throwable){
        int maxLoop=20;
        while(throwable.getCause()!=null){
            throwable = throwable.getCause();
            maxLoop--;
            if(maxLoop<=0){
                break;
            }
        }
        return throwable;
    }


}
