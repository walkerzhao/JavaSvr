package com.tencent.dolphin.svrcore.qapp;

import com.tencent.dolphin.svrcore.CodecService;
import com.tencent.dolphin.svrcore.packet.IoPacketDecoder;
import com.tencent.dolphin.svrcore.packet.IoPacketEncoder;
import io.netty.channel.Channel;

public class QAppClientCodecService implements CodecService<QAppReqPacket> {

	@Override
	public IoPacketDecoder getDecoder(Channel ch) {
		return new QAppClientDecoder();
	}

	@Override
	public IoPacketEncoder<QAppReqPacket> getEncoder(Channel ch) {
		return new QAppClientEncoder();
	}

}
