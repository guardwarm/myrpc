package com.guardwarm.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 序列化类型
 * @author guardWarm
 * @date 2021-03-14 10:59
 */
@AllArgsConstructor
@Getter
public enum SerializationTypeEnum {
	KYRO((byte) 0x01, "kyro"),
	PROTOSTUFF((byte) 0x02, "protostuff");;

	private final byte code;
	private final String name;

	public static String getName(byte code) {
		for (SerializationTypeEnum c : SerializationTypeEnum.values()) {
			if (c.getCode() == code) {
				return c.name;
			}
		}
		return null;
	}
}
