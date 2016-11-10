package net.dubboclub.tracing.core;


import net.dubboclub.tracing.core.utils.NetUtils;
import org.apache.commons.lang.StringUtils;

import java.lang.management.ManagementFactory;
import java.util.concurrent.atomic.AtomicLong;

/**
 * TraceIdGenerator
 * Created by bieber.bibo on 16/11/9
 */

public class TraceIdGenerator {

    private static final byte[] LOCAL_IP= NetUtils.getLocalAddress().getAddress();

    private static final byte[] MAC_ADDRESS = NetUtils.getLocalHostMac();

    private static final AtomicLong CURRENT_SEQUENCE = new AtomicLong(0);

    private static final int MASK = 0xff;

    private static final String CURRENT_NOT_TRACEID_PREFIX;

    static {
        StringBuffer stringBuffer = new StringBuffer();
        for(byte b:MAC_ADDRESS){
            stringBuffer.append(fillHexString(b));
        }
        for(byte b:LOCAL_IP){
            stringBuffer.append(fillHexString(b));
        }
        CURRENT_NOT_TRACEID_PREFIX=stringBuffer.toString();
    }

    private static String fillHexString(byte b){
        String item = Integer.toHexString(b&MASK);
        if(item.length()==1){
            item="0"+item;
        }
        return item;
    }


    private static String getPID(){
        String name = ManagementFactory.getRuntimeMXBean().getName();
        return StringUtils.split(name,"@")[0];
    }

    /**
     * MAC+IP+TIME+SEQUENCE+PID
     * @return
     */
    public static String generateTraceId(){
        StringBuffer traceId = new StringBuffer(CURRENT_NOT_TRACEID_PREFIX);
        traceId.append(System.currentTimeMillis()).append(CURRENT_SEQUENCE.incrementAndGet()).append("P").append(getPID());
        return traceId.toString();
    }
}
