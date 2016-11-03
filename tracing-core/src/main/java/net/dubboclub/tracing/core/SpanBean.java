package net.dubboclub.tracing.core;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * SpanBean
 * Created by bieber.bibo on 16/11/3
 */

public class SpanBean implements Serializable,Comparable<SpanBean> {

    /**
     * span的id
     */
    private String id;

    @Override
    public String toString() {
        return "SpanBean{" +
                "annotationList=" + annotationList +
                ", id='" + id + '\'' +
                ", traceId='" + traceId + '\'' +
                ", bizType=" + bizType +
                ", name='" + name + '\'' +
                ", parentId='" + parentId + '\'' +
                ", start=" + start +
                ", end=" + end +
                ", rpcId='" + rpcId + '\'' +
                ", application='" + application + '\'' +
                ", spanType=" + spanType +
                '}';
    }

    /**
     * 链路追踪id
     */
    private String traceId;

    /**
     * 类型,比如RPC,MQ等
     */
    private short bizType;

    /**
     * 名称,可以自定义
     */
    private String name;

    /**
     * 父的spanId
     */
    private String parentId;

    /**
     * 开始时间
     */
    private long start;

    /**
     * 结束时间
     */
    private long end;

    /**
     * 调用层级id 0.0.0.0
     */
    private String rpcId;

    /**
     * 应用名
     */
    private String application;

    /**
     * 该跨度上面的申明信息
     */
    private List<Annotation> annotationList = new ArrayList<Annotation>();

    /**
     * 用于描述span发送的端,是发送端还是接收端
     */
    private short spanType;

    public SpanBean() {
    }

    public SpanBean(Span span){
        setRpcId(span.getRpcId());
        setParentId(span.getParentId());
        setAnnotationList(span.getAnnotationList());
        setApplication(span.getApplication());
        setBizType(span.getBizType().getType());
        setEnd(span.getEnd());
        setId(span.getId());
        setName(span.getName());
        setSpanType(span.getSpanType().getType());
        setStart(span.getStart());
        setTraceId(span.getTraceId());
    }

    public List<Annotation> getAnnotationList() {
        return annotationList;
    }

    public void setAnnotationList(List<Annotation> annotationList) {
        this.annotationList = annotationList;
    }

    public String getApplication() {
        return application;
    }

    public void setApplication(String application) {
        this.application = application;
    }

    public short getBizType() {
        return bizType;
    }

    public void setBizType(short bizType) {
        this.bizType = bizType;
    }

    public long getEnd() {
        return end;
    }

    public void setEnd(long end) {
        this.end = end;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getParentId() {
        return parentId;
    }

    public void setParentId(String parentId) {
        this.parentId = parentId;
    }

    public String getRpcId() {
        return rpcId;
    }

    public void setRpcId(String rpcId) {
        this.rpcId = rpcId;
    }

    public short getSpanType() {
        return spanType;
    }

    public void setSpanType(short spanType) {
        this.spanType = spanType;
    }

    public long getStart() {
        return start;
    }

    public void setStart(long start) {
        this.start = start;
    }

    public String getTraceId() {
        return traceId;
    }

    public void setTraceId(String traceId) {
        this.traceId = traceId;
    }

    @Override
    public int compareTo(SpanBean o) {
        return this.rpcId.compareTo(o.rpcId);
    }
}
