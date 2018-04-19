package com.tencent.jungle.svrcore.ps;

import com.tencent.jungle.svrcore.packet.IoPacket;
import com.tencent.jungle.svrcore.ps.Processor;
import com.tencent.jungle.svrcore.ps.ProcessorService;
import com.tencent.jungle.svrcore.client.PacketLikeLookupItem;
import io.netty.channel.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 作为一个client，通常对响应包都是进行统一处理（调用对应的回调），因此IoCallBackProcessor可单例
 */
public class ClientIoProcessorService implements ProcessorService {
	static final Logger log = LoggerFactory.getLogger(ClientIoProcessorService.class);
	
	public static final Processor INSTANCE = new IoCallBackProcessor();

	@Override
	public Processor map(Channel ch, IoPacket msg) {
		return INSTANCE;
	}

	static class IoCallBackProcessor implements Processor<IoPacket, IoPacket>{
		
		@Override
		public IoPacket process(IoPacket resp, Channel ioChannel)
				throws  Exception {
			PacketLikeLookupItem<IoPacket, IoPacket> item = (PacketLikeLookupItem<IoPacket, IoPacket>)resp;
			
			if (item.result instanceof IoPacket){
				try {
					item.callback.callback(ioChannel, item.req, (IoPacket)item.result);
				} catch (Exception e) {
					onExecptionCaught(ioChannel, item, e);
				}
			}
			else
				onExecptionCaught(ioChannel, item, (Exception)item.result);
			return null;
		}
		
		static void onExecptionCaught(Channel ioChannel, PacketLikeLookupItem<IoPacket, IoPacket> item, 
				Exception e) {
			try{
				item.callback.exceptionCaught(ioChannel, item.req, e);
			}
			catch (Exception e2){
				log.error("exception caught again", e2);
			}
		}
	}
	
}
