package com.tencent.dolphin.svrcore.ps;

import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Singleton;
import org.apache.commons.configuration.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Iterator;

/**
 * 从配置文件加载业务处理handler
 */
@Singleton
public class PropertiesProcessorService extends BaseProcessorService {
    static final Logger log = LoggerFactory.getLogger(PropertiesProcessorService.class);
    static final String K_APP_NAME = "server.appname";
    static final String K_EXIT_ON_INIT_FAIL = "server.exit_on_init_fail";


    @Inject
    public PropertiesProcessorService(Configuration config, Injector injector) {
        this(config, injector, config.getString(K_APP_NAME));
    }

    public PropertiesProcessorService(Configuration config, Injector injector, String appname) {
        boolean exit = config.getBoolean(K_EXIT_ON_INIT_FAIL, false);
        Configuration hdSubset = config.subset(appname + ".p");
        Iterator<String> keys = hdSubset.getKeys();
        int total = 0;
        int loaded = 0;
        long now = System.currentTimeMillis();
        StringBuilder sb = new StringBuilder("loaded processors ");
        while (keys.hasNext()) {
            total++;
            String key = keys.next();
            String value = hdSubset.getString(key);
            try {
                Class clazz = (Class) Class.forName(value);
                Processor processor = (Processor) injector.getInstance(clazz);
                if (processor instanceof BaseProcessor)
                    ((BaseProcessor) processor).setServiceCmd(key);
                super.processors.put(convertKey(key), processor);
                sb.append(key).append(':').append(value).append(',');
                loaded++;
            } catch (Exception ex) {
                if (exit)
                    throw new RuntimeException(ex);
                else
                    log.error("init failed, " + key + "=" + value, ex);
            }
        }
        sb.append('(').append(loaded).append('/').append(total).append(')');
        sb.append(", cost ").append(System.currentTimeMillis() - now).append(" ms");
        log.error(sb.toString());

    }

    protected Object convertKey(String key) {
        return key;
    }

}
