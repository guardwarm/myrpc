package com.guardwarm.framework.annotation;

import com.guardwarm.framework.spring.CustomScannerRegistrar;
import org.springframework.context.annotation.Import;
import org.springframework.stereotype.Component;

import java.lang.annotation.*;

/**
 * 定制注解扫描的包
 * @author guardWarm
 * @date 2021-03-14 13:31
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Import(CustomScannerRegistrar.class)
@Documented
public @interface RpcScan {
	String[] basePackage();
}
