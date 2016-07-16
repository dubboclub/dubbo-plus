package net.dubboclub.tracing.client.dubbo;

import com.alibaba.dubbo.common.Constants;
import com.alibaba.dubbo.common.extension.Activate;
import com.alibaba.dubbo.common.logger.Logger;
import com.alibaba.dubbo.common.logger.LoggerFactory;
import com.alibaba.dubbo.rpc.*;
import net.dubboclub.tracing.client.ContextHolder;
import net.dubboclub.tracing.client.Tracer;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * Created by Zetas on 2016/7/8.
 */
@Activate(group = {Constants.PROVIDER, Constants.CONSUMER})
public class TracingFilter implements Filter {

    private static Logger logger = LoggerFactory.getLogger(TracingFilter.class);

    private Tracer tracer;

    public void setTracer(Tracer tracer) {
        this.tracer = tracer;
    }

    public Result invoke(Invoker<?> invoker, Invocation invocation) throws RpcException {
        try {
            ContextHolder.setInvoker(invoker);
            tracer.beforeInvoke();
            Result result = invoker.invoke(invocation);
            if (result.hasException()) {
                tracer.addException(result.getException());
            }
            return result;
        } catch (RpcException e) {
            tracer.addException(e);
            throw e;
        } finally {
            tracer.afterInvoke();
            ContextHolder.removeAll();
        }
    }

    static {
        // FIXME: 2016/7/15 不用spring
        logger.info("Tracing filter is loading tracing-client-init file...");
        String resourceName = "classpath*:tracing-client-init.xml";
        final ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext(new String[]{
                resourceName
        });
        logger.info("Tracing config context is starting,config file path is:" + resourceName);
        context.start();
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                context.close();
            }
        });
    }
}
