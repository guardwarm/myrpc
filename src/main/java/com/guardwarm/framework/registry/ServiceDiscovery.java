package com.guardwarm.framework.registry;

import com.guardwarm.common.extension.SPI;

import java.net.InetSocketAddress;

/**
 * 服务发现
 * @author guardWarm
 * @date 2021-03-14 16:53
 */
@SPI
public interface ServiceDiscovery {
	/**
	 * 根据服务名寻找可提供服务的服务器
	 *
	 * @param rpcServiceName 服务名
	 * @return 服务器地址
	 */
	InetSocketAddress lookupService(String rpcServiceName);
}
