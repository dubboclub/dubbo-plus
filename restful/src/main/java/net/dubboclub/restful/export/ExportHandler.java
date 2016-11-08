package net.dubboclub.restful.export;

/**
 * @date: 2016/2/22.
 * @author:bieber.
 * @project:dubbo-plus.
 * @package:net.dubboclub.restful.export.
 * @version:1.0.0
 * @fix:
 * @description: 描述功能
 */
public class ExportHandler implements Runnable {

    private String serviceKey;

    public ExportHandler(String serviceKey) {
        this.serviceKey = serviceKey;
    }

    @Override
    public void run() {

    }
}
