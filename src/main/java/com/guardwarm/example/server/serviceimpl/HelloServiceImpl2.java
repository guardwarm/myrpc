package com.guardwarm.example.server.serviceimpl;

import com.guardwarm.api.Hello;
import com.guardwarm.api.HelloService;
import com.guardwarm.framework.annotation.RpcService;
import lombok.extern.slf4j.Slf4j;

/**
 * @author guardWarm
 * @date 2021-03-14 23:50
 */
@Slf4j
@RpcService(group = "test2", version = "version2")
public class HelloServiceImpl2 implements HelloService {

	static {
		System.out.println("HelloServiceImpl2被创建");
	}

	@Override
	public String hello(Hello hello) {
		log.info("HelloServiceImpl2收到: {}.", hello.getMessage());
		String result = "Hello description is " + hello.getDescription();
		log.info("HelloServiceImpl2返回: {}.", result);
		return result;
	}
}

