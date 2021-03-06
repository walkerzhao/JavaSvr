package com.tencent.dolphin.svrcore.Bootstrap;

import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Singleton;
import com.google.protobuf.Message;
import com.tencent.dolphin.svrcore.codec.AdaptorPacket;
import com.tencent.dolphin.svrcore.io.ServerIoService;
import com.tencent.dolphin.svrcore.qapp.QAppServerCodecService;
import com.tencent.dolphin.svrcore.ws.WorkerService;
import com.tencent.dolphin.svrcore.io.TcpServerIoService;
import com.tencent.dolphin.svrcore.ps.NotFoundProcessorService;
import com.tencent.dolphin.svrcore.ps.PropertiesProcessorService;
import com.tencent.dolphin.svrcore.utils.U;
import io.netty.channel.Channel;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.lang.SystemUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TcpSppServer {
	Logger logger = LoggerFactory.getLogger(this.getClass());
	public TcpSppServer(Injector injector, Configuration configs) {
		NotFoundProcessorService notFoundAdapter = new NotFoundProcessorService(new PropertiesProcessorService(configs, injector), injector.getInstance(SppNotFoundProcessor.class));
		WorkerService ws = injector.getInstance(WorkerService.class);
		ServerIoService server = new TcpServerIoService(injector);
		String nic = configs.getString("server.qapp.bind.nic", "eth1");
		String svrIp = configs.getString("server.qapp.bind.ip", "10.19.85.54");
		int port = configs.getInt("server.qapp.bind.port", 22054);
		logger.info("nic:{} port:{}", nic, port);
		server.setCodecService(new QAppServerCodecService())
				.setWorkerService(ws)
				.setProcessorService(notFoundAdapter);
		if(SystemUtils.IS_OS_WINDOWS || SystemUtils.IS_OS_MAC_OSX) {
			server.setBindIpPort(svrIp, port);
		} else {
			server.setBindNic(nic, port);
		}

		server.start();
	}

	// 当cmd未定义时调用本接口
	@Singleton
	public static class SppNotFoundProcessor extends AdaptorBaseProcessor<Message> {

		private static final Logger LOG = LoggerFactory.getLogger(SppNotFoundProcessor.class);

		@Inject
		public SppNotFoundProcessor(Injector injector) {
			super(injector, null);
		}

		@Override
		protected AdaptorPacket service(AdaptorPacket pkg, Channel ioChannel, Message attach) throws Exception {
			LOG.error("not found processor {},{},{}", pkg.getIoCmd(), pkg.getReqUid(), pkg.getClientIp());
			return pkg.newResponsePacket(pkg, 0, "success where ever", U.EMPTY_BYTES);
		}

	}
}
