package com.guardwarm.framework.remoting.transport.netty.codec;

import com.guardwarm.common.enums.CompressTypeEnum;
import com.guardwarm.common.enums.SerializationTypeEnum;
import com.guardwarm.common.extension.ExtensionLoader;
import com.guardwarm.framework.compress.Compress;
import com.guardwarm.framework.remoting.constants.RpcConstants;
import com.guardwarm.framework.remoting.dto.RpcMessage;
import com.guardwarm.framework.remoting.dto.RpcRequest;
import com.guardwarm.framework.remoting.dto.RpcResponse;
import com.guardwarm.framework.serialize.Serializer;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import lombok.extern.slf4j.Slf4j;

import java.util.Arrays;

/**
 * 解码器--基于length field in the message
 * @author guardWarm
 * @date 2021-03-14 17:04
 */
@Slf4j
public class RpcMessageDecoder extends LengthFieldBasedFrameDecoder {
	public RpcMessageDecoder() {
		// lengthFieldOffset: magic code is 4B, and version is 1B ==> value is 5
		// lengthFieldLength: 长度字段使用4B ==> value is 4
		// lengthAdjustment: 全长包括所有数据，需跳过前九个字节 ==> values is -9
		// initialBytesToStrip: 我们将手动检查magic code和version ==> do not strip any bytes ==> values is 0
		this(RpcConstants.MAX_FRAME_LENGTH, 5, 4, -9, 0);
	}

	/**
	 * @param maxFrameLength      最大帧长。它决定了可以接收的最大数据长度。如果超过，数据将被丢弃.
	 * @param lengthFieldOffset   长度字段偏移量。长度字段是跳过指定字节长度的字段。
	 * @param lengthFieldLength   长度字段中的字节数。
	 * @param lengthAdjustment    补偿值添加到长度字段的值，使长度字段变为实际数据长度
	 * @param initialBytesToStrip 跳过的字节数。
	 *                            如果您需要接收所有标头+正文数据，则此值为0
	 *                            如果只想接收正文数据，则需要跳过标头消耗的字节数。
	 */
	public RpcMessageDecoder(int maxFrameLength,
	                         int lengthFieldOffset, int lengthFieldLength,
	                         int lengthAdjustment, int initialBytesToStrip) {
		super(maxFrameLength, lengthFieldOffset, lengthFieldLength, lengthAdjustment, initialBytesToStrip);
	}

	/**
	 * 解码
	 * @param ctx ChannelHandlerContext
	 * @param in ByteBuf
	 * @return 解码后的对象
	 * @throws Exception Decode frame error
	 */
	@Override
	protected Object decode(ChannelHandlerContext ctx, ByteBuf in) throws Exception {
		Object decoded = super.decode(ctx, in);
		if (decoded instanceof ByteBuf) {
			ByteBuf frame = (ByteBuf) decoded;
			if (frame.readableBytes() >= RpcConstants.TOTAL_LENGTH) {
				try {
					return decodeFrame(frame);
				} catch (Exception e) {
					log.error("Decode frame error!", e);
					throw e;
				} finally {
					frame.release();
				}
			}

		}
		return decoded;
	}


	/**
	 * 根据协议格式解码
	 * @param in ByteBuf
	 * @return 解码后的rpcMessage
	 */
	private Object decodeFrame(ByteBuf in) {
		// note: must read ByteBuf in order
		checkMagicNumber(in); // 4B
		checkVersion(in); // 1B
		// 包长
		int fullLength = in.readInt(); // 4B
		// build RpcMessage object
		byte messageType = in.readByte(); // 1B
		byte codecType = in.readByte();  // 1B
		byte compressType = in.readByte();  // 1B
		int requestId = in.readInt();  // 4B
		RpcMessage rpcMessage
				= RpcMessage.builder()
				.codec(codecType)
				.requestId(requestId)
				.messageType(messageType).build();
		if (messageType == RpcConstants.HEARTBEAT_REQUEST_TYPE) {
			rpcMessage.setData(RpcConstants.PING);
			return rpcMessage;
		}
		if (messageType == RpcConstants.HEARTBEAT_RESPONSE_TYPE) {
			rpcMessage.setData(RpcConstants.PONG);
			return rpcMessage;
		}
		int bodyLength = fullLength - RpcConstants.HEAD_LENGTH;
		if (bodyLength > 0) {
			byte[] bs = new byte[bodyLength];
			in.readBytes(bs);
			// 解压缩
			String compressName = CompressTypeEnum.getName(compressType);
			Compress compress = ExtensionLoader.getExtensionLoader(Compress.class)
					.getExtension(compressName);
			bs = compress.decompress(bs);
			// 反序列化
			String codecName = SerializationTypeEnum.getName(rpcMessage.getCodec());
			log.info("codec name: [{}] ", codecName);
			Serializer serializer
					= ExtensionLoader
					.getExtensionLoader(Serializer.class)
					.getExtension(codecName);
			if (messageType == RpcConstants.REQUEST_TYPE) {
				RpcRequest tmpValue = serializer.deserialize(bs, RpcRequest.class);
				rpcMessage.setData(tmpValue);
			} else {
				RpcResponse tmpValue = serializer.deserialize(bs, RpcResponse.class);
				rpcMessage.setData(tmpValue);
			}
		}
		return rpcMessage;

	}

	/**
	 * 检查Version是否正确
	 * @param in 待验证ByteBuf
	 */
	private void checkVersion(ByteBuf in) {
		// read the version and compare
		byte version = in.readByte();
		if (version != RpcConstants.VERSION) {
			throw new RuntimeException("version isn't compatible" + version);
		}
	}

	/**
	 * MagicNumber是否正确
	 * @param in 待验证ByteBuf
	 */
	private void checkMagicNumber(ByteBuf in) {
		// read the first 4 bit, which is the magic number, and compare
		int len = RpcConstants.MAGIC_NUMBER.length;
		byte[] tmp = new byte[len];
		in.readBytes(tmp);
		for (int i = 0; i < len; i++) {
			if (tmp[i] != RpcConstants.MAGIC_NUMBER[i]) {
				throw new IllegalArgumentException("Unknown magic code: " + Arrays.toString(tmp));
			}
		}
	}
}
