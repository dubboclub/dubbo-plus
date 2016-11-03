package net.dubboclub.tracing.core;

import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Span
 * Created by bieber.bibo on 16/11/2
 * 该类用来描述一次跨度信息,主要是消息的发送端和接收端进行描述
 */
public class Span {

    private static final String RPC_ID_SPLIT =".";

    /**
     * span的id
     */
    private String id;

    /**
     * 链路追踪id
     */
    private String traceId;

    /**
     * 类型,比如RPC,MQ等
     */
    private SpanBizType bizType;

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
    private SpanType spanType;

    /**
     * 下一个rpcid,默认是0
     */
    private transient int nextRPCId = 0;

    public static String START_RPC_ID="0";



    protected Span(SpanType spanType,String spanName,String rpcId) {
        this.traceId = TracingContext.getTraceId(true);
        this.id = TracingContext.generateUUID();
        this.spanType = spanType;
        this.bizType = SpanBizType.LOCAL;
        this.start = System.currentTimeMillis();
        this.name = spanName;
        this.rpcId = StringUtils.isNotBlank(rpcId)?rpcId:TracingContext.newRpcId();
        String parentId = TracingContext.getCurrentSpanId();
        if(StringUtils.isNotBlank(parentId)){
            this.parentId=parentId;
        }
        TracingContext.pushSpan(this);
    }

    protected void setRpcId(String rpcId){
        this.rpcId = rpcId;
    }


    protected String nextRPCId(){
        StringBuffer rpcId = new StringBuffer(getRpcId()).append(RPC_ID_SPLIT).append(nextRPCId);
        nextRPCId++;
        return rpcId.toString();
    }

    public String getRpcId() {
        return rpcId;
    }

    public String getApplication() {
        return application;
    }

    public void setApplication(String application) {
        this.application = application;
    }

    public long getEnd() {
        return end;
    }

    public void stop() {
        this.end = System.currentTimeMillis();
    }

    public long getStart() {
        return start;
    }



    public List<Annotation> getAnnotationList() {
        return annotationList;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getParentId() {
        return parentId;
    }

    public String getTraceId() {
        return traceId;
    }

    public SpanBizType getBizType() {
        return bizType;
    }

    public void setBizType(SpanBizType bizType) {
        this.bizType = bizType;
    }

    protected void addAnnotation(String key, String value){
        Annotation annotation = new Annotation(key,value);
        this.annotationList.add(annotation);
    }


    public SpanType getSpanType() {
        return spanType;
    }

    public enum SpanType{
        //发送端
        SENDER((short) 0),
        //接收端
        REVIVER((short) 1);

        private short type;

        SpanType(short type){
            this.type = type;
        }

        public short getType() {
            return type;
        }
    }

    public enum SpanBizType{
        RPC((short) 0),
        WEB((short) 1),
        LOCAL((short) 2),
        MQ((short) 3);
        private short type;

        SpanBizType(short type){
            this.type =type;
        }

        public short getType() {
            return type;
        }
    }

    @Override
    public String toString() {
        return "Span{" +
                "annotationList=" + annotationList +
                ", id='" + id + '\'' +
                ", traceId='" + traceId + '\'' +
                ", bizType='" + bizType + '\'' +
                ", name='" + name + '\'' +
                ", rpcId='" + rpcId + '\'' +
                ", parentId='" + parentId + '\'' +
                ", start=" + start +
                ", end=" + end +
                ", application='" + application + '\'' +
                ", spanType=" + spanType +
                '}';
    }
}
