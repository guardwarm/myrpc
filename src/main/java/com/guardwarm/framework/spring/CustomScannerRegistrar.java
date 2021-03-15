package com.guardwarm.framework.spring;

import com.guardwarm.framework.annotation.RpcScan;
import com.guardwarm.framework.annotation.RpcService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.ResourceLoaderAware;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.core.type.StandardAnnotationMetadata;
import org.springframework.stereotype.Component;

/**
 * @author guardWarm
 * @date 2021-03-14 13:33
 */
@Slf4j
public class CustomScannerRegistrar implements ImportBeanDefinitionRegistrar, ResourceLoaderAware {

	private static final String SPRING_BEAN_BASE_PACKAGE = "com.guardwarm.framework.spring";
	// 对应RpcScan注解中定义的属性
	private static final String BASE_PACKAGE_ATTRIBUTE_NAME = "basePackage";
	private ResourceLoader resourceLoader;

	@Override
	public void setResourceLoader(ResourceLoader resourceLoader) {
		this.resourceLoader = resourceLoader;

	}

	@Override
	public void registerBeanDefinitions(AnnotationMetadata annotationMetadata, BeanDefinitionRegistry beanDefinitionRegistry) {
		//get the attributes and values of RpcScan annotation
		AnnotationAttributes rpcScanAnnotationAttributes
				= AnnotationAttributes.fromMap(annotationMetadata.getAnnotationAttributes(RpcScan.class.getName()));
		String[] rpcScanBasePackages = new String[0];
		if (rpcScanAnnotationAttributes != null) {
			// get the value of the basePackage property
			rpcScanBasePackages = rpcScanAnnotationAttributes.getStringArray(BASE_PACKAGE_ATTRIBUTE_NAME);
		}
		if (rpcScanBasePackages.length == 0) {
			rpcScanBasePackages = new String[]{((StandardAnnotationMetadata) annotationMetadata).getIntrospectedClass().getPackage().getName()};
		}
		// Scan the RpcService annotation
		CustomScanner rpcServiceScanner = new CustomScanner(beanDefinitionRegistry, RpcService.class);
		// Scan the Component annotation
		CustomScanner springBeanScanner = new CustomScanner(beanDefinitionRegistry, Component.class);
		if (resourceLoader != null) {
			rpcServiceScanner.setResourceLoader(resourceLoader);
			springBeanScanner.setResourceLoader(resourceLoader);
		}
		int springBeanAmount = springBeanScanner.scan(SPRING_BEAN_BASE_PACKAGE);
		log.info("springBeanScanner扫描的数量 [{}]", springBeanAmount);
		int rpcServiceCount = rpcServiceScanner.scan(rpcScanBasePackages);
		log.info("rpcServiceScanner扫描的数量 [{}]", rpcServiceCount);

	}
}

