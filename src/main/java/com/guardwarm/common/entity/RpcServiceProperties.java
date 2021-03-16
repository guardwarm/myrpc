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
	 * 服务版本
	 */
	private String version;
	/**
	 * 服务所属组（用于区分同一服务的不同实现）
	 */
	private String group;
	/**
	 * 服务名称
	 */
	private String serviceName;

	public String toRpcServiceName() {
		return this.getServiceName()
				+ this.getGroup()
				+ this.getVersion();
	}
}
