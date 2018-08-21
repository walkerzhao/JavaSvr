package com.tencent.dolphin.svrcore.ps;

import com.tencent.dolphin.svrcore.packet.IoPacket;
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
