package com.tencent.jungle.svrcore.client;

import com.tencent.jungle.svrcore.packet.IoPacket;
import com.tencent.jungle.svrcore.client.RouterService.RouterInfo;

import java.net.InetSocketAddress;
import java.util.concurrent.Future;

/**
 * 伪装为IoPacket的类。保存乱序协议中的状态
 * @see TcpClientIoService#lookups
 */
public class PacketLikeLookupItem<T_REQ extends IoPacket, T_RSP extends IoPacket>
		implements IoPacket{
	public final IoPacket req;
	public final IoCallBack<T_REQ, T_RSP> callback;
	public final RouterInfo routerInfo;
	public final Future<?> timeoutTask;
	/** IoPacket or Exception */
	public Object result;
	
	public PacketLikeLookupItem(IoPacket req, IoCallBack<T_REQ, T_RSP> callback,
                                RouterInfo routerInfo, Future<?> timeoutTask) {
		this.req = req;
		this.callback = callback;
		this.routerInfo = routerInfo;
		this.timeoutTask = timeoutTask;
	}

	@Override
	public Object getIoSeq() {
		return null;
	}

	@Override
	public Object getIoCmd() {
		return null;
	}

	@Override
	public Object getRouterId() {
		return null;
	}

	@Override
	public long getCreateTime() {
		return 0;
	}

	@Override
	public long getReqUid() {
		return 0;
	}

	@Override
	public IoPacket newResponsePacket(IoPacket reqPacket, int ec,
			String message, Object body) {
		return null;
	}
	
	@Override
	public InetSocketAddress getRouterAddr() {
		return null;
	}

	@Override
	public void setRouterAddr(InetSocketAddress addr) {
		// nothing to do
	}
	
	@Override
	public int getEstimateSize() {
		return 0;
	}

	@Override
	public long getCreateNanoTime() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public long getRetCode() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public String getErrorMsg() {
		// TODO Auto-generated method stub
		return null;
	}
}
