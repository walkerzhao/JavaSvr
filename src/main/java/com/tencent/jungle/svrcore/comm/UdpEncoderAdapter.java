package com.tencent.jungle.svrcore.comm;

import com.tencent.jungle.svrcore.packet.IoPacket;
import com.tencent.jungle.svrcore.packet.IoPacketEncoder;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.socket.DatagramPacket;
import io.netty.handler.codec.MessageToMessageEncoder;

import java.net.InetSocketAddress;
import java.util.List;

public class UdpEncoderAdapter extends MessageToMessageEncoder<IoPacket>{
	IoPacketEncoder<IoPacket> wrap;
	
	public UdpEncoderAdapter(IoPacketEncoder<IoPacket> wrap) {
		this.wrap = wrap;
		this.wrap.setUdp(true);
	}
	
	@Override
	protected void encode(ChannelHandlerContext ctx, IoPacket msg,
			List<Object> out) throws Exception {
		InetSocketAddress addr = msg.getRouterAddr();
		if (addr == null){
			throw new IllegalStateException("IoPacket.getSenderAddr() could not be null for udp, check your IoPacket.newResponsePacket() method");
		}
		ByteBuf buf = null;
		boolean release = true;
		try {
			buf = ctx.alloc().directBuffer();
			wrap.doEncode(ctx, msg, buf);
			release = false;
		}finally{
			if(release && buf !=null){
				buf.release();
			}
		}
		DatagramPacket pkg = new DatagramPacket(buf, addr);
		out.add(pkg);
	}
}