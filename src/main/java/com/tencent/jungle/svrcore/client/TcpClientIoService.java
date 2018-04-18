package com.tencent.jungle.svrcore.client;

import com.tencent.jungle.api.APIException;
import com.tencent.jungle.svrcore.packet.IoPacket;
import com.tencent.jungle.svrcore.client.RouterService.RouterInfo;
import com.tencent.jungle.svrcore.comm.ChannelAttachment;
import com.tencent.jungle.svrcore.utils.MonitorUtils;
import com.tencent.jungle.svrcore.utils.U;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.Attribute;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

/**
 * 通用tcp client服务，管理连接、回调、IO。
 * 使用独立的IO/工作线程池<br/>
 * <code>
 * new KilimClientIoService<WnsUpPackage, WnsDownPackage>(new TcpClientIoService<WnsUpPackage, WnsDownPackage>())
				.setCodecService(new WnsClientCodecService())
				.setRouterService(new PropertiesL5RouterService(injector.getInstance(Configuration.class), "wns"))
				.setTimeoutManager(injector.getInstance(TimeoutManager.class))
				.setWorkerService(injector.getInstance(WorkerService.class))
				.start();
 * </code>
 */
//TODO 当前未实现清理已关闭的连接逻辑，对于远端超多IP的场景，可能会资源泄漏
@Sharable
public class TcpClientIoService<T_REQ extends IoPacket, T_RSP extends IoPacket> 
		extends BaseClientIoService<T_REQ, T_RSP> {
	static final Logger log = LoggerFactory.getLogger(TcpClientIoService.class);

	protected ConcurrentHashMap<String, MyChannelGroup> groups = 
			new ConcurrentHashMap<String, MyChannelGroup>(256);
	protected int channelsPerAddr = 2;
	protected int connectTimeout = 500;
	
	public TcpClientIoService<T_REQ, T_RSP> setChannelsPerAddr(int channelsPerAddr) {
		this.channelsPerAddr = channelsPerAddr;
		return this;
	}
	
	public void setConnectTimeout(int connectTimeout) {
		this.connectTimeout = connectTimeout;
	}
	
	@Override
	public void channelActive(ChannelHandlerContext ctx) throws Exception {
		if (U.SVRCORE_DEBUG_ENABLED)
			log.debug(System.currentTimeMillis() + " active channel " + ctx.channel().localAddress()+"|"+ctx.channel().remoteAddress());
		MonitorUtils.monitor(678449); //Auto-gen monitor: TCP_CLIENT打开连接
		super.channelActive(ctx);
	}
	@Override
	public void channelInactive(ChannelHandlerContext ctx) throws Exception {
		if (U.SVRCORE_DEBUG_ENABLED)
			log.debug(System.currentTimeMillis() + " inactive channel " + ctx.channel().localAddress()+"|"+ctx.channel().remoteAddress());
		MonitorUtils.monitor(678447); //Auto-gen monitor: TCP_CLIENT关闭连接
		super.channelInactive(ctx);
	}
	
	@Override
	public synchronized void start() {
		if (bootstrap != null)
			throw new IllegalStateException("could not start more than once");
		this.bootstrap = new Bootstrap();
		bootstrap
			.channel(NioSocketChannel.class)
			.group(new NioEventLoopGroup())
			.handler(new ChannelInitializer<Channel>() {
				protected void initChannel(Channel ch) throws Exception {
					ChannelPipeline p = ch.pipeline();
					p.addLast("e", codec.getEncoder(ch));
					p.addLast("d", codec.getDecoder(ch));
					p.addLast("h", TcpClientIoService.this);
				};
			})
			.option(ChannelOption.CONNECT_TIMEOUT_MILLIS, connectTimeout);
	}
	
	protected Channel getChannel(RouterInfo routerInfo){
		final String key = routerInfo.ip + ":" + routerInfo.port;
		MyChannelGroup group = groups.get(key);
		if (group == null){
			group = new MyChannelGroup(this.channelsPerAddr);
			MyChannelGroup oGroup = groups.putIfAbsent(key, group);
			if (oGroup != null)
				group = oGroup;
		}
		int chIndex = Math.abs(group.seq.getAndIncrement()%group.channels.length);
		AtomicReference<Channel> ref = group.channels[chIndex];
		if (U.SVRCORE_DEBUG_ENABLED) {
			log.debug(Thread.currentThread().getId() + ", index=" + chIndex + ", old=" + ref.get());
		}
		return getChannel(routerInfo, ref, 2);
	}
	
	protected Channel getChannel(RouterInfo routerInfo, AtomicReference<Channel> ref, int retry){
		Channel ch = ref.get();
		if (ch != null && ch.isActive()){
			if (U.SVRCORE_DEBUG_ENABLED) 
				log.debug(System.currentTimeMillis() + " reusing channel " + ch.localAddress() + "|" + ch.remoteAddress());
			MonitorUtils.monitor(678448); //Auto-gen monitor: TCP_CLIENT复用连接
			return ch;
		}
		if (ch != null){
			if (U.SVRCORE_DEBUG_ENABLED) 
				log.debug(System.currentTimeMillis() + " closing channel " + ch.localAddress() + "|" + ch.remoteAddress());
			ch.close();
		}
		ChannelFuture f = bootstrap.connect(routerInfo.getSocketAddr());
		Attribute<ChannelAttachment> attachment = f.channel().attr(ChannelAttachment.ATTR_KEY_CH_ATTACHMENT);
		attachment.set(new ChannelAttachment());
		if (U.SVRCORE_DEBUG_ENABLED) 
			log.debug(System.currentTimeMillis()+" new channel "+f.channel().localAddress()+"|"+routerInfo.getSocketAddr());
		
		NotYetConnectedChannel nycChannelAndFuture = new NotYetConnectedChannel(f, ref);
		if (ref.compareAndSet(ch, nycChannelAndFuture)) {
			if (U.SVRCORE_DEBUG_ENABLED) {
				log.error(Thread.currentThread().getId() + ", old=" + ch + ", new" + nycChannelAndFuture);
			}
			f.addListener(nycChannelAndFuture);
			return nycChannelAndFuture;
		}
		else {
			f.cancel(false);
			f.channel().close();
			if (retry <= 0){
				throw new APIException(10001, "connect to " + routerInfo.getSocketAddr() + " failed with retry, no channel available");
			}
			else {
				MonitorUtils.monitor(678452); //Auto-gen monitor: TCP_CLIENT新建连接重试
				return getChannel(routerInfo, ref, retry-1);
			}
		}
		
//		if (f.isSuccess() && f.channel().isActive()){
//			MonitorUtils.monitor(678451); //Auto-gen monitor: TCP_CLIENT新建连接成功
//			Channel nch = f.channel();
//			if (ref.compareAndSet(ch, nch)){
//				return nch;
//			}
//			else{
//				nch.close();
//				if (retry <= 0){
//					routerService.update(routerInfo, false);
//					throw new APIException(10001, "connect to " + routerInfo.getSocketAddr() + " failed with retry, no channel available");
//				}
//				if (log.isDebugEnabled())
//					log.debug("another thread created new channel, retrying getChannel " + routerInfo.getSocketAddr() + ", retry=" + retry);
//				MonitorUtils.monitor(678452); //Auto-gen monitor: TCP_CLIENT新建连接重试
//				return getChannel(routerInfo, ref, retry-1);
//			}
//		}
//		else{
//			MonitorUtils.monitor(678450); //Auto-gen monitor: TCP_CLIENT新建连接失败
//			f.channel().close();
//			if (retry <= 0){
//				routerService.update(routerInfo, false);
//				throw new APIException(10001, "connect to " + routerInfo.getSocketAddr() + " failed with retry, no channel available");
//			}
//			log.warn("create new channel timeout, retrying getChannel " + routerInfo.getSocketAddr() + ", retry=" + retry);
//			MonitorUtils.monitor(678452); //Auto-gen monitor: TCP_CLIENT新建连接重试
//			return getChannel(routerInfo, ref, retry-1);
//		}
	}

	static class MyChannelGroup {
		final AtomicInteger seq = new AtomicInteger(0);
		final AtomicReference<Channel>[] channels;
		MyChannelGroup(int channels){
			this.channels = new AtomicReference[channels];
			for (int i=0;i<this.channels.length;i++){
				this.channels[i] = new AtomicReference<Channel>(null);
			}
		}
	}
}
