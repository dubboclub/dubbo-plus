package net.dubboclub.tracing.api;

import java.util.List;

/**
 * Created by Zetas on 2016/7/7.
 */
public interface TracingCollector {
    public void push(List<Span> spanList);
}
