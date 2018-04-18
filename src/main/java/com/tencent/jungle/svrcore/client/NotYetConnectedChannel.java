package com.tencent.jungle.svrcore.client;

import com.tencent.jungle.svrcore.utils.MonitorUtils;
import com.tencent.jungle.svrcore.utils.U;
import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.*;
import io.netty.util.Attribute;
import io.netty.util.AttributeKey;
import io.netty.util.concurrent.GenericFutureListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.SocketAddress;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.atomic.AtomicReference;

public class NotYetConnectedChannel implements Channel, GenericFutureListener<ChannelFuture> {
	static final Logger log = LoggerFactory.getLogger(NotYetConnectedChannel.class);
	protected ChannelFuture connectingFuture;
	protected AtomicReference<Channel> ref;
	protected final Queue<Msg> pendingMsgs = new LinkedList<Msg>();

	public NotYetConnectedChannel(ChannelFuture connectingFuture, AtomicReference<Channel> ref) {
		this.connectingFuture = connectingFuture;
		this.ref = ref;
	}

	/**
	 * 连接操作回调。运行在event loop上
	 * 1、操作成功，替换MyChannelGroup中对应的ref
	 * 2、操作失败，连接超时/对端refuse等
	 */
	@Override
	public void operationComplete(ChannelFuture future) throws Exception {
		Channel ch = future.channel();
		if (future != connectingFuture) {
			log.warn("unmatched future object: ref=" + ref.get() + ", future=" + ch);
		}
		if (future.isSuccess()) {
			MonitorUtils.monitor(678451); //Auto-gen monitor: TCP_CLIENT新建连接成功
			if (U.SVRCORE_DEBUG_ENABLED) {
				log.debug(System.currentTimeMillis() + " new channel connected " + ch.localAddress() + "|" + ch.remoteAddress());
			}
			// 理应仅当一个NotYetConnectedChannel连接失败，但回调尚未执行，但业务线程又执行了一次ClientIoService.async时出现
			if (!ref.compareAndSet(this, ch)){
				if (U.SVRCORE_DEBUG_ENABLED) {
					log.debug("channel connected, but ref cas failed: ref=" + ref.get() + ", future=" + ch);
				}
				ch.close();
			}
			// 避免：如果恰好有一个msg在连接完成前后来write，并且pendingMsgs是空或数据很少，那么可能这个msg会被忽略而超时
			synchronized (pendingMsgs) {
				int size = pendingMsgs.size();
				while (true) {
					try {
						Msg m = pendingMsgs.poll();
						if (m == null) break;
						if (m.flush) {
							ch.writeAndFlush(m.msg, m.promise);
						}
						else {
							ch.write(m.msg, m.promise);
						}
					}
					catch (Exception ex) {
						// 理论上write方法产生的所有异常都会通知到promise，这里似乎不应该出现
						log.error("exception caugth when channel connected", ex);
					}
				}
				ch.flush();
				if (U.SVRCORE_DEBUG_ENABLED) {
					log.debug("channel connected, flush pending msgs: count=" + (size-pendingMsgs.size()) + ", " + ch.localAddress() + "|" + ch.remoteAddress());
				}
			}
		}
		else {
			MonitorUtils.monitor(678450); //Auto-gen monitor: TCP_CLIENT新建连接失败
			// 比较麻烦，先不重试了
			log.warn("connect to " + (ch==null?"":ch.remoteAddress()) + " failed, flush all pending msgs");
			Throwable throwable = future.cause();
			synchronized (pendingMsgs) {
				while (true) {
					try {
						Msg m = pendingMsgs.poll();
						if (m == null) break;
						m.promise.setFailure(throwable);
					}
					catch (Exception ex) {
						log.error("exception caugth when seting promise future failure", ex);
					}
				}
			}
		}
	}

	/** 当一个NotYetConnectedChannel被放入MyChannelGroup，又没有连接成功时（未连接超时），需要当作是active的，否则另一个rpc调用会把连接关掉 */
	@Override
	public boolean isActive() {
		return !connectingFuture.isDone() || connectingFuture.channel().isActive();
	}

	static class Msg {
		final Object msg;
		final ChannelPromise promise;
		final boolean flush;
		public Msg(Object msg, ChannelPromise promise, boolean flush) {
			this.msg = msg;
			this.promise = promise;
			this.flush = flush;
		}
	}

	protected ChannelFuture write0(Object msg, ChannelPromise promise, boolean flush) {
		Channel ch = connectingFuture.channel();
		if (promise == null){
			promise = ch.newPromise();
		}
		if (!connectingFuture.isDone()) {
			synchronized (pendingMsgs) {
				if (ch.isActive()) {
					ch.write(msg, promise);
				}
				else {
					pendingMsgs.offer(new Msg(msg, promise, true));
				}
			}
		}
		// closed or active
		else {
			if (flush) {
				ch.writeAndFlush(msg, promise);
			}
			else {
				ch.write(msg, promise);
			}
		}
			
		return promise;
	}
	
	@Override
	public ChannelFuture write(Object msg) {
		return write0(msg, null, true);
	}
	
	@Override
	public ChannelFuture write(Object msg, ChannelPromise promise) {
		return write0(msg, promise, true);
	}

	@Override
	public ChannelFuture writeAndFlush(Object msg, ChannelPromise promise) {
		return write0(msg, promise, true);
	}

	@Override
	public ChannelFuture writeAndFlush(Object msg) {
		return write0(msg, null, true);
	}

	@Override
	public <T> Attribute<T> attr(AttributeKey<T> key) {
		return connectingFuture.channel().attr(key);
	}

	@Override
	public int compareTo(Channel o) {
		return connectingFuture.channel().compareTo(o);
	}

	@Override
	public EventLoop eventLoop() {
		return connectingFuture.channel().eventLoop();
	}

	@Override
	public Channel parent() {
		return connectingFuture.channel().parent();
	}

	@Override
	public ChannelConfig config() {
		return connectingFuture.channel().config();
	}

	@Override
	public boolean isOpen() {
		return connectingFuture.channel().isOpen();
	}

	@Override
	public boolean isRegistered() {
		return connectingFuture.channel().isRegistered();
	}

	@Override
	public ChannelMetadata metadata() {
		return connectingFuture.channel().metadata();
	}

	@Override
	public SocketAddress localAddress() {
		return connectingFuture.channel().localAddress();
	}

	@Override
	public SocketAddress remoteAddress() {
		return connectingFuture.channel().remoteAddress();
	}

	@Override
	public ChannelFuture closeFuture() {
		return connectingFuture.channel().closeFuture();
	}

	@Override
	public boolean isWritable() {
		return connectingFuture.channel().isWritable();
	}

	@Override
	public Unsafe unsafe() {
		return connectingFuture.channel().unsafe();
	}

	@Override
	public ChannelPipeline pipeline() {
		return connectingFuture.channel().pipeline();
	}

	@Override
	public ByteBufAllocator alloc() {
		return connectingFuture.channel().alloc();
	}

	@Override
	public ChannelPromise newPromise() {
		return connectingFuture.channel().newPromise();
	}

	@Override
	public ChannelProgressivePromise newProgressivePromise() {
		return connectingFuture.channel().newProgressivePromise();
	}

	@Override
	public ChannelFuture newSucceededFuture() {
		return connectingFuture.channel().newSucceededFuture();
	}

	@Override
	public ChannelFuture newFailedFuture(Throwable cause) {
		return connectingFuture.channel().newFailedFuture(cause);
	}

	@Override
	public ChannelPromise voidPromise() {
		return connectingFuture.channel().voidPromise();
	}

	@Override
	public ChannelFuture bind(SocketAddress localAddress) {
		return connectingFuture.channel().bind(localAddress);
	}

	@Override
	public ChannelFuture connect(SocketAddress remoteAddress) {
		return connectingFuture.channel().connect(remoteAddress);
	}

	@Override
	public ChannelFuture connect(SocketAddress remoteAddress,
			SocketAddress localAddress) {
		return connectingFuture.channel().connect(remoteAddress, localAddress);
	}

	@Override
	public ChannelFuture disconnect() {
		return connectingFuture.channel().disconnect();
	}

	@Override
	public ChannelFuture close() {
		return connectingFuture.channel().close();
	}

	@Override
	public ChannelFuture deregister() {
		return connectingFuture.channel().deregister();
	}

	@Override
	public ChannelFuture bind(SocketAddress localAddress, ChannelPromise promise) {
		return connectingFuture.channel().bind(localAddress, promise);
	}

	@Override
	public ChannelFuture connect(SocketAddress remoteAddress,
			ChannelPromise promise) {
		return connectingFuture.channel().connect(remoteAddress, promise);
	}

	@Override
	public ChannelFuture connect(SocketAddress remoteAddress,
			SocketAddress localAddress, ChannelPromise promise) {
		return connectingFuture.channel().connect(remoteAddress, localAddress, promise);
	}

	@Override
	public ChannelFuture disconnect(ChannelPromise promise) {
		return connectingFuture.channel().disconnect(promise);
	}

	@Override
	public ChannelFuture close(ChannelPromise promise) {
		return connectingFuture.channel().close(promise);
	}

	@Override
	public ChannelFuture deregister(ChannelPromise promise) {
		return connectingFuture.channel().deregister(promise);
	}

	@Override
	public Channel read() {
		return connectingFuture.channel().read();
	}
	
	@Override
	public Channel flush() {
		return connectingFuture.channel().flush();
	}

}
