package com.guardwarm.framework.loadbalance.loadbalancer;

import com.guardwarm.framework.loadbalance.AbstartLoadBalance;

import java.util.List;
import java.util.Random;

/**
 * 随机返回一个serviceAddress
 * @author guardWarm
 * @date 2021-03-14 19:01
 */
public class RandomLoadBalance extends AbstartLoadBalance {
	@Override
	protected String doSelect(List<String> serviceAddresses, String rpcServiceName) {
		Random random = new Random();
		return serviceAddresses.get(random.nextInt(serviceAddresses.size()));
	}
}
