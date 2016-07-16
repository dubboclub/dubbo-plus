package net.dubboclub.tracing.client;

import com.alibaba.dubbo.common.Constants;
import com.alibaba.dubbo.common.utils.StringUtils;
import com.alibaba.dubbo.rpc.RpcContext;
import net.dubboclub.tracing.api.Annotation;
import net.dubboclub.tracing.api.BinaryAnnotation;
import net.dubboclub.tracing.api.Endpoint;
import net.dubboclub.tracing.api.Span;
import net.dubboclub.tracing.client.util.GUId;
import net.dubboclub.tracing.client.util.Sampler;
import org.springframework.beans.factory.InitializingBean;

/**
 * Created by Zetas on 2016/7/8.
 */
public class Tracer implements InitializingBean {

    private SyncTransfer syncTransfer;

    public void setSyncTransfer(SyncTransfer syncTransfer) {
        this.syncTransfer = syncTransfer;
    }

    public void afterPropertiesSet() throws Exception {
        syncTransfer.start();
    }

    public void beforeInvoke() {
        if (RpcContext.getContext().isConsumerSide()) {
            createConsumerSideTraceId();
            createConsumerSideSpan();
            addClientSendAnnotation();
        } else if (RpcContext.getContext().isProviderSide()) {
            createProvideSideTraceId();
            createProviderSideSpan();
            addServerReceiveAnnotation();
        }

        setAttachment();
    }

    public void afterInvoke() {
        if (RpcContext.getContext().isConsumerSide()) {
            addClientReceiveAnnotation();
        } else if (RpcContext.getContext().isProviderSide()) {
            addServerSendAnnotation();
        }
        send();
    }

    private void send() {
        Span span = ContextHolder.getSpan();
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
        if (Sampler.isSample(getServiceName())) {
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
                && StringUtils.isNotEmpty(spanId)
                && StringUtils.isNotEmpty(parentSpanId)) {
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
        if (StringUtils.isBlank(traceId)) {
            ContextHolder.setTraceId(GUId.singleton().nextId());
        }
        return ContextHolder.getTraceId();
    }

    private String createProvideSideTraceId() {
        RpcContext rpcContext = RpcContext.getContext();
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
