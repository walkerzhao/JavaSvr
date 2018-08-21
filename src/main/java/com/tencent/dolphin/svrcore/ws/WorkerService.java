package com.tencent.dolphin.svrcore.ws;

import com.tencent.dolphin.svrcore.packet.IoPacket;
import com.tencent.dolphin.svrcore.ps.Processor;
import com.tencent.dolphin.svrcore.UserTask;
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
