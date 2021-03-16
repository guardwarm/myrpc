package com.guardwarm.framework.loadbalance.loadbalancer;

import com.guardwarm.framework.loadbalance.AbstractLoadBalance;

import java.util.List;
import java.util.Random;

/**
 * 基于随机返回的负载均衡
 * @author guardWarm
 * @date 2021-03-14 19:01
 */
public class RandomLoadBalance extends AbstractLoadBalance {
	@Override
	protected String doSelect(List<String> serviceAddresses, String rpcServiceName) {
		Random random = new Random();
		return serviceAddresses.get(random.nextInt(serviceAddresses.size()));
	}
}
