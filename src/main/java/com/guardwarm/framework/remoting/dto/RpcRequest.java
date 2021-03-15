package com.guardwarm.framework.remoting.dto;

import com.guardwarm.common.entity.RpcServiceProperties;
import lombok.*;

import java.io.Serializable;

/**
 * @author guardWarm
 * @date 2021-03-14 16:42
 */
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Builder
@ToString
public class RpcRequest implements Serializable {
	private static final long serialVersionUID = 1905122041950251207L;
	private String requestId;
	private String interfaceName;
	private String methodName;
	private Object[] parameters;
	private Class<?>[] paramTypes;
	private String version;
	private String group;

	public RpcServiceProperties toRpcProperties() {
		return RpcServiceProperties.builder().serviceName(this.getInterfaceName())
				.version(this.getVersion())
				.group(this.getGroup()).build();
	}
}
