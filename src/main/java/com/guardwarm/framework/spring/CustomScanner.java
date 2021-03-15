package com.guardwarm.framework.spring;

import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.annotation.ClassPathBeanDefinitionScanner;
import org.springframework.core.type.filter.AnnotationTypeFilter;

import java.lang.annotation.Annotation;

/**
 * @author guardWarm
 * @date 2021-03-14 13:35
 */
public class CustomScanner extends ClassPathBeanDefinitionScanner {
	public CustomScanner(BeanDefinitionRegistry registry, Class<? extends Annotation> annoType) {
		super(registry);
		super.addIncludeFilter(new AnnotationTypeFilter(annoType));
	}

	@Override
	public int scan(String... basePackages) {
		return super.scan(basePackages);
	}
}
