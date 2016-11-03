package net.dubboclub.tracing.core;

/**
 * NullSpan
 * Created by bieber.bibo on 16/11/2
 * 用于进行空得场景,避免出现空指针
 */

public class NullSpan extends Span {


    protected NullSpan(SpanType spanType, String spanName, String rpcId) {
        super(spanType, spanName, rpcId);
    }


}
