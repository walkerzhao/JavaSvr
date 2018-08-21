package com.tencent.dolphin.svrcore.Bootstrap;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.tencent.dolphin.svrcore.comm.JungleServerCoreModule;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.ArrayList;
import java.util.List;

public class Bootstrap {
    static Logger logger = LoggerFactory.getLogger("Console");
    public static void main(String[] args) throws ConfigurationException {

        Injector injector = Guice.createInjector(loadModules());
        Configuration configs = injector.getInstance(Configuration.class);

        // workService --> 目前使用ThreadPoolWorkerService
        new TcpSppServer(injector, configs);

    }

    private static List<Module> loadModules() {
        List<Module> modules = new ArrayList<Module>();
        modules.add(new JungleServerCoreModule());
        return modules;
    }
}
