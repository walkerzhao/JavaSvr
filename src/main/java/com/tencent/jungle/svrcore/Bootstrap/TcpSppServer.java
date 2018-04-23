package com.tencent.jungle.svrcore.Bootstrap;

import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Singleton;
import com.google.protobuf.Message;
import com.tencent.jungle.svrcore.codec.AdaptorPacket;
import com.tencent.jungle.svrcore.io.ServerIoService;
import com.tencent.jungle.svrcore.ps.BaseProcessor;
import com.tencent.jungle.svrcore.qapp.QAppServerCodecService;
import com.tencent.jungle.svrcore.ws.WorkerService;
import com.tencent.jungle.svrcore.io.TcpServerIoService;
import com.tencent.jungle.svrcore.ps.NotFoundProcessorService;
import com.tencent.jungle.svrcore.ps.PropertiesProcessorService;
import com.tencent.jungle.svrcore.utils.U;
import io.netty.channel.Channel;
import org.apache.commons.configuration.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TcpSppServer {
	public TcpSppServer(Injector injector, Configuration configs) {
		NotFoundProcessorService notFoundAdapter = new NotFoundProcessorService(new PropertiesProcessorService(configs, injector), injector.getInstance(SppNotFoundProcessor.class));
		WorkerService ws = injector.getInstance(WorkerService.class);
		ServerIoService server = new TcpServerIoService(injector);
		String nic = configs.getString("server.qapp.bind.nic", "eth1");
		int port = configs.getInt("server.qapp.bind.port", 22054);
		server.setCodecService(new QAppServerCodecService()).setWorkerService(ws).setProcessorService(notFoundAdapter).setBindNic(nic, port);
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
