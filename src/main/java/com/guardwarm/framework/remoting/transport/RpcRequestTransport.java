package com.guardwarm.framework.remoting.transport;

import com.guardwarm.common.extension.SPI;
import com.guardwarm.framework.remoting.dto.RpcRequest;

/**
 * @author guardWarm
 * @date 2021-03-14 22:19
 */
@SPI
public interface RpcRequestTransport {
	/**
	 * 发送rpcRequest到服务器并获得结果
	 * @param rpcRequest 请求
	 * @return 服务器返回的数据
	 */
	Object sendRpcRequest(RpcRequest rpcRequest);
}
