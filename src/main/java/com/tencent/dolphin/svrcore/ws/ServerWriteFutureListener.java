package com.tencent.dolphin.svrcore.ws;

import com.tencent.dolphin.svrcore.utils.MonitorUtils;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ServerWriteFutureListener implements ChannelFutureListener {
	static final Logger log = LoggerFactory.getLogger(ServerWriteFutureListener.class);

	@Override
	public void operationComplete(ChannelFuture future) throws Exception {
		if (!future.isSuccess()){
			MonitorUtils.monitor(2018808); // Server网络写失败
			if (future.cause() != null)
				log.error("server channel write failed " + future.channel().remoteAddress(), future.cause());
		}
	}
	
	public static final ServerWriteFutureListener INSTANCE = new ServerWriteFutureListener();
}
