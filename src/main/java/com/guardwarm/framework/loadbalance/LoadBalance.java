package com.guardwarm.framework.loadbalance;

import com.guardwarm.common.extension.SPI;

import java.util.List;

/**
 * Interface to the load balancing policy
 * 负载均衡策略
 * @author guardWarm
 * @date 2021-03-14 18:17
 */
@SPI
public interface LoadBalance {
	/**
	 * Choose one from the list of existing service addresses list
	 *
	 * @param serviceAddresses Service address list
	 * @return target service address
	 */
	String selectServiceAddress(List<String> serviceAddresses, String rpcServiceName);
}
