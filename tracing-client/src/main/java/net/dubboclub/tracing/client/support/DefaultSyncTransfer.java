package net.dubboclub.tracing.client.support;

import com.alibaba.dubbo.common.logger.Logger;
import com.alibaba.dubbo.common.logger.LoggerFactory;
import net.dubboclub.tracing.api.Span;
import net.dubboclub.tracing.api.TracingCollector;
import net.dubboclub.tracing.client.Configuration;
import net.dubboclub.tracing.client.SyncTransfer;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

/**
 * Created by zetas on 2016/7/8.
 */
public class DefaultSyncTransfer implements SyncTransfer {

    private static Logger logger = LoggerFactory.getLogger(DefaultSyncTransfer.class);

    private volatile TracingCollector collector;
    private volatile BlockingQueue<Span> queue;
    private volatile TransferTask transferTask;

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
                    collector.push(cacheList);
                } catch (InterruptedException e) {
                    logger.error("Dst-span-transfer-task-thread occur an error", e);
                }
            }
        }
    }

    public DefaultSyncTransfer(Configuration c) {
        int flushSize = c.getFlushSize() == null ? 1024 : c.getFlushSize();
        int queueSize = c.getQueueSize() == null ? 1024 : c.getQueueSize();
        queue = new ArrayBlockingQueue<Span>(queueSize);
        transferTask = new TransferTask(flushSize);
    }

    public void setCollector(TracingCollector collector) {
        this.collector = collector;
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
