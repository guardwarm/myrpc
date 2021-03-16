package com.guardwarm.framework.spring;

import com.guardwarm.common.entity.RpcServiceProperties;
import com.guardwarm.common.extension.ExtensionLoader;
import com.guardwarm.common.factory.SingletonFactory;
import com.guardwarm.framework.annotation.RpcReference;
import com.guardwarm.framework.annotation.RpcService;
import com.guardwarm.framework.provider.ServiceProvider;
import com.guardwarm.framework.provider.ServiceProviderImpl;
import com.guardwarm.framework.proxy.RpcClientProxy;
import com.guardwarm.framework.remoting.transport.RpcRequestTransport;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;

/**
 * @author guardWarm
 * @date 2021-03-14 23:09
 */
@Slf4j
@Component
public class SpringBeanPostProcessor implements BeanPostProcessor {
	private final ServiceProvider serviceProvider;
	private final RpcRequestTransport rpcClient;

	public SpringBeanPostProcessor() {
		this.serviceProvider = SingletonFactory
				.getInstance(ServiceProviderImpl.class);
		// SPI获取
		this.rpcClient = ExtensionLoader
				.getExtensionLoader(RpcRequestTransport.class)
				.getExtension("socket");
	}

	/**
	 * 初始化之前的后置处理器方法
	 * 处理类上标记了@RpcService.class的类
	 * @param bean bean对象
	 * @param beanName bean名字
	 * @return 新的代理对象
	 * @throws BeansException bean反射注入值发生异常
	 */
	@SneakyThrows
	@Override
	public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
		if (bean.getClass().isAnnotationPresent(RpcService.class)) {
			log.info("[{}] is annotated with  [{}]",
					bean.getClass().getName(),
					RpcService.class.getCanonicalName());
			// get RpcService annotation
			RpcService rpcService = bean.getClass()
					.getAnnotation(RpcService.class);
			// build RpcServiceProperties
			RpcServiceProperties rpcServiceProperties =
					RpcServiceProperties.builder()
							.group(rpcService.group())
							.version(rpcService.version())
							.build();
			serviceProvider.publishService(bean, rpcServiceProperties);
		}
		return bean;
	}

	/**
	 * 初始化之后的后置处理器方法
	 * 处理字段上添加了@RpcReference的类
	 * @param bean bean对象
	 * @param beanName bean名字
	 * @return 新的代理对象
	 * @throws BeansException bean反射注入值发生异常
	 */
	@Override
	public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
		Class<?> targetClass = bean.getClass();
		Field[] declaredFields = targetClass.getDeclaredFields();
		for (Field declaredField : declaredFields) {
			RpcReference rpcReference = declaredField.getAnnotation(RpcReference.class);
			if (rpcReference != null) {
				RpcServiceProperties rpcServiceProperties
						= RpcServiceProperties.builder()
						.group(rpcReference.group())
						.version(rpcReference.version()).build();
				RpcClientProxy rpcClientProxy
						= new RpcClientProxy(rpcClient, rpcServiceProperties);
				Object clientProxy
						= rpcClientProxy.getProxy(declaredField.getType());
				declaredField.setAccessible(true);
				try {
					declaredField.set(bean, clientProxy);
				} catch (IllegalAccessException e) {
					e.printStackTrace();
				}
			}
		}
		return bean;
	}
}
