package com.guardwarm.example.client;

import com.guardwarm.framework.annotation.RpcScan;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

/**
 * @author guardWarm
 * @date 2021-03-15 23:48
 */
@RpcScan(basePackage = {"com.guardwarm.example.client"})
public class NettyClientMain {
	public static void main(String[] args) throws InterruptedException {
		AnnotationConfigApplicationContext applicationContext = new AnnotationConfigApplicationContext(NettyClientMain.class);
		HelloController helloController = (HelloController) applicationContext.getBean("helloController");
		helloController.test();
	}
}