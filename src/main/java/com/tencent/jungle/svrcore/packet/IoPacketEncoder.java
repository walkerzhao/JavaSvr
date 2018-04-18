package com.tencent.jungle.svrcore.packet;

import com.tencent.jungle.svrcore.packet.IoPacket;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;


public abstract class IoPacketEncoder<T_MSG extends IoPacket> extends MessageToByteEncoder<T_MSG> {
	public void doEncode(ChannelHandlerContext ctx,
			T_MSG msg, ByteBuf buf) throws Exception{
		this.encode(ctx, msg, buf);
	}

	protected boolean isUdp = false;
	public com.tencent.jungle.svrcore.packet.IoPacketEncoder<T_MSG> setUdp(boolean isUdp) {
		this.isUdp = isUdp;
		return this;
	}
	
	@Override
	public boolean acceptOutboundMessage(Object msg) throws Exception {
		return msg instanceof IoPacket;
	}
	
	@Override
	protected ByteBuf allocateBuffer(ChannelHandlerContext ctx, T_MSG msg,
			boolean preferDirect) throws Exception {
        int estimate = msg.getEstimateSize();
		return preferDirect ? 
				(estimate>0 ? ctx.alloc().ioBuffer(estimate) : ctx.alloc().ioBuffer()) :
				(estimate>0 ? ctx.alloc().heapBuffer(estimate) : ctx.alloc().heapBuffer());
	}
	
	public ByteBuf doAllocateBuffer(ChannelHandlerContext ctx, T_MSG msg) throws Exception {
		return allocateBuffer(ctx, msg, true/*目前总是true，因为根本没有暴露对应参数的构造函数出去*/);
	}
}
