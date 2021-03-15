package com.guardwarm.framework.serialize.protostuff;

import com.guardwarm.framework.serialize.Serializer;
import io.protostuff.LinkedBuffer;
import io.protostuff.ProtostuffIOUtil;
import io.protostuff.Schema;
import io.protostuff.runtime.RuntimeSchema;

/**
 * @author guardWarm
 * @date 2021-03-14 22:13
 */
public class ProtostuffSerializer implements Serializer {
	/**
	 * Avoid re applying buffer space every time serialization
	 */
	private static final LinkedBuffer BUFFER
			= LinkedBuffer.allocate(LinkedBuffer.DEFAULT_BUFFER_SIZE);

	@Override
	public byte[] serialize(Object obj) {
		Class<?> clazz = obj.getClass();
		Schema schema = RuntimeSchema.getSchema(clazz);
		byte[] bytes;
		try {
			bytes = ProtostuffIOUtil.toByteArray(obj, schema, BUFFER);
		} finally {
			BUFFER.clear();
		}
		return bytes;
	}

	@Override
	public <T> T deserialize(byte[] bytes, Class<T> clazz) {
		Schema<T> schema = RuntimeSchema.getSchema(clazz);
		T obj = schema.newMessage();
		ProtostuffIOUtil.mergeFrom(bytes, obj, schema);
		return obj;
	}
}