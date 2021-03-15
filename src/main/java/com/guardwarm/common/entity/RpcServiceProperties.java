package com.guardwarm.common.entity;

import lombok.*;

/**
 * 协议格式 ≈ 协议头
 * @author guardWarm
 * @date 2021-03-14 11:38
 */
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
@ToString
public class RpcServiceProperties {
	/**
	 * service version
	 */
	private String version;
	/**
	 * when the interface has multiple implementation classes, distinguish by group
	 */
	private String group;
	private String serviceName;

	public String toRpcServiceName() {
		return this.getServiceName()
				+ this.getGroup()
				+ this.getVersion();
	}
}
