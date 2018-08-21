package com.tencent.dolphin.svrcore.qapp;

import com.tencent.dolphin.svrcore.packet.CodecService;
import com.tencent.dolphin.svrcore.packet.IoPacketDecoder;
import com.tencent.dolphin.svrcore.packet.IoPacketEncoder;
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
