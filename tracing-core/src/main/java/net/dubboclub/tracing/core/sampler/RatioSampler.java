package net.dubboclub.tracing.core.sampler;

import net.dubboclub.tracing.core.TracingContext;
import net.dubboclub.tracing.core.config.Config;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.UUID;

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
        try {
            MessageDigest messageDigest = MessageDigest.getInstance("MD5");
            byte[] bytes = messageDigest.digest(traceId.getBytes());

        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        int traceHashCode= Math.abs(traceId.hashCode());
        int currentIndex = traceHashCode%100;
        return currentIndex<=ratio;
    }




}
