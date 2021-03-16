package com.guardwarm.framework.remoting.transport.socket;

import com.guardwarm.common.entity.RpcServiceProperties;
import com.guardwarm.common.exception.RpcException;
import com.guardwarm.common.extension.ExtensionLoader;
import com.guardwarm.framework.registry.ServiceDiscovery;
import com.guardwarm.framework.remoting.dto.RpcRequest;
import com.guardwarm.framework.remoting.transport.RpcRequestTransport;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;

/**
 * @author guardWarm
 * @date 2021-03-14 22:22
 */
@AllArgsConstructor
@Slf4j
public class SocketRpcClient implements RpcRequestTransport {
	private final ServiceDiscovery serviceDiscovery;

	public SocketRpcClient() {
		this.serviceDiscovery =
				ExtensionLoader.getExtensionLoader(ServiceDiscovery.class).getExtension("zk");
	}

	/**
	 * 发送rpcRequest
	 * @param rpcRequest message body
	 * @return
	 */
	@Override
	public Object sendRpcRequest(RpcRequest rpcRequest) {
		// build rpc service name by rpcRequest
		String rpcServiceName
				= RpcServiceProperties.builder()
				.serviceName(rpcRequest.getInterfaceName())
				.group(rpcRequest.getGroup())
				.version(rpcRequest.getVersion())
				.build().toRpcServiceName();
		InetSocketAddress inetSocketAddress
				= serviceDiscovery
				.lookupService(rpcServiceName);
		try (Socket socket = new Socket()) {
			socket.connect(inetSocketAddress);
			ObjectOutputStream objectOutputStream
					= new ObjectOutputStream(socket.getOutputStream());
			// Send data to the server through the output stream
			objectOutputStream.writeObject(rpcRequest);
			ObjectInputStream objectInputStream
					= new ObjectInputStream(socket.getInputStream());
			// Read RpcResponse from the input stream
			return objectInputStream.readObject();
		} catch (IOException | ClassNotFoundException e) {
			throw new RpcException("调用服务失败:", e);
		}
	}
}
