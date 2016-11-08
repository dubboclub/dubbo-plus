package net.dubboclub.tracing.client.support;

import com.alibaba.dubbo.common.Constants;
import com.alibaba.dubbo.common.URL;
import com.alibaba.dubbo.common.extension.ExtensionFactory;
import com.alibaba.dubbo.common.extension.ExtensionLoader;
import com.alibaba.dubbo.common.logger.Logger;
import com.alibaba.dubbo.common.logger.LoggerFactory;
import com.alibaba.dubbo.common.utils.ConfigUtils;
import com.alibaba.dubbo.config.ApplicationConfig;
import com.alibaba.dubbo.config.ConsumerConfig;
import com.alibaba.dubbo.config.ReferenceConfig;
import com.alibaba.dubbo.rpc.Protocol;
import net.dubboclub.tracing.api.Span;
import net.dubboclub.tracing.api.TracingCollector;
import net.dubboclub.tracing.client.Configuration;
import net.dubboclub.tracing.client.DstConstants;
import net.dubboclub.tracing.client.SyncTransfer;
import net.dubboclub.tracing.client.TracingCollectorFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

/**
 * Created by zetas on 2016/7/8.
 */
public class DefaultSyncTransfer implements SyncTransfer {

    private static Logger logger = LoggerFactory.getLogger(DefaultSyncTransfer.class);

    private Protocol protocol;

    private volatile TracingCollector collector;
    private volatile BlockingQueue<Span> queue;
    private volatile TransferTask transferTask;

    private volatile boolean inited=false;

    public void setProtocol(Protocol protocol) {
        this.protocol = protocol;
    }

    private class TransferTask extends Thread {
        private List<Span> cacheList;
        private int flushSizeInner;

        private TransferTask(int flushSize) {
            cacheList = new ArrayList<Span>();
            flushSizeInner = flushSize;
            setName("Dst-span-transfer-task-thread");
        }

        @Override
        public void run() {
            while (!interrupted()) {
                try {
                    Span first = queue.take();
                    cacheList.add(first);
                    queue.drainTo(cacheList, flushSizeInner);
                    if(cacheList.size()<=0){
                        continue;
                    }
                    if(!inited&&collector==null){
                        TracingCollectorFactory tracingCollectorFactory = ExtensionLoader
                                .getExtensionLoader(TracingCollectorFactory.class)
                                .getExtension(ConfigUtils.getProperty(DstConstants.TRACING_COLLECTOR
                                        ,DstConstants.DEFAULT_COLLECTOR_TYPE));
                        collector =tracingCollectorFactory.getTracingCollector();
                        inited=true;
                    }
                    collector.push(cacheList);
                    cacheList.clear();
                } catch (InterruptedException e) {
                    logger.error("Dst-span-transfer-task-thread occur an error", e);
                }
            }
        }
    }



    public DefaultSyncTransfer() {
        queue = new ArrayBlockingQueue<Span>(Integer.parseInt(ConfigUtils.getProperty(DstConstants.FLUSH_SIZE_KEY,DstConstants.DEFAULT_FLUSH_SIZE)));
        transferTask = new TransferTask(Integer.parseInt(ConfigUtils.getProperty(DstConstants.QUEUE_SIZE_KEY,DstConstants.DEFAULT_BUFFER_QUEUE_SIZE)));
    }


    public void start() {
        transferTask.start();
        Runtime.getRuntime().addShutdownHook(new Thread() {
            public void run() {
                cancel();
            }
        });
    }

    public void cancel() {
        transferTask.interrupt();
    }

    public void syncSend(Span span) {
        try {
            queue.add(span);
        } catch (Exception e) {
            logger.error("span : ignore ..", e);
        }
    }
}
