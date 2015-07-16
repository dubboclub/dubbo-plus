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
        final MyFirstDubboService myFirstDubboService = (MyFirstDubboService) factory.getBean("myFirstDubboService");
        final User user=new User();
        user.setAge("23");
        user.setName("bieber");
        for(int i=0;i<80;i++){
            Thread t = new Thread(){
                @Override
                public void run() {
                    System.out.println(Thread.currentThread().getName());
                    long start = System.currentTimeMillis();
                  for(int j=0;j<1000;j++){
                      String result = myFirstDubboService.sayHello(user);
                      if(j%100==0){
                          System.out.println(Thread.currentThread().getName()+"  "+result);
                      }
                        try {
                            Thread.sleep(100);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                    System.out.println("Thread "+Thread.currentThread()+" cost time "+(System.currentTimeMillis()-start));
                }
            };
            t.start();
        }


    }
}
