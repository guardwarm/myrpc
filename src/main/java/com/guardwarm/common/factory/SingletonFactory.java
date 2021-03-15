package com.guardwarm.common.factory;

import lombok.NoArgsConstructor;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

/**
 * 获取单例对象的工厂类
 * 第一次获取时通过反射实例化并写入map
 * 通过synchronized保证只实例化一次
 *
 * @author guardWarm
 * @date 2021-03-14 10:54
 */
@NoArgsConstructor
public class SingletonFactory {
	private static final Map<String, Object> OBJECT_MAP = new HashMap<>();

	public static <T> T getInstance(Class<T> c) {
		String key = c.toString();
		Object instance;
		synchronized (SingletonFactory.class) {
			instance = OBJECT_MAP.get(key);
			if (instance == null) {
				try {
					instance = c.getDeclaredConstructor().newInstance();
					OBJECT_MAP.put(key, instance);
				} catch (IllegalAccessException | InstantiationException | NoSuchMethodException | InvocationTargetException e) {
					throw new RuntimeException(e.getMessage(), e);
				}
			}
		}
        /*
            // 将instance转为c类型
            public T cast(Object obj) {
                if (obj != null && !isInstance(obj))
                    throw new ClassCastException(cannotCastMsg(obj));
                return (T) obj;
            }
         */
		return c.cast(instance);
	}
}
