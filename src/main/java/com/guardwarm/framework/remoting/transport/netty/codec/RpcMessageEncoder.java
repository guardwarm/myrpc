package com.guardwarm.framework.remoting.transport.netty.codec;

import com.guardwarm.common.enums.CompressTypeEnum;
import com.guardwarm.common.enums.SerializationTypeEnum;
import com.guardwarm.common.extension.ExtensionLoader;
import com.guardwarm.framework.compress.Compress;
import com.guardwarm.framework.remoting.constants.RpcConstants;
import com.guardwarm.framework.remoting.dto.RpcMessage;
import com.guardwarm.framework.serialize.Serializer;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * 编码器
 * @author guardWarm
 * @date 2021-03-14 17:07
 */
@Slf4j
public class RpcMessageEncoder extends MessageToByteEncoder<RpcMessage> {
	private static final AtomicInteger ATOMIC_INTEGER = new AtomicInteger(0);

	/**
	 * 编码--按协议格式封装
	 * @param ctx ChannelHandlerContext
	 * @param rpcMessage 待编码对象
	 * @param out ByteBuf
	 */
	@Override
	protected void encode(ChannelHandlerContext ctx, RpcMessage rpcMessage, ByteBuf out) {
		try {
			out.writeBytes(RpcConstants.MAGIC_NUMBER);
			out.writeByte(RpcConstants.VERSION);
			// leave a place to write the value of full length
			out.writerIndex(out.writerIndex() + 4);
			byte messageType = rpcMessage.getMessageType();
			out.writeByte(messageType);
			out.writeByte(rpcMessage.getCodec());
			out.writeByte(CompressTypeEnum.GZIP.getCode());
			// requestId/responseId
			out.writeInt(ATOMIC_INTEGER.getAndIncrement());
			// build full length
			byte[] bodyBytes = null;
			int fullLength = RpcConstants.HEAD_LENGTH;
			// if messageType is not heartbeat message,fullLength = head length + body length
			if (messageType != RpcConstants.HEARTBEAT_REQUEST_TYPE
					&& messageType != RpcConstants.HEARTBEAT_RESPONSE_TYPE) {
				// serialize the object
				String codecName = SerializationTypeEnum.getName(rpcMessage.getCodec());
				log.info("codec name: [{}] ", codecName);
				Serializer serializer
						= ExtensionLoader
						.getExtensionLoader(Serializer.class)
						.getExtension(codecName);
				bodyBytes = serializer.serialize(rpcMessage.getData());
				// compress the bytes
				String compressName = CompressTypeEnum.getName(rpcMessage.getCompress());
				Compress compress
						= ExtensionLoader
						.getExtensionLoader(Compress.class)
						.getExtension(compressName);
				bodyBytes = compress.compress(bodyBytes);
				fullLength += bodyBytes.length;
			}

			if (bodyBytes != null) {
				out.writeBytes(bodyBytes);
			}
			// 保存当前待写入位置
			int writeIndex = out.writerIndex();
			// 下标移到该写入包长的索引处
			out.writerIndex(writeIndex - fullLength + RpcConstants.MAGIC_NUMBER.length + 1);
			out.writeInt(fullLength);
			out.writerIndex(writeIndex);
		} catch (Exception e) {
			log.error("Encode request error!", e);
		}
	}
}
