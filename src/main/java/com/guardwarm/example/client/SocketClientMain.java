package com.guardwarm.example.client;

import com.guardwarm.api.Hello;
import com.guardwarm.api.HelloService;
import com.guardwarm.common.entity.RpcServiceProperties;
import com.guardwarm.framework.proxy.RpcClientProxy;
import com.guardwarm.framework.remoting.transport.RpcRequestTransport;
import com.guardwarm.framework.remoting.transport.socket.SocketRpcClient;

/**
 * socket连接测试类
 * @author guardWarm
 * @date 2021-03-14 23:51
 */
public class SocketClientMain {

	public static void main(String[] args) {
		RpcRequestTransport rpcRequestTransport = new SocketRpcClient();
		RpcServiceProperties rpcServiceProperties = RpcServiceProperties.builder()
				.group("test3").version("version3").build();
		RpcClientProxy rpcClientProxy = new RpcClientProxy(rpcRequestTransport, rpcServiceProperties);
		HelloService helloService = rpcClientProxy.getProxy(HelloService.class);
		String hello = helloService.hello(new Hello("你好", "今天状态如何"));
		System.out.println(hello);
	}
}
