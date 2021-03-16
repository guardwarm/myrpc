package com.guardwarm.framework.annotation;

import java.lang.annotation.*;

/**
 * RPC reference annotation
 * 自动注入符合的服务实现类
 * @author guardWarm
 * @date 2021-03-14 13:17
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD})
@Inherited
public @interface RpcReference {
	/**
	 * 服务版本
	 */
	String version() default "";

	/**
	 * 服务组，同一个接口的不同实例以此区分
	 */
	String group() default "";
}
