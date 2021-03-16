package com.guardwarm.common.exception;

/**
 * 序列化异常
 * @author guardWarm
 * @date 2021-03-14 10:31
 */
public class SerializeException extends RuntimeException {
	public SerializeException(String message) {
		super(message);
	}
}
