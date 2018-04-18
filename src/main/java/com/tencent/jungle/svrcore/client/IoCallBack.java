package com.tencent.jungle.svrcore.client;

import com.tencent.jungle.svrcore.packet.IoPacket;
import io.netty.channel.Channel;

/**
 * 网络异步调用回调接口
 */
public interface IoCallBack<T_REQ extends IoPacket, T_RSP extends IoPacket> {
	public void callback(Channel ch, T_REQ req, T_RSP resp) throws  Exception;
	
	public void exceptionCaught(Channel ch, T_REQ req, Throwable ex);
}
