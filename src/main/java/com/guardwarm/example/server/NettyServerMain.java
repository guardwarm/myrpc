package com.guardwarm.example.server;

import com.guardwarm.api.HelloService;
import com.guardwarm.common.entity.RpcServiceProperties;
import com.guardwarm.example.server.serviceimpl.HelloServiceImpl2;
import com.guardwarm.framework.annotation.RpcScan;
import com.guardwarm.framework.remoting.transport.netty.server.NettyRpcServer;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

/**
 * @author guardWarm
 * @date 2021-03-15 23:33
 */
@RpcScan(basePackage = {"com.guardwarm"})
public class NettyServerMain {
	public static void main(String[] args) {
		// Register service via annotation
		AnnotationConfigApplicationContext applicationContext = new AnnotationConfigApplicationContext(NettyServerMain.class);
		NettyRpcServer nettyRpcServer = (NettyRpcServer) applicationContext.getBean("nettyRpcServer");
		// Register service manually
		HelloService helloService2 = new HelloServiceImpl2();
		RpcServiceProperties rpcServiceProperties = RpcServiceProperties.builder()
				.group("test2").version("version2").build();
		nettyRpcServer.registerService(helloService2, rpcServiceProperties);
		nettyRpcServer.start();
	}
}
