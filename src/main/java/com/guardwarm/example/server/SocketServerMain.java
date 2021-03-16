package com.guardwarm.example.server;

import com.guardwarm.api.HelloService;
import com.guardwarm.common.entity.RpcServiceProperties;
import com.guardwarm.example.server.serviceimpl.HelloServiceImpl;
import com.guardwarm.framework.annotation.RpcScan;
import com.guardwarm.framework.remoting.transport.socket.SocketRpcServer;
import org.springframework.stereotype.Component;

/**
 * @author guardWarm
 * @date 2021-03-14 23:48
 */
@RpcScan(basePackage = {"com.guardwarm.example.server"})
public class SocketServerMain {
	public static void main(String[] args) {
		HelloService helloService = new HelloServiceImpl();
		SocketRpcServer socketRpcServer = new SocketRpcServer();
		RpcServiceProperties rpcServiceProperties = RpcServiceProperties.builder()
				.group("test3").version("version3").build();
		socketRpcServer.registerService(helloService, rpcServiceProperties);
		socketRpcServer.start();
	}
}
