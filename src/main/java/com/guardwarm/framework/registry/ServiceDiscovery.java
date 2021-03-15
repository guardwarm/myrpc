package com.guardwarm.framework.registry;

import com.guardwarm.common.extension.SPI;

import java.net.InetSocketAddress;

/**
 * @author guardWarm
 * @date 2021-03-14 16:53
 */
@SPI
public interface ServiceDiscovery {
	/**
	 * lookup service by rpcServiceName
	 *
	 * @param rpcServiceName rpc service name
	 * @return service address
	 */
	InetSocketAddress lookupService(String rpcServiceName);
}
