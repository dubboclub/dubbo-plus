package net.dubboclub.tracing.client.dubbo;

import com.alibaba.dubbo.common.Constants;
import com.alibaba.dubbo.common.URL;
import com.alibaba.dubbo.common.extension.Activate;
import com.alibaba.dubbo.rpc.*;
import net.dubboclub.tracing.core.Span;
import net.dubboclub.tracing.core.Tracer;
import net.dubboclub.tracing.core.collector.TracingCollector;
import net.dubboclub.tracing.core.config.Config;

/**
 * Created by Zetas on 2016/7/8.
 */
@Activate(group = {Constants.PROVIDER, Constants.CONSUMER})
public class TracingFilter implements Filter {

    public Result invoke(Invoker<?> invoker, Invocation invocation) throws RpcException {
        if(invoker.getInterface()== TracingCollector.class){
            return invoker.invoke(invocation);
        }
        boolean isConsumerSide = isConsumerSide();
        startTracing(!isConsumerSide);
        try {
            RpcContext rpcContext = RpcContext.getContext();
            Span span = null;
            if(isConsumerSide){
                span = Tracer.startSpan(isConsumerSide? Span.SpanType.SENDER: Span.SpanType.REVIVER,
                        generateSpanName(invoker.getInterface(),
                                invocation.getMethodName(),isConsumerSide));
            }else{
                span = Tracer.startSpan(isConsumerSide? Span.SpanType.SENDER: Span.SpanType.REVIVER,
                        generateSpanName(invoker.getInterface(),
                                invocation.getMethodName(),isConsumerSide),rpcContext.getAttachment(Tracer.RPC_ID_KEY));
            }
            span.setBizType(Span.SpanBizType.RPC);
            span.setApplication(invoker.getUrl().getParameter(Constants.APPLICATION_KEY));
            addAnnotation(isConsumerSide);
            attachTracing();
            Result result = invoker.invoke(invocation);
            return result;
        } catch (RpcException e) {
            Tracer.addErrorAnnotation(e);
            throw e;
        } finally {
            Tracer.stopSpan();
        }
    }

    private void attachTracing(){
        RpcContext rpcContext = RpcContext.getContext();
        rpcContext.setAttachment(Tracer.TRACE_ID_KEY,Tracer.getTraceId());
        rpcContext.setAttachment(Tracer.RPC_ID_KEY,Tracer.getRpcId());
    }


    private void startTracing(boolean isProviderEnd){
        if(isProviderEnd){
            RpcContext rpcContext = RpcContext.getContext();
            Tracer.startTracing(rpcContext.getAttachment(Tracer.TRACE_ID_KEY));
        }else{
            Tracer.startTracing();
        }
    }

    private void addAnnotation(boolean isConsumerSide){
        RpcContext rpcContext = RpcContext.getContext();
        if(isConsumerSide){
            Tracer.addHostAnnotation(rpcContext.getLocalHost());
        }else{
            Tracer.addHostAnnotation(rpcContext.getLocalHost());
            Tracer.addRemoteHostAnnotation(rpcContext.getRemoteAddress().getHostName());
        }
    }

    private String generateSpanName(Class<?> clazz,String method,boolean isConsumerSide){
        if(isConsumerSide){
            return "Cell=>"+clazz.getName()+":"+method;
        }else{
            return "Echo=>"+clazz.getName()+":"+method;
        }
    }

    private boolean isConsumerSide(){
        URL url =  RpcContext.getContext().getUrl();
        return Constants.CONSUMER_SIDE.equals(url.getParameter(Constants.SIDE_KEY));
    }

}
