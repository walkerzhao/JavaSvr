package com.tencent.jungle.svrcore.packet;

import com.tencent.jungle.svrcore.ProcessorService;
import com.tencent.jungle.svrcore.client.RouterService;
import com.tencent.jungle.svrcore.qapp.QAppReqPacket;
import com.tencent.jungle.svrcore.sso.MsfPackage;
import com.tencent.jungle.svrcore.wns.WnsUpPackage;
import java.net.InetSocketAddress;

/**
 * 网络上/下行包
 * @see QAppReqPacket
 * @see WnsUpPackage
 * @see MsfPackage
 */
public interface IoPacket {
	/**
	 * 用于协议乱序回包识别，一般为uint32
	 * @return
	 */
	Object getIoSeq();
	/**
	 * 协议命令号。部分协议回包并没有该字段。{@link ProcessorService}
	 * @return
	 */
	Object getIoCmd();
	/**
	 * 协议包创建时间。一般用作处理耗时统计
	 * @return
	 */
	long getCreateTime();
	long getCreateNanoTime();
	/**
	 * 创建请求包对应的响应包。
	 * @param reqPacket
	 * @param ec
	 * @param message
	 * @param body
	 * @return
	 */
	com.tencent.jungle.svrcore.IoPacket newResponsePacket(com.tencent.jungle.svrcore.IoPacket reqPacket, int ec, String message, Object body);

	/**
	 * 协议包路由规则。一般用于上行包。{@link RouterService}
	 * @return
	 */
	Object getRouterId();
	/**
	 * 对上行IO包，表示路由地址；对下行包，表示发送者地址
	 * @return
	 */
	InetSocketAddress getRouterAddr();
	/**
	 * 对上行IO包，表示路由地址；对下行包，表示发送者地址
	 * @return 
	 */
	void setRouterAddr(InetSocketAddress addr);
	
	/**
	 * 估算IoPacket的序列化大小。若无法估算，返回0
	 * @return
	 */
	int getEstimateSize();
	
	/**
	 * 获取请求包体中uid
	 * @return
	 */
	long getReqUid();
	
	/**
	 * 获取返回包中的错误码,用于智能监控上报
	 * @return
	 */
	long getRetCode();
	/**
	 * 获取返回包中的错误消息,用于智能监控上报
	 * @return
	 */
	String getErrorMsg();
	
}
