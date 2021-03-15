package com.guardwarm.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 支持的压缩类型
 * @author guardWarm
 * @date 2021-03-14 11:11
 */
@AllArgsConstructor
@Getter
public enum CompressTypeEnum {
	GZIP((byte) 0x01, "gzip");

	private final byte code;
	private final String name;

	/**
	 * 获取code对应name
	 * @param code 代码
	 * @return 对应的name
	 */
	public static String getName(byte code) {
		for (CompressTypeEnum c : CompressTypeEnum.values()) {
			if (c.getCode() == code) {
				return c.name;
			}
		}
		return null;
	}
}
