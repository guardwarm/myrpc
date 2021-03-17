package com.guardwarm.framework.provider;

import com.guardwarm.common.entity.RpcServiceProperties;

/**
 * 服务提供接口
 * @author guardWarm
 * @date 2021-03-14 16:50
 */
public interface ServiceProvider {
	/**
	 * 增加服务
	 * @param service              服务对象
	 * @param serviceClass         服务对象的实现类
	 * @param rpcServiceProperties 服务关联的属性
	 */
	void addService(Object service, Class<?> serviceClass, RpcServiceProperties rpcServiceProperties);

	/**
	 * 获取服务
	 * @param rpcServiceProperties 服务关联的属性
	 * @return service object
	 */
	Object getService(RpcServiceProperties rpcServiceProperties);

	/**
	 * 注册服务
	 * @param service              服务对象
	 * @param rpcServiceProperties 服务关联的属性
	 */
	void publishService(Object service, RpcServiceProperties rpcServiceProperties);

	/**
	 * 注册服务
	 * @param service 服务对象
	 */
	void publishService(Object service);
}
