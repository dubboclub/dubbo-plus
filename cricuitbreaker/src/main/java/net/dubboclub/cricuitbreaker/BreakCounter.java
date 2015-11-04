package net.dubboclub.cricuitbreaker;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetAddress;
import java.util.Iterator;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 某个方法调用异常记录器
 */
class BreakCounter implements Runnable {

    private static final Logger statisticsLogger = LoggerFactory.getLogger("CIRCUITBREAKER-STATISTICS");

    private static final Logger logger = LoggerFactory.getLogger("CIRCUITBREAKER");

    private BlockingQueue<ExceptionMarker> markerList = new LinkedBlockingQueue<ExceptionMarker>();
    //出现异常的次数
    private volatile AtomicLong exceptionCount = new AtomicLong(0);
    //服务降级的次数
    private volatile AtomicLong circuitBreakCount = new AtomicLong(0);

    private String invoker;

    private volatile boolean isEnable=true;
    
    private volatile AtomicLong retryTimes = new AtomicLong(0);
    
    private static final InetAddress localAddress = Config.getLocalAddress();
    

    BreakCounter(String invoker){
        this.invoker = invoker;
    }

    public void disable(){
        this.isEnable=false;
    }
    
    public boolean isEnable(){
        return isEnable;
    }

    
    public void addExceptionMarker(ExceptionMarker marker){
        exceptionCount.incrementAndGet();
        markerList.offer(marker);
    }

    public void incrementRetryTimes(){
        retryTimes.incrementAndGet();
    }
    
    public long getCurrentRetryTimes(){
        return retryTimes.get();
    }
    
    public long decrementException(){
        return exceptionCount.decrementAndGet();
    }

    public long getCurrentExceptionCount(){
        return exceptionCount.get();
    }
    
    public long getCurrentBreakCount(){
        return circuitBreakCount.get();
    }
    
    
    public void incrementBreakCount(){
        circuitBreakCount.incrementAndGet();
    }
    
    public void run() {
        if(!isEnable){
            return;
        }
        Iterator<ExceptionMarker> markerIterator = markerList.iterator();
        statisticsLogger.info("[{}] checking [{}] break status,current exception times [{}] ,current break times [{}],retry times[{}]",localAddress,invoker,getCurrentExceptionCount(),getCurrentBreakCount(),getCurrentRetryTimes());
        while(markerIterator.hasNext()){
            ExceptionMarker marker = markerIterator.next();
            logger.info("[{}] checking [{}] break marker whether expired  break at [{}],remain  [{}],case[{}]" ,localAddress,invoker,marker.getBreakTime(),marker.getRemain(),marker.getCaseByMessage());
            if(marker.isExpire()){
                markerIterator.remove();
                decrementException();
            }
        }
        //statisticsLogger.info("[{}] current exception count [{}]",localAddress,getCurrentExceptionCount());
    }
}