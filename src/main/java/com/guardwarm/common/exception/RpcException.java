package com.guardwarm.common.exception;

import com.guardwarm.common.enums.RpcErrorMessageEnum;

/**
 * RPC操作异常
 * @author guardWarm
 * @date 2021-03-14 10:30
 */
public class RpcException extends RuntimeException {
	public RpcException(String message, Throwable cause) {
		super(message, cause);
	}

	public RpcException(RpcErrorMessageEnum rpcErrorMessageEnum) {
		super(rpcErrorMessageEnum.getMessage());
	}

	public RpcException(RpcErrorMessageEnum rpcErrorMessageEnum, String detail) {
		super(rpcErrorMessageEnum.getMessage() + ":" + detail);
	}
}
