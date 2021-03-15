package com.guardwarm.framework.loadbalance;

import java.util.List;

/**
 * Abstract class for a load balancing policy
 * @author guardWarm
 * @date 2021-03-14 18:58
 */
public abstract class AbstartLoadBalance implements LoadBalance {
	@Override
	public String selectServiceAddress(List<String> serviceAddresses, String rpcServiceName) {
		if (serviceAddresses == null || serviceAddresses.size() == 0) {
			return null;
		}
		if (serviceAddresses.size() == 1) {
			return serviceAddresses.get(0);
		}
		return doSelect(serviceAddresses, rpcServiceName);
	}

	protected abstract String doSelect(List<String> serviceAddresses, String rpcServiceName);

}
