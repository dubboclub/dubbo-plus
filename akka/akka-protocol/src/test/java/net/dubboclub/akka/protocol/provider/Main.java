package net.dubboclub.akka.protocol.provider;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.io.IOException;

/**
 * Created by bieber on 2015/5/27.
 */
public class Main {

    public static void main(String[] args) throws IOException {
        System.setProperty("dubbo.properties.file","dubbo-provider.properties");
        ApplicationContext context = new ClassPathXmlApplicationContext(new String[]{"classpath:/spring/dubbo-provider.xml","classpath:/spring/applicationContext.xml"});
        System.out.println("Service started....." + context);
        System.in.read();
    }
}
