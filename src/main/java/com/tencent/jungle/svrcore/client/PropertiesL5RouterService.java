package com.tencent.jungle.svrcore.client;

import com.tencent.jungle.svrcore.packet.IoPacket;
import org.apache.commons.configuration.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class PropertiesL5RouterService implements RouterService {
	static final Logger log = LoggerFactory.getLogger(PropertiesL5RouterService.class);

	private final boolean isTestMode;
	private final Map<String, L5Config> data = new HashMap<String, L5Config>();

	/**
	 * 有效key：<br/>
	 * 1、[PREFIX].test.mode 是否开启静态路由表<br/>
	 * 2、[PREFIX].l5worker.* 所需连的命名服务<br/>
	 * 3、[PREFIX].l5.* 命名服务静态路由表配置<br/>
	 */
	public PropertiesL5RouterService(Configuration configs, String prefix) {
		this.isTestMode = configs.getBoolean(prefix+".test.mode", false);

		Configuration l5configs = configs.subset(prefix + ".l5worker");
		Iterator<String> it = l5configs.getKeys();
		while (it.hasNext()){
			String key = it.next();
			String value = l5configs.getString(key);
			String[] parts = value==null ? null : value.split(":");
			if (parts != null && parts.length == 2){
				RouterInfo testRouterInfo = null;
				String testIpPort = configs.getString(prefix + ".l5." + key);
				if (isTestMode && testIpPort!=null){
					String[] ipport = testIpPort.split(":");
					testRouterInfo = ipport!=null&&ipport.length==2 ?
							new RouterInfo(0, ipport[0], Integer.parseInt(ipport[1].trim()), null) : null;
				}
				data.put(key, new L5Config(
						Integer.parseInt(parts[1].trim()),
						Integer.parseInt(parts[0].trim()),
						key,
						testRouterInfo));
			}
			else
				log.error("config error "+prefix+"." + key + "=" + value);
		}
	}

	public PropertiesL5RouterService(Configuration configs) {
		this(configs, "l5");
	}

	@Override
	public List<RouterInfo> all(Object routerId) {
		L5Config c = data.get((String)routerId);
		if (c == null)
			return Collections.EMPTY_LIST;
		List<RouterInfo> ls = new LinkedList<RouterInfo>();
		if (isTestMode && c.testRouterInfo!=null)
			ls.add(c.testRouterInfo);
		else{
//			L5QOSPacket p = new L5QOSPacket(c.modid, c.cmdid);
//			p = L5API.getRouteTable(p);
//			long now = System.currentTimeMillis();
//			for (int i=0;i<p.hosts.length;i++)
//				ls.add(new RouterInfo(now, p.hosts[i], p.ports[i], p));
		}
		return ls;
	}

	@Override
	public RouterInfo next(Object routerId, IoPacket req) {

		String[] routeInfo = ((String) routerId).split(":");

		if (routeInfo.length == 2) {
			log.debug("rpc routInfo:{}", routeInfo);
			if (routeInfo[0].contains(".")) {//ip:port
				return new RouterInfo(0, routeInfo[0], Integer.parseInt(routeInfo[1].trim()), null);
			} else {
//				L5QOSPacket p = new L5QOSPacket(Integer.valueOf(routeInfo[0]), Integer.valueOf(routeInfo[1]));
//				p = L5API.getRoute(p, 0.1F);
//				long now = System.currentTimeMillis();
//				return new RouterInfo(now, p.ip, p.port, p);
			}
		} else {
			L5Config c = data.get((String)routerId);
			if (c == null)
				return null;
			if (isTestMode && c.testRouterInfo!=null)
				return c.testRouterInfo;
			else{
//				L5QOSPacket p = new L5QOSPacket(c.modid, c.cmdid);
//				p = L5API.getRoute(p, 0.1F);
//				long now = System.currentTimeMillis();
//				return new RouterInfo(now, p.ip, p.port, p);
				return null;
			}
		}
		return null;
	}

	@Override
	public void update(RouterInfo info, boolean succ) {
		if (info != null && info.attach != null) {
//			L5API.updateRoute((L5QOSPacket)info.attach, succ ? 1 : -1);
		}
	}

	static class L5Config{
		int cmdid;
		int modid;
		String name;
		RouterInfo testRouterInfo;
		public L5Config(int cmdid, int modid, String name, RouterInfo testRouterInfo) {
			this.cmdid = cmdid;
			this.modid = modid;
			this.name = name;
			this.testRouterInfo = testRouterInfo;
		}
	}
}
