package net.dubboclub.tracing.core.sampler;

/**
 * FullSampler
 * Created by bieber.bibo on 16/11/3
 */

public class FullSampler implements Sampler {

    @Override
    public boolean next(String traceId) {
        return true;
    }

}
