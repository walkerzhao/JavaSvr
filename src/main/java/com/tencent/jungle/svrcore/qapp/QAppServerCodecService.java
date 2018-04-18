package com.tencent.jungle.svrcore.qapp;

import com.tencent.jungle.svrcore.packet.CodecService;
import com.tencent.jungle.svrcore.packet.IoPacketDecoder;
import com.tencent.jungle.svrcore.packet.IoPacketEncoder;
import io.netty.channel.Channel;

public class QAppServerCodecService implements CodecService<QAppRspPacket> {

	@Override
	public IoPacketDecoder getDecoder(Channel ch) {
		return new QAppServerDecoder();
	}

	@Override
	public IoPacketEncoder<QAppRspPacket> getEncoder(Channel ch) {
		return new QAppServerEncoder();
	}

}
