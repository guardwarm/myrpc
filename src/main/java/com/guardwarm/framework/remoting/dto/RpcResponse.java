package com.guardwarm.framework.remoting.dto;

import com.guardwarm.common.enums.RpcResponseCodeEnum;
import lombok.*;

import java.io.Serializable;

/**
 * @author guardWarm
 * @date 2021-03-14 16:45
 */
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
@ToString
public class RpcResponse<T> implements Serializable {
	private static final long serialVersionUID = 715745410605631233L;
	private String requestId;
	/**
	 * response code
	 */
	private Integer code;
	/**
	 * response message
	 */
	private String message;
	/**
	 * response body
	 */
	private T data;

	public static <T> RpcResponse<T> success(T data, String requestId) {
		RpcResponse<T> response = new RpcResponse<>();
		response.setCode(RpcResponseCodeEnum.SUCCESS.getCode());
		response.setMessage(RpcResponseCodeEnum.SUCCESS.getMessage());
		response.setRequestId(requestId);
		if (null != data) {
			response.setData(data);
		}
		return response;
	}

	public static <T> RpcResponse<T> fail(RpcResponseCodeEnum rpcResponseCodeEnum) {
		RpcResponse<T> response = new RpcResponse<>();
		response.setCode(rpcResponseCodeEnum.getCode());
		response.setMessage(rpcResponseCodeEnum.getMessage());
		return response;
	}
}
