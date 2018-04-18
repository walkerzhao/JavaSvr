package com.tencent.jungle.svrcore.ps;

import com.tencent.jungle.svrcore.packet.IoPacket;
import com.tencent.jungle.svrcore.ps.Processor;
import com.tencent.jungle.svrcore.ServerIoService;
import com.tencent.jungle.svrcore.client.PropertiesL5RouterService;
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
