package com.tencent.jungle.svrcore;

import com.tencent.jungle.svrcore.packet.IoPacket;
import com.tencent.jungle.svrcore.packet.IoPacketDecoder;
import com.tencent.jungle.svrcore.packet.IoPacketEncoder;
import com.tencent.jungle.svrcore.qapp.QAppServerCodecService;
import io.netty.channel.Channel;

/**
 * 编解码器
 * @see QAppServerCodecService
 */
public interface CodecService<T_RSP extends IoPacket> {
	/**
	 * 每次调用需返回新实例。如果在不同的IoService中使用不同的CodecService，可返回同一实例
	 */
	IoPacketDecoder getDecoder(Channel ch);
	/**
	 * 每次调用需返回新实例。如果在不同的IoService中使用不同的CodecService，可返回同一实例
	 */
	IoPacketEncoder<T_RSP> getEncoder(Channel ch);
}
