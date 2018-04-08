package com.tencent.jungle.svrcore.Bootstrap;

import com.tencent.jungle.svrcore.WorkerService;
import com.tencent.jungle.svrcore.client.DefaultTimeoutManager;
import com.tencent.jungle.svrcore.client.TimeoutManager;
import com.tencent.jungle.svrcore.ws.ThreadPoolWorkerService;
import org.apache.commons.configuration.PropertiesConfiguration;

import java.io.File;

public class Bootstrap {
    public static void main(String[] args) {
        System.out.println("hello,world");
        //加载配置
        PropertiesConfiguration tmp = new PropertiesConfiguration();
        File cFile = new File(Bootstrap.class.getClassLoader().getResource("jungle.properties").getFile());
        if (cFile.exists() && cFile.isFile()){
            try {
                tmp.load(cFile);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        String value = tmp.getString("jungle.test");
        System.out.println(value);

        // workService --> 目前使用ThreadPoolWorkerService
        //后续引入kilim的线程池
        Class<? extends WorkerService> workerServiceClazz = ThreadPoolWorkerService.class;

        //使用默认的超时管理器--client 发包
        Class<? extends TimeoutManager> clazz = DefaultTimeoutManager.class;

        //监听端口

        //分发请求

        //根据命令字调用对应的processor去处理请求

        //回包
    }
}
