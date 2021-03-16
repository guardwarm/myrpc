package com.guardwarm.example.client;

import com.guardwarm.api.Hello;
import com.guardwarm.api.HelloService;
import com.guardwarm.common.entity.RpcServiceProperties;
import com.guardwarm.framework.annotation.RpcReference;
import com.guardwarm.framework.annotation.RpcScan;
import com.guardwarm.framework.proxy.RpcClientProxy;
import com.guardwarm.framework.remoting.transport.RpcRequestTransport;
import com.guardwarm.framework.remoting.transport.socket.SocketRpcClient;
import org.springframework.stereotype.Component;

/**
 * socket连接测试类
 * @author guardWarm
 * @date 2021-03-14 23:51
 */
@RpcScan(basePackage = {"com.guardwarm.example.client"})
@Component
public class SocketClientMain {

	@RpcReference(group = "version1", version = "version1")
	private static HelloService h2;
	public static void main(String[] args) {
		RpcRequestTransport rpcRequestTransport = new SocketRpcClient();
		RpcServiceProperties rpcServiceProperties = RpcServiceProperties.builder()
				.group("test1").version("version1").build();
		RpcClientProxy rpcClientProxy = new RpcClientProxy(rpcRequestTransport, rpcServiceProperties);
		HelloService helloService = rpcClientProxy.getProxy(HelloService.class);
		String hello = helloService.hello(new Hello("你好", "今天状态如何"));
		System.out.println(h2.hello(new Hello("aaa", "ccc")));
		System.out.println(hello);
	}
}
