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
	 * send rpc request to server and get result
	 *
	 * @param rpcRequest message body
	 * @return data from server
	 */
	Object sendRpcRequest(RpcRequest rpcRequest);
}
