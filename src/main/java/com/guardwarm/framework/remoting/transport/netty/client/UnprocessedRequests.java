package com.guardwarm.framework.remoting.transport.netty.client;

import com.guardwarm.framework.remoting.dto.RpcResponse;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 未完成的响应
 * @author guardWarm
 * @date 2021-03-14 23:07
 */
public class UnprocessedRequests {
	private static final Map<String, CompletableFuture<RpcResponse<Object>>>
			UNPROCESSED_RESPONSE_FUTURES = new ConcurrentHashMap<>();

	public void put(String requestId, CompletableFuture<RpcResponse<Object>> future) {
		UNPROCESSED_RESPONSE_FUTURES.put(requestId, future);
	}

	public void complete(RpcResponse<Object> rpcResponse) {
		CompletableFuture<RpcResponse<Object>> future = UNPROCESSED_RESPONSE_FUTURES.remove(rpcResponse.getRequestId());
		if (null != future) {
			future.complete(rpcResponse);
		} else {
			throw new IllegalStateException();
		}
	}
}
