package com.tencent.dolphin.svrcore.qapp;

import com.tencent.dolphin.svrcore.packet.IoPacketDecoder;
import com.tencent.dolphin.svrcore.utils.MonitorUtils;
import io.netty.buffer.ByteBuf;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class QAppServerDecoder extends IoPacketDecoder {
    static final Logger log = LoggerFactory.getLogger(QAppServerDecoder.class);
    
    public QAppReqPacket decode(ByteBuf buf) throws Exception{
    	List out = new ArrayList<QAppReqPacket>(1);
    	decode(null, buf, out);
    	if (out.size() > 0)
    		return (QAppReqPacket)out.get(0);
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
		
		QAppMsg.QAppRequest msg = QAppMsg.QAppRequest.parseFrom(body);
		
		out.add(new QAppReqPacket(msg));
	}
}
