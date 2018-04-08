package com.tencent.jungle.svrcore.ps;

import com.tencent.jungle.svrcore.IoPacket;
import com.tencent.jungle.svrcore.Processor;
import com.tencent.jungle.svrcore.ProcessorService;
import io.netty.channel.Channel;

import java.util.HashMap;
import java.util.Map;

public abstract class BaseProcessorService implements ProcessorService {
	protected Map<Object, Processor> processors = new HashMap<Object, Processor>(64);

	@Override
	public Processor map(Channel ch, IoPacket req) {
		return processors.get(req.getIoCmd());
	}

}
