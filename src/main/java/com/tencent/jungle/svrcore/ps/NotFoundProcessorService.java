package com.tencent.jungle.svrcore.ps;

import com.google.common.collect.Lists;
import com.tencent.jungle.svrcore.packet.IoPacket;
import com.tencent.jungle.svrcore.ps.Processor;
import com.tencent.jungle.svrcore.ps.ProcessorService;
import io.netty.channel.Channel;

import java.util.List;

/**
 * 一个包装类，当一个IoPacket无法映射到任意processor时候，返回默认的processor。
 * 一般作为命令字找不到时回包处理
 */
public class NotFoundProcessorService implements ProcessorService {
	final Processor defaultProcessor;
	final List<ProcessorService> serviceList = Lists.newArrayList();
	
	public NotFoundProcessorService(ProcessorService service, Processor defaultProcessor) {
		this.serviceList.add(service);
		this.defaultProcessor = defaultProcessor;
	}

	public NotFoundProcessorService(Processor defaultProcessor, ProcessorService ... services) {
		for (ProcessorService service : services) {
			serviceList.add(service);
		}
		this.defaultProcessor = defaultProcessor;
	}

	@Override
	public Processor map(Channel ch, IoPacket req) {

		Processor<IoPacket, IoPacket> p = null;
		for (ProcessorService processorService : serviceList) {
			p = processorService.map(ch, req);
			if (p != null) {
				break;
			}
		}
		return p == null ? defaultProcessor : p;
	}

}
