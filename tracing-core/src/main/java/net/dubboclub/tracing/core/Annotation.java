package net.dubboclub.tracing.core;

import java.io.Serializable;

/**
 * Annotation
 * Created by bieber.bibo on 16/11/2
 * 某个跨度里面的信息值
 */
public class Annotation implements Serializable{

    /**
     * key
     */
    private String key;

    /**
     * value
     */
    private String value;

    protected Annotation(String key, String value) {
        this.key = key;
        this.value = value;
    }

    public String getKey() {
        return key;
    }

    public String getValue() {
        return value;
    }

    @Override
    public String toString() {
        return "Annotation{" +
                "key='" + key + '\'' +
                ", value='" + value + '\'' +
                '}';
    }
}
