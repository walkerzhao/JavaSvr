package com.tencent.jungle.conf;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.tencent.jungle.svrcore.comm.JungleServerCoreModule;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by andy on 2018/8/21.
 */
public class ConfTest {

    @Test
    public void test() throws ConfigurationException {
        Configuration configuration = new PropertiesConfiguration("jungle.properties");
        String value = configuration.getString("jungle.test");
        System.out.println("hello,world." + value);
    }

    @Test
    public void testInjectConf() {
        Injector injector = Guice.createInjector(loadModules());
        Configuration configs = injector.getInstance(Configuration.class);
        String value1 = configs.getString("jungle.test");
        System.out.println(value1);
    }

    private static List<Module> loadModules() {
        List<Module> modules = new ArrayList<Module>();
        modules.add(new JungleServerCoreModule());
        return modules;
    }
}
