package net.dubboclub.tracing.client;


import net.dubboclub.tracing.api.Span;

/**
 * Created by Zetas on 2016/7/8.
 */
public interface SyncTransfer {

    public void start();
    public void cancel();
    public void syncSend(Span span);

}
