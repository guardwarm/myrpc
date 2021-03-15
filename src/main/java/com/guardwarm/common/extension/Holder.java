package com.guardwarm.common.extension;

/**
 * @author guardWarm
 * @date 2021-03-14 11:13
 */
public class Holder<T> {
	private volatile T value;

	public T get() {
		return value;
	}

	public void set(T value) {
		this.value = value;
	}
}
