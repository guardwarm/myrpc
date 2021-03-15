package com.guardwarm.framework.config;

import com.guardwarm.common.util.concurrent.threadpool.ThreadPoolFactoryUtils;
import com.guardwarm.framework.registry.zk.util.CuratorUtils;
import com.guardwarm.framework.remoting.transport.netty.server.NettyRpcServer;
import lombok.extern.slf4j.Slf4j;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;

/**
 * When the server  is closed, do something such as unregister all services
 * 定制shutdown时的钩子
 * @author guardWarm
 * @date 2021-03-14 16:59
 */
@Slf4j
public class CustomShutdownHook {
	// 饿汉模式实现单例
	private static final CustomShutdownHook CUSTOM_SHUTDOWN_HOOK = new CustomShutdownHook();

	public static CustomShutdownHook getCustomShutdownHook() {
		return CUSTOM_SHUTDOWN_HOOK;
	}

	public void clearAll() {
		log.info("addShutdownHook for clearAll");
		Runtime.getRuntime().addShutdownHook(new Thread(() -> {
			try {
				// 清除在zk中注册的信息
				InetSocketAddress inetSocketAddress = new InetSocketAddress(InetAddress.getLocalHost().getHostAddress(), NettyRpcServer.PORT);
				CuratorUtils.clearRegistry(CuratorUtils.getZkClient(), inetSocketAddress);
			} catch (UnknownHostException ignored) {
			}
			ThreadPoolFactoryUtils.shutDownAllThreadPool();
		}));
	}
}
