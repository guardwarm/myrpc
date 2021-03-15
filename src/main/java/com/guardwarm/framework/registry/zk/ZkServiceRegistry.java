package com.guardwarm.framework.registry.zk;

import com.guardwarm.framework.registry.ServiceRegistry;
import com.guardwarm.framework.registry.zk.util.CuratorUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;

import java.net.InetSocketAddress;

/**
 * @author guardWarm
 * @date 2021-03-14 20:58
 */
@Slf4j
public class ZkServiceRegistry implements ServiceRegistry {
	@Override
	public void registerService(String rpcServiceName, InetSocketAddress inetSocketAddress) {
		String servicePath = CuratorUtils.ZK_REGISTER_ROOT_PATH + "/" + rpcServiceName + inetSocketAddress.toString();
		CuratorFramework zkClient = CuratorUtils.getZkClient();
		CuratorUtils.createPersistentNode(zkClient, servicePath);
	}
}
