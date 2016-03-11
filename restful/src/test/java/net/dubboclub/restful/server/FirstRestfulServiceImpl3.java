package net.dubboclub.restful.server;

import net.dubboclub.restful.api.FirstRestfulService;

/**
 * @date: 2016/2/25.
 * @author:bieber.
 * @project:dubbo-plus.
 * @package:net.dubboclub.restful.server.
 * @version:1.0.0
 * @fix:
 * @description: 描述功能
 */
public class FirstRestfulServiceImpl3 implements FirstRestfulService {
    @Override
    public String sayHello(String name) {
        return "Hello world3! "+name;
    }
}
