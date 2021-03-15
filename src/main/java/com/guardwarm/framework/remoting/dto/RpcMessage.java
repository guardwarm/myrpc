package com.guardwarm.framework.remoting.dto;

import lombok.*;

/**
 * @author guardWarm
 * @date 2021-03-14 16:46
 */
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
@ToString
public class RpcMessage {
	//rpc message type
	private byte messageType;
	//serialization type
	private byte codec;
	//compress type
	private byte compress;
	//request id
	private int requestId;
	//request data
	private Object data;
}
