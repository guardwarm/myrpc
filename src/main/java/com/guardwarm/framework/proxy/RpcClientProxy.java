package com.guardwarm.framework.proxy;

import com.guardwarm.common.entity.RpcServiceProperties;
import com.guardwarm.common.enums.RpcErrorMessageEnum;
import com.guardwarm.common.enums.RpcResponseCodeEnum;
import com.guardwarm.common.exception.RpcException;
import com.guardwarm.framework.remoting.dto.RpcRequest;
import com.guardwarm.framework.remoting.dto.RpcResponse;
import com.guardwarm.framework.remoting.transport.RpcRequestTransport;
import com.guardwarm.framework.remoting.transport.netty.client.NettyRpcClient;
import com.guardwarm.framework.remoting.transport.socket.SocketRpcClient;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * @author guardWarm
 * @date 2021-03-14 22:17
 */
@Slf4j
public class RpcClientProxy implements InvocationHandler {
	private static final String INTERFACE_NAME = "interfaceName";

	/**
	 * 用于发送request的服务.
	 * 有两种实现: socket and netty
	 */
	private final RpcRequestTransport rpcRequestTransport;

	private final RpcServiceProperties rpcServiceProperties;

	public RpcClientProxy(RpcRequestTransport rpcRequestTransport, RpcServiceProperties rpcServiceProperties) {
		this.rpcRequestTransport = rpcRequestTransport;

		// 赋默认值
		if (rpcServiceProperties.getGroup() == null) {
			rpcServiceProperties.setGroup("");
		}
		if (rpcServiceProperties.getVersion() == null) {
			rpcServiceProperties.setVersion("");
		}
		this.rpcServiceProperties = rpcServiceProperties;
	}


	public RpcClientProxy(RpcRequestTransport rpcRequestTransport) {
		this.rpcRequestTransport = rpcRequestTransport;
		this.rpcServiceProperties
				= RpcServiceProperties.builder().group("").version("").build();
	}


	@SuppressWarnings("unchecked")
	public <T> T getProxy(Class<T> clazz) {
		return (T) Proxy.newProxyInstance(clazz.getClassLoader(), new Class<?>[]{clazz}, this);
	}

	/**
	 * 使用代理对象调用方法时，实际上会调用此方法。
	 * 代理对象是您通过getProxy方法获得的对象。
	 */
	@SneakyThrows
	@SuppressWarnings("unchecked")
	@Override
	public Object invoke(Object proxy, Method method, Object[] args) {
		log.info("invoked method: [{}]", method.getName());
		RpcRequest rpcRequest = RpcRequest.builder()
				.methodName(method.getName())
				.parameters(args)
				.interfaceName(method.getDeclaringClass().getName())
				.paramTypes(method.getParameterTypes())
				// java.util包下的
				.requestId(UUID.randomUUID().toString())
				.group(rpcServiceProperties.getGroup())
				.version(rpcServiceProperties.getVersion())
				.build();
		RpcResponse<Object> rpcResponse = null;
		if (rpcRequestTransport instanceof NettyRpcClient) {
			CompletableFuture<RpcResponse<Object>> completableFuture
					= (CompletableFuture<RpcResponse<Object>>) rpcRequestTransport
					.sendRpcRequest(rpcRequest);
			// 等待必要的时间以完成此将来，然后返回其结果
			rpcResponse = completableFuture.get();
		}
		if (rpcRequestTransport instanceof SocketRpcClient) {
			rpcResponse = (RpcResponse<Object>) rpcRequestTransport.sendRpcRequest(rpcRequest);
		}
		this.check(rpcResponse, rpcRequest);
		return rpcResponse.getData();
	}

	/**
	 * 检验响应是否正确
	 * @param rpcResponse 响应对象
	 * @param rpcRequest 请求对象
	 */
	private void check(RpcResponse<Object> rpcResponse, RpcRequest rpcRequest) {
		// 响应为空
		if (rpcResponse == null) {
			throw new RpcException(RpcErrorMessageEnum.SERVICE_INVOCATION_FAILURE, INTERFACE_NAME + ":" + rpcRequest.getInterfaceName());
		}
		// RequestId请求的和响应的对不上
		if (!rpcRequest.getRequestId().equals(rpcResponse.getRequestId())) {
			throw new RpcException(RpcErrorMessageEnum.REQUEST_NOT_MATCH_RESPONSE, INTERFACE_NAME + ":" + rpcRequest.getInterfaceName());
		}
		// 不是成功的响应
		if (rpcResponse.getCode() == null || !rpcResponse.getCode().equals(RpcResponseCodeEnum.SUCCESS.getCode())) {
			throw new RpcException(RpcErrorMessageEnum.SERVICE_INVOCATION_FAILURE, INTERFACE_NAME + ":" + rpcRequest.getInterfaceName());
		}
	}
}
