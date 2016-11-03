package net.dubboclub.tracing.core.sampler;

/**
 * Created by 玄伯 on 16/11/2.
 * 链路的采样器
 */
public interface Sampler {
    /**
     * 判断是否进行采样
     * @param traceId
     * @return
     */
    boolean next(String traceId);


}
