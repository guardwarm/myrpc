package com.guardwarm.framework.annotation;

import org.springframework.stereotype.Component;

import java.lang.annotation.*;

/**
 * RPC service annotation
 * 标记在服务实现类上
 * @author guardWarm
 * @date 2021-03-14 13:37
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
@Inherited
@Component
public @interface RpcService {
	/**
	 * 服务版本
	 */
	String version() default "";

	/**
	 * 服务组，用以区分同一服务的不同实现
	 */
	String group() default "";
}
