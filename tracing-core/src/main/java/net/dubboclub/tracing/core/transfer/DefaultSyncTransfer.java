package net.dubboclub.tracing.core.transfer;

import com.alibaba.dubbo.common.logger.Logger;
import com.alibaba.dubbo.common.logger.LoggerFactory;
import net.dubboclub.tracing.core.ComponentLoader;
import net.dubboclub.tracing.core.SpanBean;
import net.dubboclub.tracing.core.collector.TracingCollector;
import net.dubboclub.tracing.core.config.Config;
import net.dubboclub.tracing.core.exception.TracingException;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

/**
 * Created by zetas on 2016/7/8.
 */
public class DefaultSyncTransfer implements SyncTransfer {

    private static Logger logger = LoggerFactory.getLogger(DefaultSyncTransfer.class);

    private static TracingCollector collector = ComponentLoader.getComponent(Config.getProperty(Config.COLLECTOR_KEY,Config.DEFAULT_COLLECTOR),TracingCollector.class);
    private volatile BlockingQueue<SpanBean> queue;
    private volatile TransferTask transferTask;


    private class TransferTask extends Thread {
        private List<SpanBean> cacheList;
        private int flushSizeInner;

        private TransferTask(int flushSize) {
            cacheList = new ArrayList<SpanBean>();
            flushSizeInner = flushSize;
            setName("Dst-span-transfer-task-thread");
        }

        @Override
        public void run() {
            while (!interrupted()) {
                try {
                    SpanBean first = queue.take();
                    cacheList.add(first);
                    queue.drainTo(cacheList, flushSizeInner);
                    if(cacheList.size()<=0){
                        continue;
                    }
                    collector.push(cacheList);
                } catch (Exception e) {
                    logger.error("Dst-span-transfer-task-thread occur an error", e);
                } finally {
                    cacheList.clear();//在发生异常时也要保证数据被清理，不然会内存溢出
                }
            }
        }
    }



    public DefaultSyncTransfer() {
        queue = new ArrayBlockingQueue<SpanBean>(Config.getProperty(Config.SYNC_QUEUE_SIZE,Config.DEFAULT_SYNC_QUEUE_SIZE));
        transferTask = new TransferTask(Config.getProperty(Config.SYNC_FLUSH_SIZE,Config.DEFAULT_FLUSH_SIZE));
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

    public void syncSend(SpanBean span) {
        try {
            queue.add(span);
        } catch (Exception e) {
            logger.error("span : ignore ..", e);
        }
    }
}
