package com.tencent.jungle.svrcore.Bootstrap;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.tencent.jungle.svrcore.comm.JungleServerCoreModule;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.ArrayList;
import java.util.List;

public class Bootstrap {
    static Logger logger = LoggerFactory.getLogger(Bootstrap.class);
    public static void main(String[] args) throws ConfigurationException {
//        System.out.println("hello,world");
        logger.info("hello,world");
        //加载配置
        Configuration configuration = new PropertiesConfiguration("jungle.properties");


        String value = configuration.getString("jungle.test");
        logger.info("value:{}", value);

        Injector injector = Guice.createInjector(loadModules());
        Configuration configs = injector.getInstance(Configuration.class);
        String value1 = configs.getString("jungle.test");
        logger.info("value1:{}", value1);
        new TcpSppServer(injector, configs);
        // workService --> 目前使用ThreadPoolWorkerService


        //监听端口

        //分发请求

        //根据命令字调用对应的processor去处理请求

        //回包
    }

    private static List<Module> loadModules() {
        List<Module> modules = new ArrayList<Module>();
        logger.info("begin load module junlge svr core.");
        modules.add(new JungleServerCoreModule());
        logger.info("load module junlge svr core suc.");
        return modules;
    }
}
