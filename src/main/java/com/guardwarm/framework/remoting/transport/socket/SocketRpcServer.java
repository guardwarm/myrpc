package com.guardwarm.framework.remoting.transport.socket;

import com.guardwarm.common.entity.RpcServiceProperties;
import com.guardwarm.common.factory.SingletonFactory;
import com.guardwarm.common.util.concurrent.threadpool.ThreadPoolFactoryUtils;
import com.guardwarm.framework.config.CustomShutdownHook;
import com.guardwarm.framework.provider.ServiceProvider;
import com.guardwarm.framework.provider.ServiceProviderImpl;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;

import static com.guardwarm.framework.remoting.transport.netty.server.NettyRpcServer.PORT;

/**
 * @author guardWarm
 * @date 2021-03-14 22:23
 */
@Slf4j
public class SocketRpcServer {
	private final ExecutorService threadPool;
	private final ServiceProvider serviceProvider;


	public SocketRpcServer() {
		threadPool = ThreadPoolFactoryUtils.createCustomThreadPoolIfAbsent("socket-server-rpc-pool");
		serviceProvider = SingletonFactory.getInstance(ServiceProviderImpl.class);
	}

	public void registerService(Object service) {
		serviceProvider.publishService(service);
	}

	public void registerService(Object service, RpcServiceProperties rpcServiceProperties) {
		serviceProvider.publishService(service, rpcServiceProperties);
	}

	public void start() {
		try (ServerSocket server = new ServerSocket()) {
			String host = InetAddress.getLocalHost().getHostAddress();
			server.bind(new InetSocketAddress(host, PORT));
			CustomShutdownHook.getCustomShutdownHook().clearAll();
			Socket socket;
			while ((socket = server.accept()) != null) {
				log.info("client connected [{}]", socket.getInetAddress());
				threadPool.execute(new SocketRpcRequestHandlerRunnable(socket));
			}
			threadPool.shutdown();
		} catch (IOException e) {
			log.error("occur IOException:", e);
		}
	}
}
