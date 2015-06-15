package net.dubboclub.cricuitbreaker.filter;

import com.alibaba.dubbo.common.logger.Logger;
import com.alibaba.dubbo.common.logger.LoggerFactory;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 某个方法调用异常记录器
 */
class BreakCounter implements Runnable {

    private static final Logger logger = LoggerFactory.getLogger(BreakCounter.class);

    private List<ExceptionMarker> markerList = new ArrayList<ExceptionMarker>();
    //出现异常的次数
    private volatile AtomicLong exceptionCount = new AtomicLong(0);
    //服务降级的次数
    private volatile AtomicLong circuitBreakCount = new AtomicLong(0);

    private String invoker;

    private Object markersLock = new Object();

    private volatile boolean isEnable=true;
    

    BreakCounter(String invoker){
        this.invoker = invoker;
    }

    public void disable(){
        this.isEnable=false;
    }
    
    public boolean enable(){
        return isEnable;
    }

    public void addExceptionMarker(ExceptionMarker marker){
        synchronized (markersLock){
            exceptionCount.incrementAndGet();
            markerList.add(marker);
        }
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
        synchronized (markersLock){
            Iterator<ExceptionMarker> markerIterator = markerList.iterator();
            if(logger.isDebugEnabled()){
                logger.debug("check "+invoker+" break status,current break count ["+ getCurrentExceptionCount()+"]");
            }
            while(markerIterator.hasNext()){
                ExceptionMarker marker = markerIterator.next();
                if(logger.isDebugEnabled()){
                    logger.debug("check "+invoker+" break marker whether expired "+marker.toString());
                }
                if(marker.isExpire()){
                    markerIterator.remove();
                    decrementException();
                }
            }
            logger.debug("current break count ["+ getCurrentExceptionCount()+"]");
        }
    }
}