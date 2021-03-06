package com.guardwarm.framework.remoting.transport.netty.server;

import com.guardwarm.common.entity.RpcServiceProperties;
import com.guardwarm.common.factory.SingletonFactory;
import com.guardwarm.common.util.RuntimeUtil;
import com.guardwarm.common.util.concurrent.threadpool.ThreadPoolFactoryUtils;
import com.guardwarm.framework.config.CustomShutdownHook;
import com.guardwarm.framework.provider.ServiceProvider;
import com.guardwarm.framework.provider.ServiceProviderImpl;
import com.guardwarm.framework.remoting.transport.netty.codec.RpcMessageDecoder;
import com.guardwarm.framework.remoting.transport.netty.codec.RpcMessageEncoder;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.util.concurrent.DefaultEventExecutorGroup;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.net.InetAddress;
import java.util.concurrent.TimeUnit;

/**
 *  服务端
 *  根据客户端信息调用方法并且返回结果
 * @author guardWarm
 * @date 2021-03-14 16:57
 */
@Slf4j
@Component
public class NettyRpcServer {
	public static final int PORT = 9998;

	private final ServiceProvider serviceProvider
			= SingletonFactory
			.getInstance(ServiceProviderImpl.class);

	public void registerService(Object service, RpcServiceProperties rpcServiceProperties) {
		serviceProvider.publishService(service, rpcServiceProperties);
	}

	@SneakyThrows
	public void start() {
		CustomShutdownHook.getCustomShutdownHook().clearAll();
		String host = InetAddress.getLocalHost().getHostAddress();
		EventLoopGroup bossGroup
				= new NioEventLoopGroup(1);
		EventLoopGroup workerGroup
				= new NioEventLoopGroup();
		DefaultEventExecutorGroup serviceHandlerGroup
				= new DefaultEventExecutorGroup(
				RuntimeUtil.cpus() * 2,
				ThreadPoolFactoryUtils.createThreadFactory("service-handler-group", false)
		);
		try {
			ServerBootstrap b = new ServerBootstrap();
			b.group(bossGroup, workerGroup)
					.channel(NioServerSocketChannel.class)
					// TCP默认开启了 Nagle 算法，TCP_NODELAY 参数的作用就是控制是否启用 Nagle 算法。
					.childOption(ChannelOption.TCP_NODELAY, true)
					// 是否开启 TCP 底层心跳机制
					.childOption(ChannelOption.SO_KEEPALIVE, true)
					// 全连接队列长度：表示系统用于临时存放已完成三次握手的请求的队列的最大长度,
					// 如果连接建立频繁，服务器处理创建新连接较慢，可以适当调大这个参数
					.option(ChannelOption.SO_BACKLOG, 128)
					.handler(new LoggingHandler(LogLevel.INFO))
					// 当客户端第一次进行请求的时候才会进行初始化
					.childHandler(new ChannelInitializer<SocketChannel>() {
						@Override
						protected void initChannel(SocketChannel ch) {
							// 30 秒之内没有收到客户端请求的话就关闭连接
							ChannelPipeline p = ch.pipeline();
							/*
								读空闲时间、写空闲时间、读写空闲时间
								每15秒进行一次读检测
							 */
							p.addLast(new IdleStateHandler(15, 0, 0, TimeUnit.SECONDS));
							/*
								编码
							 */
							p.addLast(new RpcMessageEncoder());
							/*
								解码
							 */
							p.addLast(new RpcMessageDecoder());
							/*
								自定义处理器
							 */
							p.addLast(serviceHandlerGroup, new NettyRpcServerHandler());
						}
					});

			// 绑定端口，同步等待绑定成功
			ChannelFuture f = b.bind(host, PORT).sync();
			// 等待服务端监听端口关闭
			f.channel().closeFuture().sync();
		} catch (InterruptedException e) {
			log.error("occur exception when start server:", e);
		} finally {
			log.error("shutdown bossGroup and workerGroup");
			bossGroup.shutdownGracefully();
			workerGroup.shutdownGracefully();
			serviceHandlerGroup.shutdownGracefully();
		}
	}
}
