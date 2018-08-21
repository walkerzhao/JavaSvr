package com.tencent.dolphin.svrcore.client;

import com.tencent.dolphin.svrcore.CodecService;
import com.tencent.dolphin.svrcore.packet.IoPacket;
import com.tencent.dolphin.svrcore.ws.WorkerService;
import com.tencent.dolphin.svrcore.client.RouterService.RouterInfo;
import com.tencent.dolphin.svrcore.comm.ChannelAttachment;
import com.tencent.dolphin.svrcore.ps.ClientIoProcessorService;
import com.tencent.dolphin.svrcore.utils.ClientTimeoutException;
import com.tencent.dolphin.svrcore.utils.MonitorUtils;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.ChannelHandler.Sharable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.concurrent.*;


@Sharable
public abstract class BaseClientIoService<T_REQ extends IoPacket, T_RSP extends IoPacket> 
		extends SimpleChannelInboundHandler<IoPacket> // 这里不能指定泛型为T_RSP，会抛异常cannot determine the type of the type parameter 'T_RSP'
		implements ClientIoService<T_REQ, T_RSP>,
		ChannelFutureListener{
	static final Logger log = LoggerFactory.getLogger(BaseClientIoService.class);
	
	protected Bootstrap bootstrap;
	protected CodecService codec;
	protected RouterService routerService;
	protected WorkerService workerService;
	protected TimeoutManager timeoutManager;
	
	/**
	 * 异步协议回调状态存储
	 */
	protected ConcurrentHashMap<Object, PacketLikeLookupItem> lookups = 
			new ConcurrentHashMap<Object, PacketLikeLookupItem>(8192, 0.75F, 512);
	
	@Override
	public ClientIoService<T_REQ, T_RSP> setCodecService(CodecService codec) {
		this.codec = codec;
		return this;
	}

	@Override
	public ClientIoService<T_REQ, T_RSP> setTimeoutManager(TimeoutManager timeoutManager) {
		this.timeoutManager = timeoutManager;
		return this;
	}

	@Override
	public ClientIoService<T_REQ, T_RSP> setRouterService(RouterService routerService) {
		this.routerService = routerService;
		return this;
	}

	@Override
	public ClientIoService<T_REQ, T_RSP> setWorkerService(WorkerService workerService) {
		this.workerService = workerService;
		return this;
	}
	
	PacketLikeLookupItem getLookupsItem(Channel ch, Object ioSeq) {
		return getLookups(ch).remove(ioSeq);
	}
	ConcurrentHashMap<Object, PacketLikeLookupItem> getLookups(Channel ch){
		ChannelAttachment attachment = ChannelAttachment.get(ch);
		return attachment==null ? lookups : attachment.lookups;
	}
	
	@Override
	protected void channelRead0(ChannelHandlerContext ctx, final IoPacket msg)
			throws Exception {
		
		final Channel ch = ctx.channel();
		final PacketLikeLookupItem item = getLookupsItem(ctx.channel(), msg.getIoSeq());
		if (item == null) {
			log.error(msg.getIoCmd()+", seq="+msg.getIoSeq()+" response callback could not be found, lookups="+lookups.size());
			MonitorUtils.monitor(2225508); //Auto-gen monitor: 超时后回包
			return;
		}
		Exception err=null;
		try{
		item.timeoutTask.cancel(false);
		item.result = msg;
		routerService.update(item.routerInfo, true);
		workerService.dispatch(ch, item, ClientIoProcessorService.INSTANCE);
		}catch (Exception e) {
			err=e;
			throw e;
		}finally {
		}
	}
	
	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause)
			throws Exception {
		MonitorUtils.monitor(678446); //Auto-gen monitor: Channel异常
		log.error("error in channel " + ctx.channel().localAddress() + "->" + ctx.channel().remoteAddress(), cause);
	}
	
	protected abstract Channel getChannel(RouterInfo routerInfo);
	
	@Override
	public void async(final T_REQ req, long timeout, final IoCallBack<T_REQ, T_RSP> callback) {

		final RouterInfo routerInfo = routerService.next(req.getRouterId(), req);
		if (routerInfo == null)
			throw new IllegalStateException("could not get router info for " + req.getRouterId());
		final Channel ch = getChannel(routerInfo);
		if (log.isDebugEnabled())
			log.debug(req.getIoCmd() + ", seq=" + req.getIoSeq() + ", send request to " + req.getRouterId() + " " + routerInfo.getSocketAddr());
		req.setRouterAddr(routerInfo.getSocketAddr());
		Future<?> timeoutTask = timeoutManager.watch(new Runnable() {
			
			@Override
			public void run() {
				PacketLikeLookupItem item = getLookupsItem(ch, req.getIoSeq());
				if (item == null){
					log.error(req.getIoCmd()+", seq="+req.getIoSeq()+" timeout callback could not be found, lookups="+getLookups(ch).size());
					MonitorUtils.monitor(2225507); //Auto-gen monitor: 回包后超时
					return;
				}
				else if (item.req != req){
					MonitorUtils.monitor(678454); //Auto-gen monitor: 回包SEQ与原包不匹配
					log.error("FATAL ERROR!! unmatched ioseq & req, lookups held too many items without timeout checking?");
				}
				
				String errMsg = item.req.getIoCmd() + " timeout, " + routerInfo.ip+":"+routerInfo.port + ", seq="+item.req.getIoSeq();
				if (routerInfo != item.routerInfo)
					errMsg += ", unmatched response packet " + item.routerInfo.ip+":"+item.routerInfo.port;
				item.result = new MiniTimeoutException(errMsg);
				routerService.update(routerInfo, false);

				//*************************************************
				// 注：如果任务是同步的，这里dispatch的任务一定要注意不能与本身产生死锁！
				//*************************************************
				workerService.dispatch(ch, item, ClientIoProcessorService.INSTANCE);
				log.warn("request timeout ,{}",errMsg);

			}
		}, timeout);
		if (getLookups(ch).putIfAbsent(req.getIoSeq(), new PacketLikeLookupItem(req, callback, routerInfo, timeoutTask)) != null){
			MonitorUtils.monitor(678453); //Auto-gen monitor: 上行包SEQ重复
			log.error("FATAL ERROR!! duplicate ioseq in lookups! seq=" + req.getIoSeq() + ", cmd=" + req.getIoCmd());
		}
		// write需要在callback被放入lookups后才执行；否则机器性能太好，瞬间回包时，会出现有网络交互但api报超时
		
		ch.writeAndFlush(req).addListener(this);
		
		/*ChannelAttachment attachment = ChannelAttachment.get(ch);
		if (attachment != null && attachment.written.incrementAndGet()<5) {
			log.error(Thread.currentThread().getName()+" write incrementAndGet "+attachment.written.get()+" seq=" +req.getIoSeq());
			ch.write(req).addListener(this);
			log.error(Thread.currentThread().getName()+" write incrementAndGet "+attachment.written.get()+" seq=" +req.getIoSeq() +" DONE");
		}
		else {
			ch.writeAndFlush(req).addListener(this);
			EncoderFlushAdapter.flush.incrementAndGet();
		}*/
	}

	@Override
	public T_RSP sync(T_REQ req, long timeout) throws  Exception {
		final CountDownLatch latch = new CountDownLatch(1);
		final Object[] result = new Object[1];
		IoCallBack<T_REQ, T_RSP> callback = new IoCallBack<T_REQ, T_RSP>() {
			
			@Override
			public void exceptionCaught(Channel ch, IoPacket req, Throwable ex){
				result[0] = ex;
				latch.countDown();
			}
			
			@Override
			public void callback(Channel ch, IoPacket req, IoPacket resp)
					throws  Exception {
				result[0] = resp;
				latch.countDown();
			}
		};
		async(req, timeout, callback);
		
		latch.await();
		if (result[0] instanceof IoPacket)
			return (T_RSP)result[0];
		
		throw (Exception)result[0];
	}
	
	public Future<T_RSP> send(T_REQ req) {
		return send(req, 5000);
	}

	public Future<T_RSP> send(T_REQ req, long timeout) {
		SimpleFuture<T_RSP> future = new SimpleFuture<T_RSP>();
		async(req, timeout, future);
		return future;
	}
	
	@Override
	public void operationComplete(ChannelFuture future)
			throws Exception {
		if (future.isDone() && future.cause()!=null){
			MonitorUtils.monitor(2018807); // Client网络写失败
			log.error("client channel wrtie failed " + future.channel().remoteAddress(), future.cause());
			// TODO 回调中可以把ioseq带回来，如此可以提早通知写失败的请求回调
		}
	}
	
	static class SimpleFuture<T> implements IoCallBack, Future<T> {
		final CountDownLatch latch = new CountDownLatch(1);
		Object result;

		@Override
		public void callback(Channel ch, IoPacket req, IoPacket resp)
				throws  Exception {
			if (latch.getCount() == 0)
				return;
			this.result = resp;
			latch.countDown();
		}

		@Override
		public void exceptionCaught(Channel ch, IoPacket req, Throwable ex) {
			if (latch.getCount() == 0)
				return;
			this.result = ex;
			latch.countDown();
		}

		@Override
		public boolean cancel(boolean mayInterruptIfRunning) {
			return false;
		}

		@Override
		public boolean isCancelled() {
			return false;
		}

		@Override
		public boolean isDone() {
			return latch.getCount() == 0;
		}

		@Override
		public T get() throws InterruptedException, ExecutionException {
			latch.await();
			if (result instanceof Throwable)
				throw new ExecutionException((Throwable)result);
			return (T)result;
		}

		@Override
		public T get(long timeout, TimeUnit unit) throws InterruptedException,
				ExecutionException, TimeoutException {
			if (!latch.await(timeout, unit))
				throw TIMEOUT;
			if (result instanceof Throwable)
				throw new ExecutionException((Throwable)result);
			return (T)result;
		}
		
		static final TimeoutException TIMEOUT = new TimeoutException("future.get timeout"){
			public synchronized Throwable fillInStackTrace() {return this;};
		};
	}
}

/**
 * use {@link ClientTimeoutException} instead
 */
@Deprecated
class MiniTimeoutException extends ClientTimeoutException{
	public MiniTimeoutException(String msg) {
	super(msg);
}
}
