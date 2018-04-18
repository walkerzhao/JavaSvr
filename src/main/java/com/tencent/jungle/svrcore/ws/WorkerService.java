package com.tencent.jungle.svrcore.ws;

import com.tencent.jungle.svrcore.packet.IoPacket;
import com.tencent.jungle.svrcore.ps.Processor;
import com.tencent.jungle.svrcore.UserTask;
import io.netty.channel.Channel;

/**
 * 工作线程池。
 * @see KilimBasedWorkerService
 * @see ThreadPoolWorkerService
 * @see ExtremeWorkerService
 */
public interface WorkerService {
	void dispatch(Channel ch, IoPacket msg, Processor<IoPacket, IoPacket> processor);
	
	void dispatch(UserTask task);
}
