package com.guardwarm.framework.remoting.transport.netty.client;

import com.guardwarm.common.enums.CompressTypeEnum;
import com.guardwarm.common.enums.SerializationTypeEnum;
import com.guardwarm.common.factory.SingletonFactory;
import com.guardwarm.framework.remoting.constants.RpcConstants;
import com.guardwarm.framework.remoting.dto.RpcMessage;
import com.guardwarm.framework.remoting.dto.RpcResponse;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.util.ReferenceCountUtil;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;

/**
 * @author guardWarm
 * @date 2021-03-14 23:06
 */
@Slf4j
public class NettyRpcClientHandler extends ChannelInboundHandlerAdapter {
	private final UnprocessedRequests unprocessedRequests;
	private final NettyRpcClient nettyRpcClient;

	public NettyRpcClientHandler() {
		this.unprocessedRequests = SingletonFactory.getInstance(UnprocessedRequests.class);
		this.nettyRpcClient = SingletonFactory.getInstance(NettyRpcClient.class);
	}

	/**
	 * 读取服务端发回的数据
	 */
	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) {
		try {
			log.info("client receive msg: [{}]", msg);
			if (msg instanceof RpcMessage) {
				RpcMessage tmp = (RpcMessage) msg;
				byte messageType = tmp.getMessageType();
				if (messageType == RpcConstants.HEARTBEAT_RESPONSE_TYPE) {
					log.info("heart [{}]", tmp.getData());
				} else if (messageType == RpcConstants.RESPONSE_TYPE) {
					RpcResponse<Object> rpcResponse = (RpcResponse<Object>) tmp.getData();
					unprocessedRequests.complete(rpcResponse);
				}
			}
		} finally {
			ReferenceCountUtil.release(msg);
		}
	}

	/**
	 * 处理心跳机制
	 */
	@Override
	public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
		if (evt instanceof IdleStateEvent) {
			IdleState state = ((IdleStateEvent) evt).state();
			if (state == IdleState.WRITER_IDLE) {
				log.info("write idle happen [{}]", ctx.channel().remoteAddress());
				Channel channel = nettyRpcClient.getChannel((InetSocketAddress) ctx.channel().remoteAddress());
				RpcMessage rpcMessage = new RpcMessage();
				rpcMessage.setCodec(SerializationTypeEnum.PROTOSTUFF.getCode());
				rpcMessage.setCompress(CompressTypeEnum.GZIP.getCode());
				rpcMessage.setMessageType(RpcConstants.HEARTBEAT_REQUEST_TYPE);
				rpcMessage.setData(RpcConstants.PING);
				channel.writeAndFlush(rpcMessage).addListener(ChannelFutureListener.CLOSE_ON_FAILURE);
			}
		} else {
			super.userEventTriggered(ctx, evt);
		}
	}

	/**
	 * 在处理客户消息发生异常时调用
	 */
	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
		log.error("client catch exception：", cause);
		cause.printStackTrace();
		ctx.close();
	}
}
