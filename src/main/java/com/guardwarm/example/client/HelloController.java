package com.guardwarm.example.client;

import com.guardwarm.api.Hello;
import com.guardwarm.api.HelloService;
import com.guardwarm.framework.annotation.RpcReference;
import org.springframework.stereotype.Component;

/**
 * 测试通过注解消费服务
 * @author guardWarm
 * @date 2021-03-15 23:49
 */
@Component
public class HelloController {

	@RpcReference(version = "version1", group = "test1")
	private HelloService helloService;

	public void test() throws InterruptedException {
		String hello = this.helloService.hello(new Hello("111", "222"));
		//如需使用 assert 断言，需要在 VM options 添加参数：-ea
		assert "Hello description is 222".equals(hello);
		Thread.sleep(12000);
		for (int i = 0; i < 10; i++) {
			System.out.println(helloService.hello(new Hello("111", "222")));
		}
	}
}
