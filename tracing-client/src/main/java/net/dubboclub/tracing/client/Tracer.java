package net.dubboclub.tracing.client;

import com.alibaba.dubbo.common.Constants;
import com.alibaba.dubbo.common.extension.ExtensionLoader;
import com.alibaba.dubbo.common.utils.ConfigUtils;
import com.alibaba.dubbo.common.utils.StringUtils;
import com.alibaba.dubbo.rpc.RpcContext;
import net.dubboclub.tracing.api.Annotation;
import net.dubboclub.tracing.api.BinaryAnnotation;
import net.dubboclub.tracing.api.Endpoint;
import net.dubboclub.tracing.api.Span;
import net.dubboclub.tracing.client.util.GUId;
import net.dubboclub.tracing.client.util.Sampler;

/**
 * Created by Zetas on 2016/7/8.
 */
public class Tracer {

    private SyncTransfer syncTransfer = ExtensionLoader.getExtensionLoader(SyncTransfer.class)
            .getExtension(ConfigUtils.getProperty(DstConstants.SYNC_TRANSFER_TYPE,DstConstants.DEFAULT_SYNC_TRANSFER));

    public void init(){
        syncTransfer.start();
    }



    public void beforeInvoke(boolean isConsumerSide) {
        if (isConsumerSide) {
            String traceId = createConsumerSideTraceId();
            if(traceId!=null){
                createConsumerSideSpan();
                addClientSendAnnotation();
            }
        } else{
            createProvideSideTraceId();
            createProviderSideSpan();
            addServerReceiveAnnotation();
        }

        setAttachment();
    }

    public void afterInvoke(boolean isConsumerSide) {
        if (isConsumerSide) {
            addClientReceiveAnnotation();
        } else{
            addServerSendAnnotation();
        }
        send();
    }

    private void send() {
        //弹出栈顶span
        Span span = ContextHolder.popSpan();
        if (span != null) {
            syncTransfer.syncSend(span);
        }
    }

    public void addException(Throwable throwable) {
        Span span = ContextHolder.getSpan();
        if (span != null) {
            Endpoint endpoint = createEndpoint();
            BinaryAnnotation annotation = new BinaryAnnotation();
            annotation.setKey(DstConstants.EXCEPTION);
            annotation.setType(throwable.getClass().getName());
            annotation.setValue(throwable.getMessage());
            annotation.setHost(endpoint);
            span.addAnnotation(annotation);
        }
    }

    private Span createConsumerSideSpan() {
        if(ContextHolder.isSample()){
            Span span = new Span();
            span.setId(GUId.singleton().nextId());
            Span parentSpan = ContextHolder.getSpan();
            if (parentSpan != null) {
                span.setParentId(parentSpan.getId());
                span.setTraceId(parentSpan.getTraceId());
            } else {
                span.setTraceId(ContextHolder.getTraceId());
            }
            span.setServiceName(getServiceName());
            span.setName(getMethodName());
            ContextHolder.setSpan(span);
        }
        return ContextHolder.getSpan();
    }

    private Span createProviderSideSpan() {
        RpcContext rpcContext = RpcContext.getContext();
        String traceId = rpcContext.getAttachment(DstConstants.DST_TRACE_ID);
        String spanId = rpcContext.getAttachment(DstConstants.DST_SPAN_ID);
        String parentSpanId = rpcContext.getAttachment(DstConstants.DST_PARENT_SPAN_ID);
        if (StringUtils.isNotEmpty(traceId)
                && StringUtils.isNotEmpty(spanId)) {//只需要判断traceId和spanid即可
            Span span = new Span();
            span.setId(spanId);
            span.setParentId(parentSpanId);
            span.setTraceId(traceId);
            span.setServiceName(getServiceName());
            span.setName(getMethodName());
            ContextHolder.setSpan(span);
        }

        return ContextHolder.getSpan();
    }

    private String createConsumerSideTraceId() {
        String traceId = ContextHolder.getTraceId();
        if (StringUtils.isBlank(traceId)) {//启动一个新的链路
            if(ContextHolder.isSample()&&Sampler.isSample(getServiceName())){
                ContextHolder.setTraceId(GUId.singleton().nextId());
            }else{
                ContextHolder.setLocalSample(false);
            }
        }
        return ContextHolder.getTraceId();
    }

    private String createProvideSideTraceId() {
        RpcContext rpcContext = RpcContext.getContext();
        String isSample = rpcContext.getAttachment(DstConstants.DST_IS_SAMPLE);
        if(StringUtils.isNotEmpty(isSample)){
            ContextHolder.setLocalSample(Boolean.valueOf(isSample));
        }
        String traceId = rpcContext.getAttachment(DstConstants.DST_TRACE_ID);
        if (StringUtils.isBlank(traceId)) {
            ContextHolder.setTraceId(GUId.singleton().nextId());
        } else {
            ContextHolder.setTraceId(traceId);
        }
        return ContextHolder.getTraceId();
    }

    private void setAttachment() {
        RpcContext rpcContext = RpcContext.getContext();
        String traceId = ContextHolder.getTraceId();
        rpcContext.setAttachment(DstConstants.DST_IS_SAMPLE,ContextHolder.isSample()+"");
        if (traceId != null) {
            rpcContext.setAttachment(DstConstants.DST_TRACE_ID, traceId);
        }
        Span span = ContextHolder.getSpan();
        if (span != null) {
            rpcContext.setAttachment(DstConstants.DST_SPAN_ID, span.getId());
            rpcContext.setAttachment(DstConstants.DST_PARENT_SPAN_ID, span.getParentId());
        }
    }

    private Endpoint createEndpoint() {
        Endpoint endpoint = new Endpoint();
        endpoint.setApplicationName(getApplicationName());
        endpoint.setIp(getIp());
        endpoint.setPort(getPort());
        return endpoint;
    }

    private long currentTimeMillis() {
        return System.currentTimeMillis();
    }

    private void addClientSendAnnotation() {
        addAnnotation(Annotation.CLIENT_SEND);
    }

    private void addClientReceiveAnnotation() {
        addAnnotation(Annotation.CLIENT_RECEIVE);
    }

    private void addServerSendAnnotation() {
        addAnnotation(Annotation.SERVER_SEND);
    }

    private void addServerReceiveAnnotation() {
        addAnnotation(Annotation.SERVER_RECEIVE);
    }

    private void addAnnotation(String value) {
        Span span = ContextHolder.getSpan();
        if (span != null) {
            Endpoint endpoint = createEndpoint();
            Annotation annotation = new Annotation();
            annotation.setValue(value);
            annotation.setHost(endpoint);
            annotation.setTimestamp(currentTimeMillis());
            span.addAnnotation(annotation);
        }
    }

    private String getApplicationName() {
        return RpcContext.getContext().getUrl().getParameter(Constants.APPLICATION_KEY);
    }

    private String getServiceName() {
        return RpcContext.getContext().getUrl().getServiceInterface();
    }

    private String getMethodName() {
        return RpcContext.getContext().getMethodName();
    }

    private String getIp() {
        return RpcContext.getContext().getLocalHost();
    }

    private Integer getPort() {
        return RpcContext.getContext().getLocalPort();
    }

}
