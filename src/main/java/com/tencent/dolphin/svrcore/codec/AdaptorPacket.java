package com.tencent.dolphin.svrcore.codec;

import com.google.protobuf.ByteString;
import com.tencent.dolphin.svrcore.packet.IoPacket;
import com.tencent.dolphin.svrcore.comm.BaseIoPacket;

public abstract class AdaptorPacket extends BaseIoPacket {
 

	@Override
	public AdaptorPacket newResponsePacket(IoPacket reqPacket, int ec,
										   String message, Object body) {
		setCodeAndMsg(ec, message);
		return null;
	}
	
	
	public abstract Object getPkg();
	public abstract ByteString getBody();
	
	public String getClientIp(){
		return null;
	};
	
	public String getCallerName(){
		return null;
	}
	
	
}
