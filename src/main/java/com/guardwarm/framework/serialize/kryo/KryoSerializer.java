package com.guardwarm.framework.serialize.kryo;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.guardwarm.common.exception.SerializeException;
import com.guardwarm.framework.remoting.dto.RpcRequest;
import com.guardwarm.framework.remoting.dto.RpcResponse;
import com.guardwarm.framework.serialize.Serializer;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

/**
 * @author guardWarm
 * @date 2021-03-14 16:38
 */
public class KryoSerializer implements Serializer {
	/**
	 * kyro不是线程安全的，所以使用ThreadLocal
	 * 每次get()时无数据时，会执行withInitial
	 */
	private final ThreadLocal<Kryo> kryoThreadLocal = ThreadLocal.withInitial(() -> {
		Kryo kryo = new Kryo();
		kryo.register(RpcResponse.class);
		kryo.register(RpcRequest.class);
		return kryo;
	});

	@Override
	public byte[] serialize(Object obj) {
		try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
		     Output output = new Output(byteArrayOutputStream)) {
			// 拿不到数据时会设置为初始值
			Kryo kryo = kryoThreadLocal.get();
			// Object->byte:将对象序列化为byte数组
			kryo.writeObject(output, obj);
			// 用完就remove()避免内存泄漏
			kryoThreadLocal.remove();
			return output.toBytes();
		} catch (Exception e) {
			throw new SerializeException("Serialization failed");
		}
	}

	@Override
	public <T> T deserialize(byte[] bytes, Class<T> clazz) {
		try (ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(bytes);
		     Input input = new Input(byteArrayInputStream)) {
			Kryo kryo = kryoThreadLocal.get();
			// byte->Object:从byte数组中反序列化出对对象
			Object o = kryo.readObject(input, clazz);
			kryoThreadLocal.remove();
			return clazz.cast(o);
		} catch (Exception e) {
			throw new SerializeException("Deserialization failed");
		}
	}

}
