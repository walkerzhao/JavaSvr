package com.tencent.jungle.svrcore.qapp;

import com.tencent.jungle.svrcore.IoPacketDecoder;
import com.tencent.jungle.svrcore.utils.MonitorUtils;
import io.netty.buffer.ByteBuf;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class QAppClientDecoder extends IoPacketDecoder {
    static final Logger log = LoggerFactory.getLogger(QAppClientDecoder.class);
    
    public QAppRspPacket decode(ByteBuf buf) throws Exception{
    	List out = new ArrayList<QAppRspPacket>(1);
    	decode(null, buf, out);
    	if (out.size() > 0)
    		return (QAppRspPacket)out.get(0);
    	return null;
    }

	@Override
	protected void decode(io.netty.channel.ChannelHandlerContext ctx,
			ByteBuf buffer, List<Object> out) throws Exception {
		if (buffer.readableBytes() < 6)
			return ;
		
		buffer.markReaderIndex();
		if (buffer.readByte() != (byte) 0x02){
			MonitorUtils.monitor(678457); //Auto-gen monitor: 解包失败
			log.error("proto error, 1-st byte must be 0x2");
			if (ctx != null && !isUdp)
				ctx.channel().close();
			return ;
		}
		int length = buffer.readInt();
		if (buffer.readableBytes() < length - 5){
			buffer.resetReaderIndex();
			return ;
		}
		
		int bodyLen = length - 6;
		byte[] body = new byte[bodyLen];
		buffer.readBytes(body);
		if (buffer.readByte() != (byte) 0x03){
			MonitorUtils.monitor(678457); //Auto-gen monitor: 解包失败
			log.error("proto error, last byte must be 0x3");
			if (ctx != null && !isUdp)
				ctx.channel().close();
			return ;
		}
		
		QAppMsg.QAppResponse msg = QAppMsg.QAppResponse.parseFrom(body);
		
		out.add(new QAppRspPacket(msg));
	}
}
