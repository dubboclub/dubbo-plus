package net.dubboclub.tracing.core.sampler;

import net.dubboclub.tracing.core.config.Config;

/**
 * RatioSampler
 * Created by bieber.bibo on 16/11/3
 */

public class RatioSampler implements Sampler {

    //百分比
    private int ratio;

    public RatioSampler(){
        ratio = Config.getProperty(Config.TRACING_SAMPLER_RATIO,Config.DEFAULT_SAMPLER_RATIO);
    }

    @Override
    public boolean next(String traceId) {
        int traceHashCode= Math.abs(traceId.hashCode());
        int currentIndex = traceHashCode%100;
        return currentIndex<=ratio;
    }

}
