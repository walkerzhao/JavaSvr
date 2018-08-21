package com.tencent.dolphin.svrcore.client;

import com.tencent.dolphin.svrcore.packet.IoPacket;

import java.net.InetSocketAddress;
import java.util.List;

/**
 * {@link ClientIoService}的路由服务
 * @see PropertiesL5RouterService
 * @see StaticRouterServiceBuilder
 */
public interface RouterService {
	public static class RouterInfo{
		public final long start;
		public final String ip;
		public final int port;
		public final Object attach;
		InetSocketAddress addr = null;
		public RouterInfo(long start, String ip, int port, Object attach) {
			this.start = start;
			this.ip = ip;
			this.port = port;
			this.attach = attach;
		}
		
		public InetSocketAddress getSocketAddr(){
			if (addr == null)
				addr = new InetSocketAddress(ip, port);
			return addr;
		}
	}
	
	List<RouterInfo> all(Object routerId);
	
	/**
	 * 
	 * @param routerId
	 * @param req 需要发送的IO包。用作更细粒度的路由
	 * @return
	 */
	RouterInfo next(Object routerId, IoPacket req);
	
	void update(RouterInfo info, boolean succ);
}
