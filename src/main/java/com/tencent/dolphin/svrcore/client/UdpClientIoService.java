package com.tencent.dolphin.svrcore.client;

import com.tencent.dolphin.svrcore.packet.IoPacket;
import com.tencent.dolphin.svrcore.client.RouterService.RouterInfo;
import com.tencent.dolphin.svrcore.comm.ChannelAttachment;
import com.tencent.dolphin.svrcore.comm.UdpDecoderAdapter;
import com.tencent.dolphin.svrcore.comm.UdpEncoderAdapter;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioDatagramChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * 通用udp client服务，管理连接、回调、IO。
 * 使用独立的IO/工作线程池<br/>
 * <code>
 * new KilimClientIoService<WnsUpPackage, WnsDownPackage>(new UdpClientIoService<WnsUpPackage, WnsDownPackage>())
				.setCodecService(new WnsClientCodecService())
				.setRouterService(new PropertiesL5RouterService(injector.getInstance(Configuration.class), "wns"))
				.setTimeoutManager(injector.getInstance(TimeoutManager.class))
				.setWorkerService(injector.getInstance(WorkerService.class))
				.start();
 * </code>
 */
@Sharable
public class UdpClientIoService<T_REQ extends IoPacket, T_RSP extends IoPacket> 
		extends BaseClientIoService<T_REQ, T_RSP> {
	static final Logger log = LoggerFactory.getLogger(UdpClientIoService.class);
	
	static final int cpuNum = Runtime.getRuntime().availableProcessors();
	final AtomicInteger seq = new AtomicInteger(0);
	protected Channel[] channel = new Channel[cpuNum];
	protected int recvBufferSize = 8096;
	
	public void setRecvBufferSize(int recvBufferSize) {
		this.recvBufferSize = recvBufferSize;
	}
	
	@Override
	public synchronized void start() {
		if (bootstrap != null)
			throw new IllegalStateException("could not start more than once");
		final NioEventLoopGroup workerGroup = new NioEventLoopGroup();
		this.bootstrap = new Bootstrap();
		bootstrap
			.channel(NioDatagramChannel.class)
			.group(new NioEventLoopGroup())
			.handler(new ChannelInitializer<Channel>() {
				protected void initChannel(Channel ch) throws Exception {
					ChannelPipeline p = ch.pipeline();
					p.addLast(workerGroup,"e", new UdpEncoderAdapter(codec.getEncoder(ch)));
					p.addLast(workerGroup,"d", new UdpDecoderAdapter(codec.getDecoder(ch)));
					p.addLast(workerGroup,"h", UdpClientIoService.this);
				};
			})
			.option(ChannelOption.RCVBUF_ALLOCATOR, new FixedRecvByteBufAllocator(recvBufferSize));
		
		
		
		for (int i = 0; i < cpuNum; ++i) {
            ChannelFuture f = null;
			try {
				f = bootstrap.bind("0.0.0.0",0).await();
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
			}
			if (f.isSuccess() && f.channel().isActive()){
				channel[i] = f.channel();
				channel[i].attr(ChannelAttachment.ATTR_KEY_CH_ATTACHMENT).set(new ChannelAttachment());
				
			}else {
				f.channel().close();
				bootstrap.group().shutdownGracefully();
				bootstrap = null;
				throw new IllegalStateException("could not bind udp socket, addr assign failed?", f.cause());
			}
		}
	}
	
	@Override
	protected Channel getChannel(RouterInfo routerInfo) {
		
		Channel ch = channel[Math.abs(seq.getAndIncrement()%channel.length)];
		if(log.isDebugEnabled()){
			log.debug("udp muti port client getChannel " + ch.localAddress());
		}
		return ch;
	}
	
	public static void main(String[] args) {
		System.out.println(-1%10);
	}
}

