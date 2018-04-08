package com.tencent.jungle.svrcore.packet;

import com.tencent.jungle.svrcore.IoPacket;
import com.tencent.jungle.svrcore.IoPacketDecoder;
import com.tencent.jungle.svrcore.IoPacketEncoder;
import com.tencent.jungle.svrcore.qapp.QAppServerCodecService;
import com.tencent.jungle.svrcore.sso.SsoCodecService;
import com.tencent.jungle.svrcore.wns.WnsCodecService;
import io.netty.channel.Channel;

/**
 * 编解码器
 * @see QAppServerCodecService
 * @see WnsCodecService
 * @see SsoCodecService
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
