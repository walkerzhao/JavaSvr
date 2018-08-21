package com.tencent.dolphin.svrcore.packet;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

import java.util.List;

public abstract class IoPacketDecoder extends ByteToMessageDecoder {
	public void doDecode(ChannelHandlerContext ctx, ByteBuf buffer, List<Object> out) throws Exception{
		this.decode(ctx, buffer, out);
	}
	
	protected boolean isUdp = false;
	public com.tencent.dolphin.svrcore.packet.IoPacketDecoder setUdp(boolean isUdp) {
		this.isUdp = isUdp;
		return this;
	}
}
