package net.dubboclub.restful.server;

import com.alibaba.dubbo.rpc.RpcContext;
import net.dubboclub.restful.api.FirstRestfulService;
import net.dubboclub.restful.api.FirstRestfulService1;

/**
 * @date: 2016/2/25.
 * @author:bieber.
 * @project:dubbo-plus.
 * @package:net.dubboclub.restful.server.
 * @version:1.0.0
 * @fix:
 * @description: 描述功能
 */
public class FirstRestfulServiceImpl1 implements FirstRestfulService1 {
    @Override
    public String sayHello(String name) {
        System.out.println(RpcContext.getContext().getAttachment("Hello"));
        return "Hello world11! "+name;
    }
}
