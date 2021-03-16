package com.guardwarm.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * rpc.properties配置文件信息
 * @author guardWarm
 * @date 2021-03-14 11:10
 */
@AllArgsConstructor
@Getter
public enum RpcConfigEnum {
	RPC_CONFIG_PATH("rpc.properties"),
	ZK_ADDRESS("rpc.zookeeper.address"),
	CONNECTION_WAY("connect_way");

	private final String propertyValue;
}
