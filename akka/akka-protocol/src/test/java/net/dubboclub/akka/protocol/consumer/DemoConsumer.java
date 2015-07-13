package net.dubboclub.akka.protocol.consumer;

import net.dubboclub.akka.protocol.service.MyFirstDubboService;
import net.dubboclub.akka.protocol.service.User;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.io.IOException;

/**
 * Created by bieber on 2015/4/30.
 */
public class DemoConsumer {

    public static void main(String[] args) throws IOException, InterruptedException {
        System.setProperty("dubbo.properties.file","dubbo-consumer.properties");
        ApplicationContext context = new ClassPathXmlApplicationContext("classpath:/spring/dubbo-consumer.xml");
        BeanFactory factory = context.getAutowireCapableBeanFactory();
        MyFirstDubboService myFirstDubboService = (MyFirstDubboService) factory.getBean("myFirstDubboService");
        User user=new User();
        user.setAge("23");
        user.setName("bieber");
        while (true){
            System.out.println(myFirstDubboService.sayHello(user));
            Thread.sleep(100);
        }
    }
}
