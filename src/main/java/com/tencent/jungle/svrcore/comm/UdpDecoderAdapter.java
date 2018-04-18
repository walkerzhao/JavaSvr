package com.tencent.jungle.svrcore.comm;

import com.tencent.jungle.svrcore.packet.IoPacket;
import com.tencent.jungle.svrcore.packet.IoPacketDecoder;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.socket.DatagramPacket;
import io.netty.handler.codec.MessageToMessageDecoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class UdpDecoderAdapter extends MessageToMessageDecoder<DatagramPacket>{
	static final Logger log = LoggerFactory.getLogger(UdpDecoderAdapter.class);
	IoPacketDecoder wrap;
	
	public UdpDecoderAdapter(IoPacketDecoder wrap) {
		this.wrap = wrap;
		this.wrap.setUdp(true);
	}

	@Override
	protected void decode(ChannelHandlerContext ctx, DatagramPacket msg,
			List<Object> out) throws Exception {
		ByteBuf buf = msg.content();
		List<Object> yetOut = new ArrayList<Object>(1);
		try {
			wrap.doDecode(ctx, buf, yetOut);
		}
		finally {
			if (ctx!=null && ctx.channel()!=null && !ctx.channel().isOpen()){
				log.error("udp server/client channel should not be closed! check your decoder " + (wrap==null ? "null" : wrap.getClass().getName()));
			}
		}
		for (Object pkg : yetOut){
			if (pkg instanceof IoPacket)
				((IoPacket) pkg).setRouterAddr(msg.sender());
			out.add(pkg);
		}
	}
	
}