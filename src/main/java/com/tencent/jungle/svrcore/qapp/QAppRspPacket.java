package com.tencent.jungle.svrcore.qapp;

import com.google.protobuf.ByteString;
import com.tencent.jungle.svrcore.comm.BaseIoPacket;

public class QAppRspPacket extends BaseIoPacket {
	QAppMsg.QAppResponse r;
	
	public QAppRspPacket(QAppMsg.QAppResponse r) {
		this.r = r;
	}

	@Override
	public Long getIoSeq() {
		return Long.valueOf(r.getSeq());
	}

	@Override
	public Object getIoCmd() {
		// 不能主动发QAppRspPacket包
		return null;
	}

	@Override
	public Object getRouterId() {
		// 不能主动发QAppRspPacket包
		return null;
	}

	@Override
	public long getReqUid() {
		return 0;
	}

	public QAppMsg.QAppResponse getMsg() {
		return r;
	}

	public void setMsg(QAppMsg.QAppResponse r) {
		this.r = r;
	}

	public static QAppRspPacket newInstance(QAppReqPacket req, int ec, String msg, byte[] rspMsgBody){
		QAppMsg.QAppResponse.Builder rspMsg = QAppMsg.QAppResponse.newBuilder()
				.setSeq(req.getMsg().getSeq())
				.setBody(ByteString.copyFrom(rspMsgBody))
				.setErrCode(ec);
		if (msg != null)
			rspMsg.setErrMsg(msg);
		QAppRspPacket rsp = new QAppRspPacket(rspMsg.build());
		rsp.setRouterAddr(req.getRouterAddr());
		rsp.setCodeAndMsg(ec, msg);
		return rsp;
	}
	
	@Override
	public String toString() {
		return r.toString();
	}
	@Override
	public int getEstimateSize() {
		return 6+r.getSerializedSize();
	}
}
