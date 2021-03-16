package com.guardwarm.framework.loadbalance;

import com.guardwarm.common.extension.SPI;

import java.util.List;

/**
 * 负载均衡策略
 * @author guardWarm
 * @date 2021-03-14 18:17
 */
@SPI
public interface LoadBalance {
	/**
	 * 从存在的服务地址中选一个返回
	 * @param serviceAddresses 服务地址列表
	 * @return target service address 被选中的服务地址
	 */
	String selectServiceAddress(List<String> serviceAddresses, String rpcServiceName);
}
