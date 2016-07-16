package net.dubboclub.tracing.client;


import com.alibaba.dubbo.common.extension.SPI;
import net.dubboclub.tracing.api.Span;

/**
 * Created by Zetas on 2016/7/8.
 */
@SPI("default")
public interface SyncTransfer {

    public void start();
    public void cancel();
    public void syncSend(Span span);

}
