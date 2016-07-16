package net.dubboclub.tracing.client;

import com.alibaba.dubbo.rpc.Invocation;
import com.alibaba.dubbo.rpc.Invoker;
import net.dubboclub.tracing.api.Span;

/**
 * Created by Zetas on 2016/7/8.
 */
public class ContextHolder {

    private static ThreadLocal<String> localTraceId = new ThreadLocal<String>();

    private static ThreadLocal<Span> localSpan = new ThreadLocal<Span>();

    private static ThreadLocal<Invoker<?>> localInvoker = new ThreadLocal<Invoker<?>>();

    private static ThreadLocal<Invocation> localInvocation = new ThreadLocal<Invocation>();

    static void setTraceId(String traceId) {
        localTraceId.set(traceId);
    }
    public static String getTraceId() {
        return localTraceId.get();
    }
    static void removeTraceId() {
        localTraceId.remove();
    }

    static void setSpan(Span span) {
        ContextHolder.localSpan.set(span);
    }

    static Span getSpan() {
        return localSpan.get();
    }

    static void removeSpan() {
        localSpan.remove();
    }

    public static void setInvoker(Invoker<?> invoker) {
        ContextHolder.localInvoker.set(invoker);
    }

    static Invoker<?> getInvoker() {
        return localInvoker.get();
    }

    static void removeInvoker() {
        localInvoker.remove();
    }

    public static void setInvocation(Invocation invocation) {
        ContextHolder.localInvocation.set(invocation);
    }

    static Invocation getInvocation() {
        return localInvocation.get();
    }

    static void removeInvocation() {
        localInvocation.remove();
    }

    public static void removeAll() {
        removeTraceId();
        removeSpan();
        removeInvoker();
        removeInvocation();
    }


}
