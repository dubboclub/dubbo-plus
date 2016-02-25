package net.dubboclub.restful;

import org.junit.Assert;
import org.junit.Test;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @date: 2016/2/25.
 * @author:bieber.
 * @project:dubbo-plus.
 * @package:net.dubboclub.restful.
 * @version:1.0.0
 * @fix:
 * @description: 描述功能
 */
public class TestArgsMatcher {

    @Test
    public void matchArgs(){
        Pattern argPattern = Pattern.compile("^(arg[1-9]{1}[0-9]{0,})$");
        Pattern argIndexPattern = Pattern.compile("[1-9]{1}[0-9]{0,}");
        String key = "arg122";
        Matcher matcher = argPattern.matcher(key);
        if(matcher.matches()){
            matcher = argIndexPattern.matcher(key);
            matcher.find();
            String index = matcher.group();
            System.out.println(index);
        }
        //Assert.assertTrue(matcher.matches());
    }

}
