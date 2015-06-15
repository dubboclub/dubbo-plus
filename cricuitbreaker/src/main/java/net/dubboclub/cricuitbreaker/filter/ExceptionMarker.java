package net.dubboclub.cricuitbreaker.filter;

import java.util.Date;

/**
 * 某个接口一次错误异常的标记
 */
class ExceptionMarker {

    private long breakTime;

    private long expireTime;

    private Date breakDate;
    
    private Throwable breakBy;
    
    

    ExceptionMarker(long breakTime, long expireTime,Throwable breakBy){
        this.breakTime = breakTime;
        this.expireTime = expireTime;
        this.breakDate = new Date(breakTime);
        this.breakBy=breakBy;
    }

    @Override
    public String toString() {
        StringBuffer toString = new StringBuffer("breakTime=");
        toString.append(breakDate).append(",expireTime="+expireTime+"ms,remain "+(System.currentTimeMillis()-breakTime)+"ms, break by "+breakBy.getMessage());
        return toString.toString();
    }

    public long getBreakTime() {
        return breakTime;
    }


    public long getExpireTime() {
        return expireTime;
    }

    public boolean isExpire(){
        return (System.currentTimeMillis()-getBreakTime())>=getExpireTime();
    }
}