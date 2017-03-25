package net.dubboclub.circuitbreaker;

import java.util.Iterator;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

class BreakCounterLoop extends Thread {

    private long intervalTime;

    private BlockingQueue<BreakCounter> queue = new LinkedBlockingQueue<BreakCounter>();

    private volatile  boolean started =false;

    private Object startLock = new Object();

    BreakCounterLoop(long intervalTime){
        this.setDaemon(true);
        this.intervalTime=intervalTime;
    }

    public void register(BreakCounter counter){
        queue.offer(counter);
        synchronized (startLock){
            if(!started){
                start();
                started=true;
            }
        }
    }

    @Override
    public void run() {
        for(;;){
            Iterator<BreakCounter> iterator = queue.iterator();
            while(iterator.hasNext()){
                BreakCounter counter = iterator.next();
                if(counter.isEnable()) {
                    counter.run();
                }else{
                    iterator.remove();
                }
            }
            try {
                Thread.sleep(intervalTime);
            } catch (InterruptedException e) {

            }
        }
    }
}