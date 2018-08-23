package com.tencent.dolphin.svrcore.qapp;

import com.google.protobuf.ByteString;
import com.tencent.dolphin.svrcore.codec.AdaptorPacket;
import com.tencent.dolphin.svrcore.packet.IoPacket;
import com.tencent.dolphin.svrcore.utils.U;

public class QappReqAdaptorPacket extends AdaptorPacket {
	/** 包装请求体 */
	private QAppMsg.QAppRequest r = null;

	/***/
	public QappReqAdaptorPacket(QAppMsg.QAppRequest r) {
		this.r = r;
	}
	
	public  QAppMsg.QAppRequest getPkg() {
		
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
	
//	@Override
//	public byte[] getAuthKey() {
//		// TODO Auto-generated method stub
//		return r.getAuthKey().toByteArray();
//	}
//
//	@Override
//	public int getAuthType(){
//		return r.getAuthType();
//	}
	

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
	public QappRspAdaptorPacket newResponsePacket(IoPacket reqPacket, int ec,
			String message, Object body) {
		return QappRspAdaptorPacket.newInstance((QappReqAdaptorPacket)reqPacket, ec, message, 
				body==null?ByteString.copyFrom(U.EMPTY_BYTES): ByteString.copyFrom((byte[])body));
	}
	
	@Override
	public int getEstimateSize() {
		return 6+r.getSerializedSize();
	}

	@Override
	public String getClientIp() {
		
		return r.getClientIp();
	}
	
	@Override
	public String getCallerName() {
		
		return r.getBusiness();
	}


	 
}
