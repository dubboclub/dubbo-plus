package com.dubboclub.cache;

import com.alibaba.dubbo.common.utils.StringUtils;

/**
 * Created by bieber on 2015/5/28.
 */
public class TestProperty {
    
    public static void main(String[] args){
        String property = StringUtils.camelToSplitName("helloWorld", ".");
        System.out.println(property);
    }
}
