package com.guardwarm.common.util;

/**
 * @author guardWarm
 * @date 2021-03-14 10:35
 */
public class RuntimeUtil {
	/**
	 * 获取CPU的核心数
	 * @return cpu的核心数
	 */
	public static int cpus() {
		return Runtime.getRuntime().availableProcessors();
	}
}
