package com.tencent.jungle.svrcore.comm;

import com.google.inject.ConfigurationException;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Singleton;
import com.tencent.jungle.svrcore.ws.WorkerService;
import com.tencent.jungle.svrcore.client.*;
import com.tencent.jungle.svrcore.qapp.QAppClientCodecService;
import com.tencent.jungle.svrcore.qapp.QAppReqPacket;
import com.tencent.jungle.svrcore.qapp.QAppRspPacket;
import com.tencent.jungle.svrcore.utils.NicUtil;
import com.tencent.jungle.svrcore.ws.ThreadPoolWorkerService;
import org.apache.commons.configuration.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 单例化server网络配置、client实例。
 * 此类会默认初始化qapp协议client，并且通过injector获取WorkerService实例。若用到该类需对WorkerService.class进行绑定
 */
@Singleton
public class ServerConfigs {
	static final Logger log = LoggerFactory.getLogger(ServerConfigs.class);

	public final String serviceName;
	public final String localAddr;
	public final int iLocalAddr;
	public final ClientIoService<QAppReqPacket, QAppRspPacket> qapp;

	@Inject
	public ServerConfigs(Configuration configs, Injector injector) {
		this.serviceName = configs.getString("server.appname", "");
		this.localAddr = NicUtil.resolveNicAddr(configs.getString("server.bind.nic", "eth1"));
		WorkerService ws = null;
		try{
			ws = injector.getInstance(WorkerService.class);
		}
		catch (ConfigurationException ex){
			ws = new ThreadPoolWorkerService();
			log.error("load WorkerService from guice failed, binding WorkerService.class to an instance is recommended. " +
					"using a new ThreadPoolWorkerService", ex);
		}
		this.qapp = new TcpClientIoService<QAppReqPacket, QAppRspPacket>()
				.setChannelsPerAddr(configs.getInt("server.client.tcp.channels", 2));
//		this.qapp = new KilimClientIoService<QAppReqPacket, QAppRspPacket>(
//				new TcpClientIoService<QAppReqPacket, QAppRspPacket>()
//						.setChannelsPerAddr(configs.getInt("server.client.tcp.channels", 2)));
		this.qapp.setCodecService(new QAppClientCodecService())
				.setRouterService(new PropertiesL5RouterService(configs, "spp"))
				.setTimeoutManager(injector.getInstance(TimeoutManager.class))
				.setWorkerService(ws)
				.start();
		int ip = 0;
		try{
			String[] parts = localAddr.split("\\.");
			ip = ((Integer.parseInt(parts[0])&0xFF) << 24) |
					((Integer.parseInt(parts[1])&0xFF) << 16) |
					((Integer.parseInt(parts[2])&0xFF) << 8) |
					(Integer.parseInt(parts[3])&0xFF);
		}
		catch (Exception ex){}
		this.iLocalAddr = ip;
//		this.routerHelper = injector.getInstance(WorkerRouterHelper.class);
//		L5MultiThreadClientDispatcher dispatcher = null;
//		if (configs.getBoolean("server.processor.yaaf.enabled", true)){
//			dispatcher = injector.getInstance(L5MultiThreadClientDispatcher.class);
//			dispatcher.getPublisher().doConfig(this.routerHelper.getWorkers());
//		}
//		this.dispatcher = dispatcher;
	}
}
