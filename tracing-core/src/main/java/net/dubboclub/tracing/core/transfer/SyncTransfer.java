package net.dubboclub.tracing.core.transfer;


import net.dubboclub.tracing.core.SpanBean;

/**
 * Created by Zetas on 2016/7/8.
 */
public interface SyncTransfer {

    void start();
    void cancel();
    void syncSend(SpanBean span);

}
