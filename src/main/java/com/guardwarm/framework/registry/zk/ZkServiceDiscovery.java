package com.guardwarm.framework.registry.zk;

import com.guardwarm.common.enums.RpcErrorMessageEnum;
import com.guardwarm.common.exception.RpcException;
import com.guardwarm.common.extension.ExtensionLoader;
import com.guardwarm.framework.loadbalance.LoadBalance;
import com.guardwarm.framework.registry.ServiceDiscovery;
import com.guardwarm.framework.registry.zk.util.CuratorUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;

import java.net.InetSocketAddress;
import java.util.List;

/**
 * @author guardWarm
 * @date 2021-03-14 20:56
 */
@Slf4j
public class ZkServiceDiscovery implements ServiceDiscovery {
	private final LoadBalance loadBalance;

	public ZkServiceDiscovery() {
		// 依据SPI确定负载均衡策略
		this.loadBalance = ExtensionLoader.getExtensionLoader(LoadBalance.class).getExtension("loadBalance");
	}

	/**
	 * 获取服务
	 * @param rpcServiceName 服务名
	 * @return 提供该服务的ip：port
	 */
	@Override
	public InetSocketAddress lookupService(String rpcServiceName) {
		CuratorFramework zkClient = CuratorUtils.getZkClient();
		List<String> serviceUrlList = CuratorUtils.getChildrenNodes(zkClient, rpcServiceName);
		if (serviceUrlList == null || serviceUrlList.size() == 0) {
			throw new RpcException(RpcErrorMessageEnum.SERVICE_CAN_NOT_BE_FOUND, rpcServiceName);
		}

		String targetServiceUrl
				= loadBalance.selectServiceAddress(serviceUrlList, rpcServiceName);
		log.info("Successfully found the service address:[{}]", targetServiceUrl);
		String[] socketAddressArray = targetServiceUrl.split(":");
		String host = socketAddressArray[0];
		int port = Integer.parseInt(socketAddressArray[1]);
		return new InetSocketAddress(host, port);
	}
}
