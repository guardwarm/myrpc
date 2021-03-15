package com.guardwarm.framework.annotation;

import java.lang.annotation.*;

/**
 * RPC reference annotation
 * autowire the service implementation class
 * @author guardWarm
 * @date 2021-03-14 13:17
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD})
@Inherited
public @interface RpcReference {
	/**
	 * Service version, default value is empty string
	 */
	String version() default "";

	/**
	 * Service group, default value is empty string
	 * 同一个接口的不同实例以此区分
	 */
	String group() default "";
}
