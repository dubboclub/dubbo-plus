package net.dubboclub.akka.protocol.service;

import java.io.Serializable;

/**
 * Created by bieber on 2015/4/30.
 */
public class User implements Serializable{

    private String name;

    private String age;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAge() {
        return age;
    }

    public void setAge(String age) {
        this.age = age;
    }
}
