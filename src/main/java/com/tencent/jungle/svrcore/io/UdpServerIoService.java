package com.tencent.jungle.svrcore.io;

import com.google.inject.Inject;
import com.google.inject.Injector;
import com.tencent.jungle.svrcore.comm.UdpDecoderAdapter;
import com.tencent.jungle.svrcore.comm.UdpEncoderAdapter;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.epoll.EpollChannelOption;
import io.netty.channel.epoll.EpollDatagramChannel;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioDatagramChannel;

import java.util.Properties;

@Sharable
public class UdpServerIoService extends BaseServerIoService {
	Bootstrap bootstrap;
	protected int recvBufferSize = 8096;
	
	@Inject
	public UdpServerIoService(Injector injector) {
		super(injector);
	}
	
	public void setRecvBufferSize(int recvBufferSize) {
		this.recvBufferSize = recvBufferSize;
	}

	@Override
	public synchronized void start() {
		if (bootstrap != null)
			throw new IllegalStateException("could not start more than once");
		
		this.bootstrap = new Bootstrap();
		
		Properties props = System.getProperties();
		String osName = props.getProperty("os.name");
		String osVer = props.getProperty("os.version");
		System.err.println("os.version " + osVer);
		if(osName != null && osName.startsWith("Linux") && osVer.split("-")[0].compareTo("3.9") >= 0){ //Linux: 3.9以上支持特性
			 bootstrap
				.channel(EpollDatagramChannel.class)
				.option(EpollChannelOption.SO_REUSEPORT,true)
				.option(ChannelOption.SO_REUSEADDR, true)
				.option(ChannelOption.RCVBUF_ALLOCATOR, new FixedRecvByteBufAllocator(recvBufferSize))
				.group(new EpollEventLoopGroup())
				.handler(new ChannelInitializer<Channel>() {
					protected void initChannel(Channel ch) throws Exception {
						ChannelPipeline p = ch.pipeline();
						p.addLast("e", new UdpEncoderAdapter(codec.getEncoder(ch)));
						p.addLast("d", new UdpDecoderAdapter(codec.getDecoder(ch)));
						p.addLast("h", UdpServerIoService.this);
					};
				});
			
			 int workerThreads = Runtime.getRuntime().availableProcessors();
	         for (int i = 0; i < workerThreads; ++i) {
	              ChannelFuture future = null;
				try {
					future = bootstrap.bind(bindAddr).await();
				} catch (InterruptedException e) {
					Thread.currentThread().interrupt();
				}
	           if (!future.isSuccess()){
	        	   throw new IllegalStateException("bind failed, addr has been used? " + bindAddr, future.cause());
	           }
	        }
		}else{
			final NioEventLoopGroup workerGroup = new NioEventLoopGroup();
			ChannelFuture f = bootstrap
				.channel(NioDatagramChannel.class)
				.group(new NioEventLoopGroup(1))
				.option(ChannelOption.SO_REUSEADDR, true)
				.option(ChannelOption.RCVBUF_ALLOCATOR, new FixedRecvByteBufAllocator(recvBufferSize))
				.handler(new ChannelInitializer<Channel>() {
					protected void initChannel(Channel ch) throws Exception {
						ChannelPipeline p = ch.pipeline();
						p.addLast(workerGroup,"e", new UdpEncoderAdapter(codec.getEncoder(ch)));
						p.addLast(workerGroup,"d", new UdpDecoderAdapter(codec.getDecoder(ch)));
						p.addLast(workerGroup,"h", UdpServerIoService.this);
					};
				}).bind(bindAddr);
			
			try {
				f.await();
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
			}
			
			if (!f.isSuccess())
				throw new IllegalStateException("bind failed, addr has been used? " + bindAddr, f.cause());
		}
	}
	
}
