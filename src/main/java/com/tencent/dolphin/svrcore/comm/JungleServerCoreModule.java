package com.tencent.dolphin.svrcore.comm;

import com.google.inject.AbstractModule;
import com.google.inject.Scopes;
import com.tencent.dolphin.svrcore.conf.JungleConfiguration;
import com.tencent.dolphin.svrcore.ws.WorkerService;
import com.tencent.dolphin.svrcore.client.DefaultTimeoutManager;
import com.tencent.dolphin.svrcore.client.TimeoutManager;
import com.tencent.dolphin.svrcore.ws.ThreadPoolWorkerService;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.PropertiesConfiguration;
import java.io.File;

/**
 * 使用到BaseProcessor/QAppBaseProcessor/WnsBaseProcessor/SsoBaseProcessor时，需加载本module
 */
public class JungleServerCoreModule extends AbstractModule {

	@Override
	protected void configure() {
		PropertiesConfiguration tmp = new PropertiesConfiguration();
		File cFile = new File("../conf/dolphin.properties");
		if (cFile.exists() && cFile.isFile()){
			try {
				tmp.load(cFile);
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
		
		Class<? extends WorkerService> workerServiceClazz = null;

		workerServiceClazz = ThreadPoolWorkerService.class;

		System.err.println("using WorkerService " + workerServiceClazz.getName());
		bind(WorkerService.class).to(workerServiceClazz);   //绑定工作线程池
		
		Class<? extends TimeoutManager> clazz = DefaultTimeoutManager.class;
		try{
			String sClazz = tmp.getString("server.client.timeout_manager");
			if (sClazz != null && sClazz.length() > 0){
				Class<?> nClazz = Class.forName(sClazz);
				if (!TimeoutManager.class.isAssignableFrom(nClazz))
					throw new IllegalStateException(sClazz + " is not a sub class of TimeoutManager");
				clazz = (Class<? extends TimeoutManager>)nClazz;
			}
		}
		catch(Exception ex){
			throw new RuntimeException(ex);
		}
		System.err.println("using TimeoutManager " + clazz.getName());
		bind(TimeoutManager.class).to(clazz);   //绑定超时管理

		bind(Configuration.class).to(JungleConfiguration.class).in(
				Scopes.SINGLETON);    //配置文件
	}

}
