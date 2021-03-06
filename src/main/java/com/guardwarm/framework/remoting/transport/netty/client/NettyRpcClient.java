package com.guardwarm.framework.remoting.transport.netty.client;

import com.guardwarm.common.enums.CompressTypeEnum;
import com.guardwarm.common.enums.SerializationTypeEnum;
import com.guardwarm.common.extension.ExtensionLoader;
import com.guardwarm.common.factory.SingletonFactory;
import com.guardwarm.framework.registry.ServiceDiscovery;
import com.guardwarm.framework.remoting.constants.RpcConstants;
import com.guardwarm.framework.remoting.dto.RpcMessage;
import com.guardwarm.framework.remoting.dto.RpcRequest;
import com.guardwarm.framework.remoting.dto.RpcResponse;
import com.guardwarm.framework.remoting.transport.RpcRequestTransport;
import com.guardwarm.framework.remoting.transport.netty.codec.RpcMessageDecoder;
import com.guardwarm.framework.remoting.transport.netty.codec.RpcMessageEncoder;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.timeout.IdleStateHandler;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

/**
 * @author guardWarm
 * @date 2021-03-14 23:05
 */
@Slf4j
public class NettyRpcClient implements RpcRequestTransport {
	private final ServiceDiscovery serviceDiscovery;
	private final UnprocessedRequests unprocessedRequests;
	private final ChannelProvider channelProvider;
	private final Bootstrap bootstrap;
	private final EventLoopGroup eventLoopGroup;

	public NettyRpcClient() {
		// initialize resources such as EventLoopGroup, Bootstrap
		eventLoopGroup = new NioEventLoopGroup();
		bootstrap = new Bootstrap();
		bootstrap.group(eventLoopGroup)
				.channel(NioSocketChannel.class)
				.handler(new LoggingHandler(LogLevel.INFO))
				// 连接超时时间
				.option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 5000)
				.handler(new ChannelInitializer<SocketChannel>() {
					@Override
					protected void initChannel(SocketChannel ch) {
						ChannelPipeline p = ch.pipeline();
						// 每15秒进行一次写检测
						p.addLast(new IdleStateHandler(0, 15, 0, TimeUnit.SECONDS));
						p.addLast(new RpcMessageEncoder());
						p.addLast(new RpcMessageDecoder());
						p.addLast(new NettyRpcClientHandler());
					}
				});
		this.serviceDiscovery
				= ExtensionLoader
				.getExtensionLoader(ServiceDiscovery.class)
				.getExtension("zk");
		this.unprocessedRequests
				= SingletonFactory
				.getInstance(UnprocessedRequests.class);
		this.channelProvider
				= SingletonFactory
				.getInstance(ChannelProvider.class);
	}

	/**
	 * connect server and get the channel ,so that you can send rpc message to server
	 *
	 * @param inetSocketAddress server address
	 * @return the channel
	 */
	@SneakyThrows
	public Channel doConnect(InetSocketAddress inetSocketAddress) {
		CompletableFuture<Channel> completableFuture = new CompletableFuture<>();
		bootstrap.connect(inetSocketAddress).addListener((ChannelFutureListener) future -> {
			if (future.isSuccess()) {
				log.info("The client has connected [{}] successful!", inetSocketAddress.toString());
				completableFuture.complete(future.channel());
			} else {
				throw new IllegalStateException();
			}
		});
		return completableFuture.get();
	}

	@Override
	public Object sendRpcRequest(RpcRequest rpcRequest) {
		// build return value
		CompletableFuture<RpcResponse<Object>> resultFuture = new CompletableFuture<>();
		// build rpc service name by rpcRequest
		String rpcServiceName = rpcRequest.toRpcProperties().toRpcServiceName();
		// get server address
		InetSocketAddress inetSocketAddress = serviceDiscovery.lookupService(rpcServiceName);
		// get  server address related channel
		Channel channel = getChannel(inetSocketAddress);
		if (channel.isActive()) {
			// put unprocessed request
			unprocessedRequests.put(rpcRequest.getRequestId(), resultFuture);
			RpcMessage rpcMessage = new RpcMessage();
			rpcMessage.setData(rpcRequest);
			rpcMessage.setCodec(SerializationTypeEnum.PROTOSTUFF.getCode());
			rpcMessage.setCompress(CompressTypeEnum.GZIP.getCode());
			rpcMessage.setMessageType(RpcConstants.REQUEST_TYPE);
			channel.writeAndFlush(rpcMessage).addListener((ChannelFutureListener) future -> {
				if (future.isSuccess()) {
					log.info("client send message: [{}]", rpcMessage);
				} else {
					future.channel().close();
					resultFuture.completeExceptionally(future.cause());
					log.error("Send failed:", future.cause());
				}
			});
		} else {
			throw new IllegalStateException();
		}

		return resultFuture;
	}

	public Channel getChannel(InetSocketAddress inetSocketAddress) {
		Channel channel = channelProvider.get(inetSocketAddress);
		if (channel == null) {
			channel = doConnect(inetSocketAddress);
			channelProvider.set(inetSocketAddress, channel);
		}
		return channel;
	}

	public void close() {
		eventLoopGroup.shutdownGracefully();
	}

}
