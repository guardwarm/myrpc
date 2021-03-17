package com.guardwarm.framework.remoting.handler;

import com.guardwarm.common.exception.RpcException;
import com.guardwarm.common.factory.SingletonFactory;
import com.guardwarm.framework.provider.ServiceProvider;
import com.guardwarm.framework.provider.ServiceProviderImpl;
import com.guardwarm.framework.remoting.dto.RpcRequest;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * @author guardWarm
 * @date 2021-03-14 16:48
 */
@Slf4j
public class RpcRequestHandler {
	private final ServiceProvider serviceProvider;

	public RpcRequestHandler() {
		serviceProvider
				= SingletonFactory.getInstance(ServiceProviderImpl.class);
	}

	/**
	 * 处理rpcRequest：调用相应的方法，返回执行结果
	 */
	public Object handle(RpcRequest rpcRequest) {
		Object service = serviceProvider.getService(rpcRequest.toRpcProperties());
		return invokeTargetMethod(rpcRequest, service);
	}

	/**
	 * 获取方法执行结果
	 * @param rpcRequest 请求
	 * @param service    服务
	 * @return 方法执行结果
	 */
	private Object invokeTargetMethod(RpcRequest rpcRequest, Object service) {
		Object result;
		try {
			Method method = service.getClass()
					.getMethod(rpcRequest.getMethodName(), rpcRequest.getParamTypes());
			result = method.invoke(service, rpcRequest.getParameters());
			log.info("service:[{}] successful invoke method:[{}]", rpcRequest.getInterfaceName(), rpcRequest.getMethodName());
		} catch (NoSuchMethodException | IllegalArgumentException | InvocationTargetException | IllegalAccessException e) {
			throw new RpcException(e.getMessage(), e);
		}
		return result;
	}
}
