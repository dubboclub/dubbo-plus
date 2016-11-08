package net.dubboclub.restful.filter;

import com.alibaba.dubbo.common.Constants;
import com.alibaba.dubbo.common.extension.Activate;
import com.alibaba.dubbo.rpc.*;

/**
 * @date: 2016/3/10.
 * @author:bieber.
 * @project:DubboM.
 * @package:net.dubboclub.restful.filter.
 * @version:1.0.0
 * @fix:
 * @description: 描述功能
 */
@Activate(group = Constants.PROVIDER, order = -100001)
public class RestfulFilter implements Filter {
    @Override
    public Result invoke(Invoker<?> invoker, Invocation invocation) throws RpcException {
        invocation.getAttachments().putAll(RpcContext.getContext().getAttachments());
        return invoker.invoke(invocation);
    }
}
