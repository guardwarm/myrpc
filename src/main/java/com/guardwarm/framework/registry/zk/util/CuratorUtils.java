package com.guardwarm.framework.registry.zk.util;

import com.guardwarm.common.enums.RpcConfigEnum;
import com.guardwarm.common.util.PropertiesFileUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.imps.CuratorFrameworkState;
import org.apache.curator.framework.recipes.cache.PathChildrenCache;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheListener;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.zookeeper.CreateMode;

import java.net.InetSocketAddress;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * @author guardWarm
 * @date 2021-03-14 17:01
 */
@Slf4j
public class CuratorUtils {
	private static final int BASE_SLEEP_TIME = 1000;
	private static final int MAX_RETRIES = 3;
	public static final String ZK_REGISTER_ROOT_PATH = "/my-rpc";
	private static final Map<String, List<String>> SERVICE_ADDRESS_MAP
			= new ConcurrentHashMap<>();
	private static final Set<String> REGISTERED_PATH_SET
			= ConcurrentHashMap.newKeySet();
	private static CuratorFramework zkClient;
	private static final String DEFAULT_ZOOKEEPER_ADDRESS
			= "127.0.0.1:2181";

	private CuratorUtils() {
	}

	/**
	 * 创建永久节点
	 * 客户端断开连接时，永久节点不会删除
	 *
	 * @param path node path
	 */
	public static void createPersistentNode(CuratorFramework zkClient, String path) {
		try {
			if (REGISTERED_PATH_SET.contains(path) || zkClient.checkExists().forPath(path) != null) {
				log.info("The node already exists. The node is:[{}]", path);
			} else {
				//eg: /my-rpc/com.guardwarm.api.HelloService/127.0.0.1:9999
				zkClient.create().creatingParentsIfNeeded().withMode(CreateMode.PERSISTENT).forPath(path);
				REGISTERED_PATH_SET.add(path);
				log.info("The node was created successfully. The node is:[{}]", path);
			}
		} catch (Exception e) {
			log.error("create persistent node for path [{}] fail", path);
		}
	}

	/**
	 * 获取一个节点下的所有子节点，并监听该节点
	 * @param rpcServiceName 服务名
	 * @return 该服务名下的所有子节点
	 */
	public static List<String> getChildrenNodes(CuratorFramework zkClient, String rpcServiceName) {
		if (SERVICE_ADDRESS_MAP.containsKey(rpcServiceName)) {
			return SERVICE_ADDRESS_MAP.get(rpcServiceName);
		}
		List<String> result = null;
		String servicePath = ZK_REGISTER_ROOT_PATH + "/" + rpcServiceName;
		try {
			result = zkClient.getChildren().forPath(servicePath);
			SERVICE_ADDRESS_MAP.put(rpcServiceName, result);
			registerWatcher(rpcServiceName, zkClient);
		} catch (Exception e) {
			log.error("get children nodes for path [{}] fail", servicePath);
		}
		return result;
	}

	/**
	 * 清除所有已注册的节点信息
	 * @param zkClient zk客户端
	 * @param inetSocketAddress ip:port
	 */
	public static void clearRegistry(CuratorFramework zkClient, InetSocketAddress inetSocketAddress) {
		REGISTERED_PATH_SET.stream().parallel().forEach(p -> {
			try {
				if (p.endsWith(inetSocketAddress.toString())) {
					zkClient.delete().forPath(p);
				}
			} catch (Exception e) {
				log.error("clear registry for path [{}] fail", p);
			}
		});
		log.info("All registered services on the server are cleared:[{}]", REGISTERED_PATH_SET.toString());
	}

	public static CuratorFramework getZkClient() {
		// check if user has set zk address
		Properties properties = PropertiesFileUtil.readPropertiesFile(RpcConfigEnum.RPC_CONFIG_PATH.getPropertyValue());
		// 配置文件未配置时使用默认的
		String zookeeperAddress =
				properties != null && properties.getProperty(RpcConfigEnum.ZK_ADDRESS.getPropertyValue()) != null ? properties.getProperty(RpcConfigEnum.ZK_ADDRESS.getPropertyValue()) : DEFAULT_ZOOKEEPER_ADDRESS;
		// if zkClient has been started, return directly
		if (zkClient != null && zkClient.getState() == CuratorFrameworkState.STARTED) {
			return zkClient;
		}
		// 重试策略：3次, 重试时间将自增
		RetryPolicy retryPolicy = new ExponentialBackoffRetry(BASE_SLEEP_TIME, MAX_RETRIES);
		zkClient = CuratorFrameworkFactory.builder()
				// the server to connect to (can be a server list)
				.connectString(zookeeperAddress)
				.retryPolicy(retryPolicy)
				.build();
		zkClient.start();
		try {
			// 等待30s直到连上zk客户端
			if (!zkClient.blockUntilConnected(30, TimeUnit.SECONDS)) {
				throw new RuntimeException("Time out waiting to connect to ZK!");
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		return zkClient;
	}

	/**
	 * 注册监听某个节点
	 * @param rpcServiceName 服务名 eg:com.guardwarm.api.HelloServicetest2version
	 */
	private static void registerWatcher(String rpcServiceName, CuratorFramework zkClient) throws Exception {
		String servicePath = ZK_REGISTER_ROOT_PATH + "/" + rpcServiceName;
		PathChildrenCache pathChildrenCache = new PathChildrenCache(zkClient, servicePath, true);
		PathChildrenCacheListener pathChildrenCacheListener = (curatorFramework, pathChildrenCacheEvent) -> {
			List<String> serviceAddresses = curatorFramework.getChildren().forPath(servicePath);
			SERVICE_ADDRESS_MAP.put(rpcServiceName, serviceAddresses);
		};
		pathChildrenCache.getListenable()
				.addListener(pathChildrenCacheListener);
		pathChildrenCache.start();
	}
}
