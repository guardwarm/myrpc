package com.guardwarm.framework.remoting.transport.netty.server;

import com.guardwarm.common.enums.CompressTypeEnum;
import com.guardwarm.common.enums.RpcResponseCodeEnum;
import com.guardwarm.common.enums.SerializationTypeEnum;
import com.guardwarm.common.factory.SingletonFactory;
import com.guardwarm.framework.remoting.constants.RpcConstants;
import com.guardwarm.framework.remoting.dto.RpcMessage;
import com.guardwarm.framework.remoting.dto.RpcRequest;
import com.guardwarm.framework.remoting.dto.RpcResponse;
import com.guardwarm.framework.remoting.handler.RpcRequestHandler;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.util.ReferenceCountUtil;
import lombok.extern.slf4j.Slf4j;

/**
 * @author guardWarm
 * @date 2021-03-14 17:08
 */
@Slf4j
public class NettyRpcServerHandler extends ChannelInboundHandlerAdapter {
	private final RpcRequestHandler rpcRequestHandler;

	public NettyRpcServerHandler() {
		this.rpcRequestHandler = SingletonFactory.getInstance(RpcRequestHandler.class);
	}

	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) {
		try {
			if (msg instanceof RpcMessage) {
				log.info("server receive msg: [{}] ", msg);
				byte messageType = ((RpcMessage) msg).getMessageType();
				RpcMessage rpcMessage = new RpcMessage();
				rpcMessage.setCodec(SerializationTypeEnum.PROTOSTUFF.getCode());
				rpcMessage.setCompress(CompressTypeEnum.GZIP.getCode());
				if (messageType == RpcConstants.HEARTBEAT_REQUEST_TYPE) {
					rpcMessage.setMessageType(RpcConstants.HEARTBEAT_RESPONSE_TYPE);
					rpcMessage.setData(RpcConstants.PONG);
				} else {
					RpcRequest rpcRequest = (RpcRequest) ((RpcMessage) msg).getData();
					// Execute the target method (the method the client needs to execute) and return the method result
					Object result = rpcRequestHandler.handle(rpcRequest);
					log.info(String.format("server get result: %s", result.toString()));
					rpcMessage.setMessageType(RpcConstants.RESPONSE_TYPE);
					if (ctx.channel().isActive() && ctx.channel().isWritable()) {
						RpcResponse<Object> rpcResponse = RpcResponse.success(result, rpcRequest.getRequestId());
						rpcMessage.setData(rpcResponse);
					} else {
						RpcResponse<Object> rpcResponse = RpcResponse.fail(RpcResponseCodeEnum.FAIL);
						rpcMessage.setData(rpcResponse);
						log.error("not writable now, message dropped");
					}
				}
				ctx.writeAndFlush(rpcMessage).addListener(ChannelFutureListener.CLOSE_ON_FAILURE);
			}
		} finally {
			//Ensure that ByteBuf is released, otherwise there may be memory leaks
			ReferenceCountUtil.release(msg);
		}
	}

	@Override
	public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
		if (evt instanceof IdleStateEvent) {
			IdleState state = ((IdleStateEvent) evt).state();
			if (state == IdleState.READER_IDLE) {
				log.info("idle check happen, so close the connection");
				ctx.close();
			}
		} else {
			super.userEventTriggered(ctx, evt);
		}
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
		log.error("server catch exception");
		cause.printStackTrace();
		ctx.close();
	}
}
