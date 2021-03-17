package com.guardwarm.framework.registry;

import com.guardwarm.common.extension.SPI;

import java.net.InetSocketAddress;

/**
 * 服务注册
 * @author guardWarm
 * @date 2021-03-14 16:52
 */
@SPI
public interface ServiceRegistry {
	/**
	 * register service
	 *
	 * @param rpcServiceName    rpc service name
	 * @param inetSocketAddress service address
	 */
	void registerService(String rpcServiceName, InetSocketAddress inetSocketAddress);
}
