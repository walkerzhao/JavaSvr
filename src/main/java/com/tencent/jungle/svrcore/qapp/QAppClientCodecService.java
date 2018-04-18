package com.tencent.jungle.svrcore.qapp;

import com.tencent.jungle.svrcore.CodecService;
import com.tencent.jungle.svrcore.packet.IoPacketDecoder;
import com.tencent.jungle.svrcore.packet.IoPacketEncoder;
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
