package com.tencent.dolphin.svrcore.client;

import com.tencent.dolphin.svrcore.packet.IoPacket;
import com.tencent.dolphin.svrcore.client.RouterService.RouterInfo;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class StaticRouterServiceBuilder {
	Map<String, List<RouterInfo>> data = new HashMap<String, List<RouterInfo>>();
	public StaticRouterServiceBuilder add(String routerId, String targetHost, int targetPort){
		List<RouterInfo> info = data.get(routerId);
		if (info == null){
			info = new ArrayList<RouterInfo>();
			data.put(routerId, info);
		}
		info.add(new RouterInfo(0, targetHost, targetPort, null));
		return this;
	}

	public RouterService build(){
		StaticRouterService s = new StaticRouterService();
		for (String routerId : data.keySet()){
			List<RouterInfo> info = data.get(routerId);
			H holder = new H(info.toArray(new RouterInfo[0]));
			s.data.put(routerId, holder);
		}
		data = null;
		return s;
	}

	private static class StaticRouterService implements RouterService{
		Map<String, H> data = new HashMap<String, H>();

		@Override
		public List<RouterInfo> all(Object routerId) {
			H holder = data.get(routerId);
			return holder==null ? Collections.EMPTY_LIST : Arrays.asList(holder.routers);
		}

		@Override
		public RouterInfo next(Object routerId, IoPacket req) {
			H holder = data.get(routerId);
			return holder==null ? null : holder.routers[holder.c.getAndIncrement()%holder.routers.length];
		}

		@Override
		public void update(RouterInfo info, boolean succ) {
			
		}
		
	}
	
	static class H {
		final AtomicInteger c = new AtomicInteger(0);
		final RouterInfo[] routers;
		public H(RouterInfo[] routers) {
			this.routers = routers;
		}
	}
}
