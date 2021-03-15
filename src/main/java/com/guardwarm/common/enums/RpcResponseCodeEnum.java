package com.guardwarm.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

/**
 * rpc响应结果状态码
 * @author guardWarm
 * @date 2021-03-14 11:06
 */
@AllArgsConstructor
@Getter
@ToString
public enum RpcResponseCodeEnum {
	SUCCESS(200, "The remote call is successful"),
	FAIL(500, "The remote call is fail");
	private final int code;
	private final String message;
}
