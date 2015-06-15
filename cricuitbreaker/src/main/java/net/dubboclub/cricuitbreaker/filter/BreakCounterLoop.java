package net.dubboclub.cricuitbreaker.filter;

import java.util.Iterator;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

class BreakCounterLoop extends Thread {

    private long intervalTime;

    private Queue<BreakCounter> queue = new ConcurrentLinkedQueue<BreakCounter>();

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
                if(counter.enable()) {
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