package com.guardwarm.common.extension;

import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * 参考 dubbo SPI实现
 * 每个type 都有唯一对应的ExtensionLoader类
 * @author guardWarm
 * @date 2021-03-14 11:15
 */
/*
	JDK自带SPI的缺陷
	1.JDK 标准的 SPI 会一次性加载实例化扩展点的所有实现，什么意思呢？
	就是如果你在 META-INF/service 下的文件里面加了 N 个实现类，那么 JDK 启动的时候都会一次性全部加载。
	那么如果有的扩展点实现初始化很耗时或者如果有些实现类并没有用到，那么会很浪费资源

	2.如果扩展点加载失败，会导致调用方报错，而且这个错误很难定位到是这个原因

	3.获取某个实现类的方式不够灵活，只能通过 Iterator 形式获取，不能根据某个参数来获取对应的实现类。

	4.不支持AOP与依赖注入。
 */

/*
	dubbo重新实现了一套功能更强的 SPI 机制, 支持了AOP与依赖注入，
	并且利用缓存提高加载实现类的性能，
	同时支持实现类的灵活获取，文中接下来将讲述SPI的应用与原理。
	用了很多懒加载吧，获取的时候拿不到才去创建
	用时间换空间
 */
@Slf4j
public class ExtensionLoader<T> {
	private static final String SERVICE_DIRECTORY = "META-INF/extensions/";

	private static final Map<Class<?>, ExtensionLoader<?>> EXTENSION_LOADERS = new ConcurrentHashMap<>();
	private static final Map<Class<?>, Object> EXTENSION_INSTANCES = new ConcurrentHashMap<>();

	private final Class<?> type;
	private final Map<String, Holder<Object>> cachedInstances = new ConcurrentHashMap<>();
	private final Holder<Map<String, Class<?>>> cachedClasses = new Holder<>();

	private ExtensionLoader(Class<?> type) {
		this.type = type;
	}

	/**
	 * 从EXTENSION_LOADERS map中获取
	 * firstly get from cache, if not hit, create one
	 * @param type 类型
	 * @param <S> 泛型
	 * @return 对应ExtensionLoader
	 */
	public static <S> ExtensionLoader<S> getExtensionLoader(Class<S> type) {
		if (type == null) {
			throw new IllegalArgumentException("Extension type should not be null.");
		}
		if (!type.isInterface()) {
			throw new IllegalArgumentException("Extension type must be an interface.");
		}
		if (type.getAnnotation(SPI.class) == null) {
			throw new IllegalArgumentException("Extension type must be annotated by @SPI");
		}
		// firstly get from cache, if not hit, create one
		ExtensionLoader<S> extensionLoader
				= (ExtensionLoader<S>) EXTENSION_LOADERS.get(type);
		if (extensionLoader == null) {
			EXTENSION_LOADERS.putIfAbsent(type, new ExtensionLoader<S>(type));
			//noinspection unchecked
			extensionLoader = (ExtensionLoader<S>) EXTENSION_LOADERS.get(type);
		}
		return extensionLoader;
	}

	/**
	 * 从cachedInstances map中获取
	 * firstly get from cache, if not hit, create one
	 * create a singleton if no instance exists
	 * @param name 类型
	 * @return 对应ExtensionLoader
	 */
	public T getExtension(String name) {
		if (name == null || name.isEmpty()) {
			throw new IllegalArgumentException("Extension name should not be null or empty.");
		}
		// firstly get from cache, if not hit, create one
		Holder<Object> holder = cachedInstances.get(name);
		if (holder == null) {
			cachedInstances.putIfAbsent(name, new Holder<>());
			holder = cachedInstances.get(name);
		}
		// create a singleton if no instance exists
		Object instance = holder.get();
		if (instance == null) {
			synchronized (holder) {
				instance = holder.get();
				if (instance == null) {
					instance = createExtension(name);
					holder.set(instance);
				}
			}
		}
		return (T) instance;
	}

	/**
	 * 从cachedClasses中获取实例，拿不到就新建
	 * @param name 类型
	 * @return 对应instance
	 */
	private T createExtension(String name) {
		// load all extension classes of type T from file and get specific one by name
		Class<?> clazz = getExtensionClasses().get(name);
		if (clazz == null) {
			throw new RuntimeException("No such extension of name " + name);
		}
		T instance = (T) EXTENSION_INSTANCES.get(clazz);
		if (instance == null) {
			try {
				EXTENSION_INSTANCES.putIfAbsent(clazz, clazz.newInstance());
				instance = (T) EXTENSION_INSTANCES.get(clazz);
			} catch (Exception e) {
				log.error(e.getMessage());
			}
		}
		return instance;
	}

	/**
	 * 获取当前已加载的类
	 * 缓存为空时，利用双重校验锁保证线程安全的前提下，去更新cache
	 * @return 更新完的cache
	 */
	private Map<String, Class<?>> getExtensionClasses() {
		// get the loaded extension class from the cache
		Map<String, Class<?>> classes = cachedClasses.get();
		// 双重校验锁
		if (classes == null) {
			synchronized (cachedClasses) {
				classes = cachedClasses.get();
				if (classes == null) {
					classes = new HashMap<>();
					// load all extensions from our extensions directory
					loadDirectory(classes);
					cachedClasses.set(classes);
				}
			}
		}
		return classes;
	}

	/**
	 * ExtensionLoader.SERVICE_DIRECTORY + type.getName()下的资源缓存到extensionClasses中
	 * @param extensionClasses 一个cache，缓存满足条件的类
	 */
	private void loadDirectory(Map<String, Class<?>> extensionClasses) {
		String fileName = ExtensionLoader.SERVICE_DIRECTORY + type.getName();
		try {
			Enumeration<URL> urls;
			ClassLoader classLoader = ExtensionLoader.class.getClassLoader();
			urls = classLoader.getResources(fileName);
			if (urls != null) {
				while (urls.hasMoreElements()) {
					URL resourceUrl = urls.nextElement();
					loadResource(extensionClasses, classLoader, resourceUrl);
				}
			}
		} catch (IOException e) {
			log.error(e.getMessage());
		}
	}

	/**
	 * 加载对应资源路径下的所有类
	 * @param extensionClasses 一个cache，缓存满足条件的类
	 * @param classLoader 类加载器
	 * @param resourceUrl 资源url
	 */
	private void loadResource(Map<String, Class<?>> extensionClasses, ClassLoader classLoader, URL resourceUrl) {
		try (BufferedReader reader = new BufferedReader(new InputStreamReader(resourceUrl.openStream(), UTF_8))) {
			String line;
			// read every line
			while ((line = reader.readLine()) != null) {
				// get index of comment
				final int ci = line.indexOf('#');
				if (ci >= 0) {
					// string after # is comment so we ignore it
					line = line.substring(0, ci);
				}
				line = line.trim();
				if (line.length() > 0) {
					try {
						final int ei = line.indexOf('=');
						String name = line.substring(0, ei).trim();
						String clazzName = line.substring(ei + 1).trim();
						// our SPI use key-value pair so both of them must not be empty
						if (name.length() > 0 && clazzName.length() > 0) {
							Class<?> clazz = classLoader.loadClass(clazzName);
							extensionClasses.put(name, clazz);
						}
					} catch (ClassNotFoundException e) {
						log.error(e.getMessage());
					}
				}

			}
		} catch (IOException e) {
			log.error(e.getMessage());
		}
	}
}
