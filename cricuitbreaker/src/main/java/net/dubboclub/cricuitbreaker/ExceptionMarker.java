package net.dubboclub.cricuitbreaker;

import java.util.Date;

/**
 * 某个接口一次错误异常的标记
 */
class ExceptionMarker {

    private long breakTime;

    private long expireTime;

    private Date breakDate;
    
    private Throwable breakBy;
    
    private String caseByMessage;

    public String getCaseByMessage() {
        return caseByMessage;
    }

    ExceptionMarker(long breakTime, long expireTime,Throwable breakBy){
        this.breakTime = breakTime;
        this.expireTime = expireTime;
        this.breakDate = new Date(breakTime);
        this.breakBy=breakBy;
        this.caseByMessage = breakBy.getMessage();
    }

    @Override
    public String toString() {
        StringBuffer toString = new StringBuffer("breakTime=");
        toString.append(breakDate).append(",expireTime="+expireTime+"ms,remain "+getRemain()+"ms, break by "+breakBy.getMessage());
        return toString.toString();
    }
    
    public long getRemain(){
        return getExpireTime()-(System.currentTimeMillis()-breakTime);
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