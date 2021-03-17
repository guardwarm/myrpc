package com.guardwarm.framework.provider;

import com.guardwarm.common.entity.RpcServiceProperties;
import com.guardwarm.common.enums.RpcErrorMessageEnum;
import com.guardwarm.common.exception.RpcException;
import com.guardwarm.common.extension.ExtensionLoader;
import com.guardwarm.framework.registry.ServiceRegistry;
import com.guardwarm.framework.remoting.transport.netty.server.NettyRpcServer;
import lombok.extern.slf4j.Slf4j;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author guardWarm
 * @date 2021-03-14 16:51
 */
@Slf4j
public class ServiceProviderImpl implements ServiceProvider{
	/**
	 * 服务缓存
	 * key: rpc service name(interface name + version + group)
	 * value: service object
	 */
	private final Map<String, Object> serviceMap;
	private final Set<String> registeredService;
	/**
	 * 服务注册方式--根据SPI获取
	 */
	private final ServiceRegistry serviceRegistry;


	public ServiceProviderImpl() {
		serviceMap = new ConcurrentHashMap<>();
		registeredService = ConcurrentHashMap.newKeySet();
		// 依据SPI动态加载类  此时配置的是ZkServiceRegistry
		serviceRegistry
				= ExtensionLoader
				.getExtensionLoader(ServiceRegistry.class)
				.getExtension("zk");
	}

	/**
	 * 增加服务
	 * @param service              service object
	 * @param serviceClass         the interface class implemented by the service instance object
	 * @param rpcServiceProperties service related attributes
	 */
	@Override
	public void addService(Object service, Class<?> serviceClass, RpcServiceProperties rpcServiceProperties) {
		String rpcServiceName = rpcServiceProperties.toRpcServiceName();
		if (registeredService.contains(rpcServiceName)) {
			return;
		}
		registeredService.add(rpcServiceName);
		serviceMap.put(rpcServiceName, service);
		log.info("Add service: {} and interfaces:{}", rpcServiceName, service.getClass().getInterfaces());
	}


	/**
	 * 获取服务
	 * @param rpcServiceProperties service related attributes
	 * @return 获取到的服务
	 * @throws RpcException 获取不到服务时抛出
	 */
	@Override
	public Object getService(RpcServiceProperties rpcServiceProperties) throws RpcException{
		Object service = serviceMap.get(rpcServiceProperties.toRpcServiceName());
		if (null == service) {
			throw new RpcException(RpcErrorMessageEnum.SERVICE_CAN_NOT_BE_FOUND);
		}
		return service;
	}

	/**
	 * 注册服务，默认version和group为""
	 * @param service service object
	 */
	@Override
	public void publishService(Object service) {
		this.publishService(service, RpcServiceProperties.builder().group("").version("").build());
	}

	/**
	 * 注册服务
	 * @param service              service object
	 * @param rpcServiceProperties service related attributes
	 */
	@Override
	public void publishService(Object service, RpcServiceProperties rpcServiceProperties) {
		try {
			String host = InetAddress.getLocalHost().getHostAddress();
			Class<?> serviceRelatedInterface = service.getClass().getInterfaces()[0];
			String serviceName = serviceRelatedInterface.getCanonicalName();
			rpcServiceProperties.setServiceName(serviceName);
			this.addService(service, serviceRelatedInterface, rpcServiceProperties);
			serviceRegistry.registerService(rpcServiceProperties.toRpcServiceName(), new InetSocketAddress(host, NettyRpcServer.PORT));
		} catch (UnknownHostException e) {
			log.error("occur exception when getHostAddress", e);
		}
	}
}
