package net.dubboclub.catmonitor;

import com.alibaba.dubbo.common.Constants;
import com.alibaba.dubbo.common.URL;
import com.alibaba.dubbo.common.extension.Activate;
import com.alibaba.dubbo.remoting.RemotingException;
import com.alibaba.dubbo.remoting.TimeoutException;
import com.alibaba.dubbo.rpc.*;
import com.dianping.cat.Cat;
import com.dianping.cat.message.*;
import com.dianping.cat.message.internal.AbstractMessage;
import com.dianping.cat.message.internal.DefaultForkedTransaction;
import com.dianping.cat.message.spi.MessageTree;
import com.pinganfu.mobile.dubboplus.cat.constants.CatConstants;
import org.apache.commons.lang.StringUtils;

import java.util.Map;

/**
 * Created by bieber on 2015/11/4.
 */
@Activate(group = {Constants.PROVIDER, Constants.CONSUMER},order = -9000)
public class CatTransaction implements Filter {

    private final static String DUBBO_BIZ_ERROR="DUBBO_BIZ_ERROR";

    private final static String DUBBO_TIMEOUT_ERROR="DUBBO_TIMEOUT_ERROR";
    
    private final static String DUBBO_REMOTING_ERROR="DUBBO_REMOTING_ERROR";

    private static final String PINGANFU_PACKAGE_PREFIX="com.pinganfu";

    @Override
    public Result invoke(Invoker<?> invoker, Invocation invocation) throws RpcException {
        long start = System.currentTimeMillis();
        if(!DubboCat.isEnable()){
            Result result =  invoker.invoke(invocation);
            return result;
        }
        boolean hasContext = false;
        if(Cat.getManager().hasContext()){
            hasContext=true;
        }
        URL url = invoker.getUrl();
        String sideKey = url.getParameter(Constants.SIDE_KEY);
        String loggerName = invoker.getInterface().getSimpleName()+"."+invocation.getMethodName();
        String type = CatConstants.CROSS_CONSUMER;
        if(Constants.PROVIDER_SIDE.equals(sideKey)){
            type= CatConstants.CROSS_SERVER;
        }
        Transaction transaction =null;

        if(hasContext){
            transaction = Cat.newTaggedTransaction(type,loggerName,"DUBBO-CROSS");
        }else{
            transaction = Cat.newTransaction(type,loggerName);
        }
        Result result=null;
        try{
            if(Constants.CONSUMER_SIDE.equals(sideKey)){
                hasForkedMessage();
                generateFork();
                createConsumerCross(url,transaction);
            }else{
                createProviderCross(url,transaction);
            }
            result =  invoker.invoke(invocation);

            if(result.hasException()){
                //给调用接口出现异常进行打点
                Throwable throwable = result.getException();
                Event event = null;
                if(RpcException.class==throwable.getClass()){
                    Throwable caseBy = throwable.getCause();
                    if(caseBy!=null&&caseBy.getClass()==TimeoutException.class){
                        event = Cat.newEvent(DUBBO_TIMEOUT_ERROR,loggerName); 
                    }else{
                        event = Cat.newEvent(DUBBO_REMOTING_ERROR,loggerName);
                    }
                }else if(RemotingException.class.isAssignableFrom(throwable.getClass())){
                    event = Cat.newEvent(DUBBO_REMOTING_ERROR,loggerName);
                }else{
                    event = Cat.newEvent(DUBBO_BIZ_ERROR,loggerName);
                }
                event.setStatus(result.getException());
                completeEvent(event);
                transaction.addChild(event);
                transaction.setStatus(result.getException().getClass().getSimpleName());
            }else{
                transaction.setStatus(Message.SUCCESS);
            }
            return result;
        }catch (RuntimeException e){
            Event event = null;
            if(RpcException.class==e.getClass()){
                Throwable caseBy = e.getCause();
                if(caseBy!=null&&caseBy.getClass()==TimeoutException.class){
                    event = Cat.newEvent(DUBBO_TIMEOUT_ERROR,loggerName);
                }else{
                    event = Cat.newEvent(DUBBO_REMOTING_ERROR,loggerName);
                }
            }else{
                event = Cat.newEvent(DUBBO_BIZ_ERROR,loggerName);
            }
            event.setStatus(e);
            completeEvent(event);
            transaction.addChild(event);
            transaction.setStatus(e.getClass().getSimpleName());
            if(result==null){
                throw e;
            }else{
                return result;
            }
        }finally {
            transaction.complete();
            System.out.println(System.currentTimeMillis()-start+"ms");
        }
    }

    private void hasForkedMessage(){
        Map<String,String> attachments = RpcContext.getContext().getAttachments();
        MessageTree messageTree = Cat.getManager().getThreadLocalMessageTree();
        if (attachments.containsKey(CatConstants.FORK_MESSAGE_ID)&&messageTree != null) {
            messageTree.setMessageId(attachments.get(CatConstants.FORK_MESSAGE_ID));
            //messageTree.setRootMessageId(attachments.get(CatConstants.FORK_ROOT_MESSAGE_ID) == null ? attachments.get(CatConstants.FORK_PARENT_MESSAGE_ID) : attachments.get(CatConstants.FORK_ROOT_MESSAGE_ID));
            messageTree.setParentMessageId(attachments.get(CatConstants.FORK_PARENT_MESSAGE_ID));
        }
    }

    private void generateFork(){
        MessageTree messageTree =  Cat.getManager().getThreadLocalMessageTree();
        DefaultForkedTransaction forkedTransaction = (DefaultForkedTransaction) Cat.newForkedTransaction("show", "");
        if(messageTree!=null){
            RpcContext.getContext().setAttachment(CatConstants.FORK_MESSAGE_ID,forkedTransaction.getForkedMessageId());
            RpcContext.getContext().setAttachment(CatConstants.FORK_PARENT_MESSAGE_ID,messageTree.getMessageId());
            RpcContext.getContext().setAttachment(CatConstants.FORK_ROOT_MESSAGE_ID,messageTree.getRootMessageId());
        }
    }

    private String getProviderAppName(URL url){
        String appName = url.getParameter(CatConstants.PROVIDER_APPLICATION_NAME);
        if(StringUtils.isEmpty(appName)){
            String interfaceName  = url.getParameter(Constants.INTERFACE_KEY);
            if(interfaceName.startsWith(PINGANFU_PACKAGE_PREFIX)){
                appName = StringUtils.split(interfaceName,".")[2];
            }else{
                appName = interfaceName.substring(0,interfaceName.lastIndexOf('.'));
            }
        }
        return appName;
    }

    private void createConsumerCross(URL url,Transaction transaction){
        Event crossAppEvent =   Cat.newEvent(CatConstants.CONSUMER_CALL_APP,getProviderAppName(url));
        Event crossServerEvent =   Cat.newEvent(CatConstants.CONSUMER_CALL_SERVER,url.getHost());
        Event crossPortEvent =   Cat.newEvent(CatConstants.CONSUMER_CALL_PORT,url.getPort()+"");
        crossAppEvent.setStatus(Event.SUCCESS);
        crossServerEvent.setStatus(Event.SUCCESS);
        crossPortEvent.setStatus(Event.SUCCESS);
        completeEvent(crossAppEvent);
        completeEvent(crossPortEvent);
        completeEvent(crossServerEvent);
        transaction.addChild(crossAppEvent);
        transaction.addChild(crossPortEvent);
        transaction.addChild(crossServerEvent);
    }

    private void completeEvent(Event event){
        AbstractMessage message = (AbstractMessage) event;
        message.setCompleted(true);
    }

    private void createProviderCross(URL url,Transaction transaction){
        String consumerAppName = RpcContext.getContext().getAttachment(Constants.APPLICATION_KEY);
        if(StringUtils.isEmpty(consumerAppName)){
            consumerAppName= RpcContext.getContext().getRemoteHost()+":"+ RpcContext.getContext().getRemotePort();
        }
        Event crossAppEvent = Cat.newEvent(CatConstants.PROVIDER_CALL_APP,consumerAppName);
        Event crossServerEvent = Cat.newEvent(CatConstants.PROVIDER_CALL_SERVER,url.getHost());
        crossAppEvent.setStatus(Event.SUCCESS);
        crossServerEvent.setStatus(Event.SUCCESS);
        completeEvent(crossAppEvent);
        completeEvent(crossServerEvent);
        transaction.addChild(crossAppEvent);
        transaction.addChild(crossServerEvent);
    }
}
