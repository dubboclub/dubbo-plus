package net.dubboclub.tracing.core;

import org.junit.Test;

/**
 * TracerTest
 * Created by bieber.bibo on 16/11/2
 */

public class TracerTest {

    @Test
    public void testTracing(){
        Tracer.startSpan(Span.SpanType.RESPONSE,"testMethod1",null);
        testMethod1();
        Tracer.stopSpan();
    }

    private void testMethod1(){
        Tracer.startSpan(Span.SpanType.RESPONSE,"testMethod2",null);
        testMethod2();
        Tracer.stopSpan();
        Tracer.startSpan(Span.SpanType.RESPONSE,"testMethod4",null);
        testMethod4();
        Tracer.stopSpan();
    }

    private void testMethod2(){
        Tracer.startSpan(Span.SpanType.RESPONSE,"testMethod3",null);
        testMethod3();
        Tracer.stopSpan();
    }

    protected void testMethod4(){
        Tracer.startSpan(Span.SpanType.RESPONSE,"testMethod2",null);
        testMethod2();
        Tracer.stopSpan();
        Tracer.startSpan(Span.SpanType.RESPONSE,"testMethod2",null);
        testMethod2();
        Tracer.stopSpan();
    }

    private void testMethod3(){

    }

}
