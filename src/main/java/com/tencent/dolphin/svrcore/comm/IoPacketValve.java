package com.tencent.dolphin.svrcore.comm;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.tencent.dolphin.svrcore.packet.IoPacket;
import com.tencent.dolphin.svrcore.ps.Processor;
import com.tencent.dolphin.svrcore.utils.MonitorUtils;
import io.netty.channel.Channel;
import org.apache.commons.configuration.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.atomic.AtomicInteger;

@Singleton
public class IoPacketValve {
	static final Logger log = LoggerFactory.getLogger(IoPacketValve.class);
	protected final AtomicInteger count = new AtomicInteger(0);
	protected final int threshold;
	protected final boolean enabled;
	
	@Inject
	public IoPacketValve(Configuration configs) {
		this(configs.getInt("server.worker_dispatcher.valve.threshold", 30000), 
				configs.getBoolean("server.worker_dispatcher.valve.enabled", false));
	}
	
	public IoPacketValve(int threshold, boolean enabled) {
		this.threshold = threshold;
		this.enabled = enabled;
	}
	
	public boolean in(Channel ch, IoPacket pkg, Processor<? extends IoPacket, ? extends IoPacket> processor){
		if (enabled){
			int c = count.incrementAndGet();
			
			if (c > threshold*3/4)
				MonitorUtils.monitor(678462); //Auto-gen monitor: 请求队列阀值75%
			else if (c > threshold/3)
				MonitorUtils.monitor(678461); //Auto-gen monitor: 请求队列阀值33%
			else if (c > threshold/10)
				MonitorUtils.monitor(678460); //Auto-gen monitor: 请求队列阀值10%
			
			if (c > threshold){
				count.getAndDecrement();
				return false;
			}
		}
		return true;
	}
	
	public void out(Channel ch, IoPacket pkg, Processor<? extends IoPacket, ? extends IoPacket> processor){
		if (enabled){
			int c = count.decrementAndGet();
			if (c < 0){
				MonitorUtils.monitor(678459); //Auto-gen monitor: 请求队列计数错误
				log.error("response more than once? " + c);
			}
		}
	}
}
