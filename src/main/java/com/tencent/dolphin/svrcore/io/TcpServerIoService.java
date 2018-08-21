package com.tencent.dolphin.svrcore.io;

import com.google.inject.Inject;
import com.google.inject.Injector;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;

@Sharable
public class TcpServerIoService extends BaseServerIoService {
	ServerBootstrap bootstrap;
	
	@Inject
	public TcpServerIoService(Injector injector) {
		super(injector);
	}

	@Override
	public synchronized void start() {
		if (bootstrap != null)
			throw new IllegalStateException("could not start more than once");
		this.bootstrap = new ServerBootstrap();
		ChannelFuture f = bootstrap
			.channel(NioServerSocketChannel.class)
			.option(ChannelOption.SO_REUSEADDR, true)
			.group(new NioEventLoopGroup(1), new NioEventLoopGroup())
			.childHandler(new ChannelInitializer<Channel>() {
				protected void initChannel(Channel ch) throws Exception {
					ChannelPipeline p = ch.pipeline();
					p.addLast("e", codec.getEncoder(ch));
					p.addLast("d", codec.getDecoder(ch));
					p.addLast("h", TcpServerIoService.this);
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
