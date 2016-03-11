package net.dubboclub.restful.client;

import com.alibaba.dubbo.rpc.RpcContext;
import net.dubboclub.restful.api.FirstRestfulService;
import org.junit.Before;
import org.junit.Test;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * @date: 2016/2/26.
 * @author:bieber.
 * @project:dubbo-plus.
 * @package:net.dubboclub.restful.client.
 * @version:1.0.0
 * @fix:
 * @description: 描述功能
 */
public class ClientTest {

    ClassPathXmlApplicationContext classPathXmlApplicationContext;

    @Before
    public void startSpring(){
        classPathXmlApplicationContext = new ClassPathXmlApplicationContext("classpath*:client.xml");
    }

    @Test
    public void testInvoke(){
        RpcContext.getContext().setAttachment("Hello","Hello112");
        FirstRestfulService firstRestfulService = (FirstRestfulService) classPathXmlApplicationContext.getBean("firstRestful");
        System.out.println(firstRestfulService.sayHello("Bieber"));
    }
}
