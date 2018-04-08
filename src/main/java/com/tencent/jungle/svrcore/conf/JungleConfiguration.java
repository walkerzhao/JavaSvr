package com.tencent.jungle.svrcore.conf;

import com.google.inject.Inject;
import com.google.inject.Injector;
import org.apache.commons.beanutils.MethodUtils;
import org.apache.commons.configuration.*;
import org.apache.commons.configuration.web.ServletContextConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Jungle的配置。配置从若干个地方获取，其优先次序为：
 */
public class JungleConfiguration extends CompositeConfiguration {

    final Logger logger = LoggerFactory.getLogger(JungleConfiguration.class);

    static boolean hasServletContext = false;
    private static Class<?> servletContextClass;

    static {
        try {
            servletContextClass = Class.forName("javax.servlet.ServletContext");
            hasServletContext = true;
            System.err.println("ServletContext class found");
        } catch (ClassNotFoundException e) {
            hasServletContext = false;
            System.err.println("ServletContext class not found");
        }

//        init();
    }

    Object servletContext = null;
    
    public final String appname;


    @Inject
    public JungleConfiguration(Injector injector) {
        super();
        setThrowExceptionOnMissing(false);
        if (hasServletContext) {
            try {
                servletContext = injector.getInstance(servletContextClass);
            } catch (com.google.inject.ConfigurationException e) {
                // no com.tencent.jungle.webdemo.servlet context found
                log("no ServletContext found. maybe not running in a servlet container.");
            }
        }
        appname = init();
    }

    String init() {
        log("create configuration");

        String homeDir = System.getenv("HOME");
        String appName = getAppName();
        log("Jungle app name is " + appName);


        // 环境变量最优先： 次序为<basename>前缀，jungle前缀，无前缀
        EnvironmentConfiguration envconf = new MyEnvironmentConfiguration();

        if (appName != null) {
            addConfiguration(new SubsetConfiguration(envconf, appName));
            log("add env config subset[" + appName + "]");
        }
        addConfiguration(new SubsetConfiguration(envconf, "jungle"));
        log("add env config subset[jungle]");
        addConfiguration(envconf);
        log("add env config");
        
        // 文件系统中，次序为
        //   ../conf/jungle.properties
        //   ${JUNGLE_SPEC_CONF}/jungle.properties
        //   $HOME/.<basename>.properties
        //   /etc/jungle/<basename>.properties
        //   $HOME/.jungle.properties
        //   /etc/jungle/jungle.properties
        if(!hasServletContext)
        {
           log("user.dir is"+System.getProperty("user.dir"));
           addConfigURL("../conf/jungle.properties");
        }
        //
        String specConf = System.getProperty("JUNGLE_SPEC_CONF");
        log("JUNGLE_SPEC_CONF path: " + specConf);
        addConfigURL(specConf + "/jungle.properties");
        
        if (appName != null) {
            addConfigURL(homeDir + "/." + appName + ".properties");
            addConfigURL("/etc/jungle/" + appName + ".properties");
        }
        addConfigURL(homeDir + "/.jungle.properties");
        addConfigURL("/etc/jungle/jungle.properties");

        // 系统属性
        addConfiguration(new SystemConfiguration());
        log("add system config");

        // web.xml 的初始化参数 (context-params)
        if (hasServletContext && servletContext != null) {
            addConfiguration(new ServletContextConfiguration((javax.servlet.ServletContext)servletContext));
            log("add config from servlet context params");
        }

        // 然后是 WEB-INF/ 下的文件
        if (appName != null)
            addConfigURL("webapp:" + appName + ".properties");
        addConfigURL("webapp:jungle.properties");

        // 最后是classpath下的文件
        if (appName != null)
            addConfigURL("classpath:" + appName + ".properties");
        addConfigURL("classpath:jungle.properties");

        if (hasServletContext && servletContext != null) {
            try {
                addProperty("jungle.webapp.name", MethodUtils.invokeExactMethod(servletContext, "getContextPath", new Object[]{}));
                addProperty("jungle.webapp.baseDir", MethodUtils.invokeExactMethod(servletContext, "getContextPath", "/"));
            } catch (Exception e) {
                // ignore
            }
        }
        return appName;
    }

    void log(String msg) {
        /*
        if (servletContext != null)
            servletContext.log("JungleConfiguration: " + msg);
        else
            System.out.println("JungleConfiguration: " + msg);
        */
        //System.out.println("JungleConfiguration: " + msg);
        logger.info(msg);
    }

    void addConfigURL(String spec) {
        URL url;
        try {
            url = newURL(spec);
        } catch (MalformedURLException e) {
            url = null;
        }
        if (url == null)
            return;

        try {
        	PropertiesConfiguration config = new PropertiesConfiguration(url);
    		config.setEncoding("utf8");
            this.addConfiguration(config);
            log("add url config " + url.toString());
        } catch (ConfigurationException e) {
            // ignore
        }
    }

    private URL newURL(String spec) throws MalformedURLException {
        if (spec.startsWith("webapp:")) {
            if (hasServletContext && servletContext != null)
                try {
                    return (URL) MethodUtils.invokeExactMethod(servletContext, "getResource", "WEB-INF/" + spec.substring(7));
                } catch (Exception e) {
                    // ignore
                }
            return null;
        }

        if (spec.startsWith("classpath:"))
            return getClass().getClassLoader().getResource(spec.substring(10));

        if (spec.startsWith("file:"))
            spec = spec.substring(5);

        File file = new File(spec);
        if (file.exists() && file.isFile() && file.canRead())
            return file.toURI().toURL();
        return null;
    }

    private String getAppName() {
        String appName = null;
        if (hasServletContext && servletContext != null)
            try {
                appName = (String) MethodUtils.invokeExactMethod(servletContext, "getServletContextName", new Object[]{});
            } catch (Exception e) {
                // ignore
            } // TODO use contextPath instead
        if (appName == null)
            appName = System.getenv("JUNGLE_APP_NAME");
        if (appName == null)
            appName = System.getProperty("JUNGLE_APP_NAME");
        if ("jungle".equals(appName)) // jungle名总是要加载
            appName = null;
        return appName;
    }

    class MyEnvironmentConfiguration extends EnvironmentConfiguration {

        private String normalize(String key) {
            return key.toUpperCase().replaceAll("[\\.\\-]", "_");
        }

        @Override
        public boolean containsKey(String key) {
            return super.containsKey(normalize(key));    //To change body of overridden methods use File | Settings | File Templates.
        }

        @Override
        public Object getProperty(String key) {
            return super.getProperty(normalize(key));    //To change body of overridden methods use File | Settings | File Templates.
        }

    }
}
