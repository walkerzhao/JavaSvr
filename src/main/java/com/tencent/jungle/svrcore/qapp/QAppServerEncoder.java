package com.tencent.jungle.svrcore.qapp;

import com.tencent.jungle.svrcore.IoPacketEncoder;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandler.Sharable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.OutputStream;

@Sharable
public class QAppServerEncoder extends IoPacketEncoder<QAppRspPacket>
{
    static final Logger log = LoggerFactory.getLogger(QAppServerEncoder.class);
    
    public ByteBuf encode(QAppRspPacket msg) throws Exception{
    	ByteBuf buf = Unpooled.buffer();
    	encode(null, msg, buf);
    	return buf;
    }

	@Override
	protected void encode(io.netty.channel.ChannelHandlerContext ctx,
			QAppRspPacket msg, ByteBuf buf) throws Exception {
		int startpos = buf.writerIndex();
		buf.writeByte((byte) 0x02);
		int hpos = buf.writerIndex();
		buf.writeInt(0);
		ByteBufOutputStream out = new ByteBufOutputStream(buf);
		msg.getMsg().writeTo(out);
		buf.writeByte((byte) 0x03);
		buf.setInt(hpos, buf.writerIndex()-startpos);
	}
	
	static class ByteBufOutputStream extends OutputStream{
		final ByteBuf buf;
		
		public ByteBufOutputStream(ByteBuf buf) {
			this.buf = buf;
		}

		@Override
		public void write(int b) throws IOException {
			buf.writeByte(b);
		}
		
		@Override
		public void write(byte[] b, int off, int len) throws IOException {
			buf.writeBytes(b, off, len);
		}
	}
	
}