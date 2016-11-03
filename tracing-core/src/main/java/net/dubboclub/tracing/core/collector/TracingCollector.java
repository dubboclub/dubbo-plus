package net.dubboclub.tracing.core.collector;


import net.dubboclub.tracing.core.SpanBean;

import java.util.List;

/**
 * Created by Zetas on 2016/7/7.
 */
public interface TracingCollector {
    public void push(List<SpanBean> spanList);
}
