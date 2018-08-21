package com.tencent.dolphin.svrcore.ps;

import com.tencent.dolphin.svrcore.packet.IoPacket;
import com.tencent.dolphin.svrcore.io.ServerIoService;
import com.tencent.dolphin.svrcore.client.PropertiesL5RouterService;
import io.netty.channel.Channel;

/**
 * 由 {@link ServerIoService} 调用。将一个协议上行包 {@link IoPacket} 映射到一个 {@link Processor} 进行业务处理
 * 示例：
 * <code>
 * ProcessorService ps = new NotFoundProcessorService(
						new PropertiesProcessorService(configs, injector), 
						injector.getInstance(NotFound.class))
 * </code>
 * @see PropertiesL5RouterService
 * @see IntPropertiesProcessorService
 * @see NotFoundProcessorService
 */
public interface ProcessorService {
	Processor<IoPacket, IoPacket> map(Channel ch, IoPacket req);
}
