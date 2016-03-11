package net.dubboclub.restful;

import org.junit.Before;
import org.junit.Test;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.io.IOException;

/**
 * @date: 2016/2/25.
 * @author:bieber.
 * @project:dubbo-plus.
 * @package:net.dubboclub.restful.
 * @version:1.0.0
 * @fix:
 * @description: 描述功能
 */
public class StartServer {
    ClassPathXmlApplicationContext classPathXmlApplicationContext=null;
    @Before
    public void startSpring(){
        classPathXmlApplicationContext = new ClassPathXmlApplicationContext("classpath*:dubbo.xml");
    }

    @Test
    public void startServer() throws IOException {
        System.out.println("Started.....");
        System.in.read();
    }

}
