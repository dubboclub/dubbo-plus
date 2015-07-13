package net.dubboclub.akka.protocol.provider;


import net.dubboclub.akka.protocol.service.MyFirstDubboService;
import net.dubboclub.akka.protocol.service.User;

/**
 * Created by bieber on 2015/4/30.
 */
public class MyFirstDubboServiceImpl implements MyFirstDubboService {
    @Override
    public String sayHello(User user) {
        return "hello "+user.getName()+" , your age is "+user.getAge();
    }
}
