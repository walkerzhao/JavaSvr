package com.tencent.jungle.svrcore.ws;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.tencent.jungle.svrcore.packet.IoPacket;
import com.tencent.jungle.svrcore.ps.Processor;
import com.tencent.jungle.svrcore.UserTask;
import com.tencent.jungle.svrcore.ws.WorkerService;
import com.tencent.jungle.svrcore.comm.IoPacketValve;
import com.tencent.jungle.svrcore.utils.EC;
import com.tencent.jungle.svrcore.utils.MonitorUtils;
import io.netty.channel.Channel;
import org.apache.commons.configuration.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 简单工作线程池。一般用于DB相关服务
 */
@Singleton
public class ThreadPoolWorkerService implements WorkerService {
	static final Logger log = LoggerFactory.getLogger(ThreadPoolWorkerService.class);
	static final AtomicInteger GID = new AtomicInteger(0);
	
	ExecutorService tasks;
	protected IoPacketValve valve;
	
	@Inject
	public ThreadPoolWorkerService(Configuration configs, IoPacketValve valve){
		this(configs.getInt("server.thread_pool_size", Runtime.getRuntime().availableProcessors()*4));
		this.valve = valve;
	}
	
	public ThreadPoolWorkerService() {
		this(Runtime.getRuntime().availableProcessors());
	}
	
	public ThreadPoolWorkerService(@Deprecated int threads) {
	    // bugfix：这里不能用fixed的thread pool，否则一旦服务请求量上来，并且Client后端回包慢，会导致线程耗尽而死锁
		this.tasks = Executors.newCachedThreadPool(new ThreadFactory() {
		    final AtomicInteger TID = new AtomicInteger(0);
		    final String GROUP_NAME = "TPWS-" + GID.getAndIncrement();
            @Override
            public Thread newThread(Runnable r) {
                return new Thread(r, GROUP_NAME + TID.getAndIncrement());
            }
        });
	}

	@Override
	public void dispatch(Channel ch, IoPacket msg, Processor<IoPacket, IoPacket> processor) {
		if (valve!=null && !valve.in(ch, msg, processor)){
			log.error("IoPacket valve over load? ");
			IoPacket rspPkg = msg.newResponsePacket(msg, EC.SERVER_OVERLOAD, "server over load", null);
			if (rspPkg != null)
				ch.write(rspPkg).addListener(ServerWriteFutureListener.INSTANCE);
			return;
		}
		tasks.execute(new TPWS_Runnable0(ch, msg, processor, valve));
	}
	
	@Override
	public void dispatch(final UserTask task) {
		tasks.execute(new Runnable() {
			
			@Override
			public void run() {
				try {
					task.runTask();
				} catch (Exception e) {
					log.error("should not reach", e);
				}
			}
		});
	}
}


class TPWS_Runnable0 implements Runnable {
	final Channel ch;
	final IoPacket msg;
	final Processor<IoPacket, IoPacket> processor;
	final IoPacketValve valve;

	public TPWS_Runnable0(final Channel ch, final IoPacket msg, final Processor<IoPacket, IoPacket> processor, IoPacketValve valve) {
		this.ch = ch;
		this.msg = msg;
		this.processor = processor;
		this.valve = valve;
	}
	
	@Override
	public void run() {
		IoPacket rsp =null;
		Throwable throwable=null;
		try{

			rsp = processor.process(msg, ch);
			if (rsp != null)
				ch.writeAndFlush(rsp).addListener(ServerWriteFutureListener.INSTANCE);
		}
		catch (Exception ex){
			MonitorUtils.monitor(678458); //Auto-gen monitor: 请求处理异常
			ThreadPoolWorkerService.log.error("error in processing " + msg.getIoCmd(), ex);
			throwable=ex;
		}
		finally{
			if (valve != null)
				valve.out(ch, msg, processor);
		}
		
	}
}
