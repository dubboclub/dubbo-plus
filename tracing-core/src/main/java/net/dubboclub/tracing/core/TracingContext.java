package net.dubboclub.tracing.core;

import org.apache.commons.lang.StringUtils;

import java.util.EmptyStackException;
import java.util.Stack;
import java.util.UUID;

/**
 * TracingContext
 * Created by bieber.bibo on 16/11/2
 * 链路追踪的当前线程上下文
 */

public class TracingContext {

    //用于存储链路的id,在当前线程中
    private static final ThreadLocal<String> TRACE_ID = new ThreadLocal<String>();

    private static final ThreadLocal<Stack<Span>> SPAN_STACK = new ThreadLocal<Stack<Span>>();

    /**
     * 获取当前的rpcId
     * @return
     */
    protected static String getCurrentRpcId(){
        Span span = getCurrentSpan();
        if(span==null){
            return null;
        }
        return span.getRpcId();
    }

    /**
     * 创建一个新的rpcId
     * @return
     */
    protected static String newRpcId(){
        Span currentSpan = TracingContext.getCurrentSpan();
        if(currentSpan==null){
            return Span.START_RPC_ID;
        }
        return currentSpan.nextRPCId();
    }


    protected static void setTraceId(String traceId){
        TRACE_ID.set(traceId);
    }

    protected static String getTraceId(boolean newInNotExist){
        String traceId = TRACE_ID.get();
        if(newInNotExist&&StringUtils.isBlank(traceId)){
            traceId = generateUUID();
            setTraceId(traceId);
        }
        return traceId;
    }


    protected static Span getCurrentSpan(){
        try{
            Stack<Span> spanStack = SPAN_STACK.get();
            if(spanStack!=null){
                Span span = spanStack.peek();
                return span;
            }
            return null;
        }catch (EmptyStackException e){
            return null;
        }
    }

    protected static String getCurrentSpanId(){
        Span span = getCurrentSpan();
        if(span!=null){
            return span.getId();
        }
        return null;
    }


    protected static Span popSpan(){
        try{
            Span span = getCurrentSpan();
            if(span!=null){
                return SPAN_STACK.get().pop();
            }
            return null;
        }catch (EmptyStackException e){
            return null;
        }
    }

    protected static void pushSpan(Span span){
        Stack<Span> spanStack = SPAN_STACK.get();
        if(spanStack==null){
            spanStack=new Stack<Span>();
            SPAN_STACK.set(spanStack);
        }
        spanStack.push(span);
    }


    protected static void clean(){
        TRACE_ID.remove();
        SPAN_STACK.remove();
    }

    protected static String generateUUID(){
        String traceId = UUID.randomUUID().toString();
        return StringUtils.replace(traceId,"-","");
    }





}
