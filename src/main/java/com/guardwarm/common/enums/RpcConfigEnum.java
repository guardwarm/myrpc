package com.guardwarm.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author guardWarm
 * @date 2021-03-14 11:10
 */
@AllArgsConstructor
@Getter
public enum RpcConfigEnum {
	RPC_CONFIG_PATH("rpc.properties"),
	ZK_ADDRESS("rpc.zookeeper.address");

	private final String propertyValue;
}
