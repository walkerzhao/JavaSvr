package com.tencent.dolphin.svrcore.qapp;

import com.google.protobuf.ByteString;
import com.tencent.dolphin.svrcore.codec.AdaptorPacket;

public class QappRspAdaptorPacket extends AdaptorPacket {
	/** 包装请求体 */
	private QAppMsg.QAppResponse r = null;

	/***/
	public QappRspAdaptorPacket(QAppMsg.QAppResponse r) {
		this.r = r;
	}
	
	public  QAppMsg.QAppResponse getPkg() {
		
		return r;
	}

	public ByteString getBody() {
		
		return r.getBody();
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

	public static QappRspAdaptorPacket newInstance(QappReqAdaptorPacket req, int ec, String msg, ByteString rspMsgBody){
		QAppMsg.QAppResponse.Builder rspMsg = QAppMsg.QAppResponse.newBuilder()
				.setSeq(req.getMsg().getSeq())
				.setBody(rspMsgBody)
				.setErrCode(ec);
		if (msg != null)
			rspMsg.setErrMsg(msg);
		QappRspAdaptorPacket qappRspDolphinPacket = new QappRspAdaptorPacket(rspMsg.build());
		qappRspDolphinPacket.setRouterAddr(req.getRouterAddr());
		qappRspDolphinPacket.setCodeAndMsg(ec, msg);
		return qappRspDolphinPacket;
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
