package com.tencent.dolphin.svrcore.qapp;

import com.tencent.dolphin.svrcore.packet.IoPacket;
import com.tencent.dolphin.svrcore.comm.BaseIoPacket;
import com.tencent.dolphin.svrcore.utils.U;

public class QAppReqPacket extends BaseIoPacket {
	private QAppMsg.QAppRequest r;
	
	public QAppReqPacket(QAppMsg.QAppRequest r) {
		this.r = r;
	}

	@Override
	public Long getIoSeq() {
		return Long.valueOf(r.getSeq());
	}

	@Override
	public Object getIoCmd() {
		return r.getCmd();
	}

	@Override
	public Object getRouterId() {
		return r.getAppName();
	}

	@Override
	public long getReqUid() {
		return r.getUid();
	}

	public QAppMsg.QAppRequest getMsg() {
		return r;
	}
	
	public void setMsg(QAppMsg.QAppRequest r) {
		this.r = r;
	}
	
	@Override
	public String toString() {
		return r.toString();
	}

	@Override
	public QAppRspPacket newResponsePacket(IoPacket reqPacket, int ec,
			String message, Object body) {
		return QAppRspPacket.newInstance((QAppReqPacket)reqPacket, ec, message, body==null?U.EMPTY_BYTES:(byte[])body);
	}
	
	@Override
	public int getEstimateSize() {
		return 6+r.getSerializedSize();
	}
}
