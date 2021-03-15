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
@RpcService(group = "test1", version = "version1")
public class HelloServiceImpl implements HelloService {

	static {
		System.out.println("HelloServiceImpl被创建");
	}

	@Override
	public String hello(Hello hello) {
		log.info("HelloServiceImpl收到: {}.", hello.getMessage());
		String result = "Hello description is " + hello.getDescription();
		log.info("HelloServiceImpl返回: {}.", result);
		return result;
	}
}

