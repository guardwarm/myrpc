package com.guardwarm.framework.annotation;

/**
 * RPC service annotation
 * marked on the service implementation class
 * @author guardWarm
 * @date 2021-03-14 13:37
 */
public @interface RpcService {
	/**
	 * Service version, default value is empty string
	 */
	String version() default "";

	/**
	 * Service group, default value is empty string
	 * 用以区分同一服务的不同实现
	 */
	String group() default "";
}
