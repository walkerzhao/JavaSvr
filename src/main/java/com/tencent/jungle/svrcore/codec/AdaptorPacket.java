package com.tencent.jungle.svrcore.codec;

import com.google.protobuf.ByteString;
import com.tencent.jungle.svrcore.packet.IoPacket;
import com.tencent.jungle.svrcore.comm.BaseIoPacket;

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
