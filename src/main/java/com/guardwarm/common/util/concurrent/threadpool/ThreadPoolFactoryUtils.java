package com.guardwarm.common.util.concurrent.threadpool;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.concurrent.*;

/**
 * @author guardWarm
 * @date 2021-03-14 10:51
 */
@Slf4j
public class ThreadPoolFactoryUtils {
	/**
	 * 缓存已创建的线程池
	 */
	private static final Map<String, ExecutorService> THREAD_POOLS
			= new ConcurrentHashMap<>();

	private ThreadPoolFactoryUtils() {
	}

	/**
	 * 创建线程池
	 * @param threadNamePrefix 线程名前缀，用于区分不同场景
	 * @return 创建好的线程池
	 */
	public static ExecutorService createCustomThreadPoolIfAbsent(String threadNamePrefix) {
		CustomThreadPoolConfig customThreadPoolConfig = new CustomThreadPoolConfig();
		return createCustomThreadPoolIfAbsent(customThreadPoolConfig, threadNamePrefix, false);
	}

	/**
	 * 创建线程池
	 * @param threadNamePrefix 线程名前缀，用于区分不同场景
	 * @param customThreadPoolConfig 配置信息
	 * @return 创建好的线程池
	 */
	public static ExecutorService createCustomThreadPoolIfAbsent(String threadNamePrefix, CustomThreadPoolConfig customThreadPoolConfig) {
		return createCustomThreadPoolIfAbsent(customThreadPoolConfig, threadNamePrefix, false);
	}

	/**
	 * 创建线程池
	 * @param customThreadPoolConfig 配置信息
	 * @param threadNamePrefix 线程名前缀，用于区分不同场景
	 * @param daemon 是否为守护线程
	 * @return 线程池
	 */
	public static ExecutorService createCustomThreadPoolIfAbsent(CustomThreadPoolConfig customThreadPoolConfig, String threadNamePrefix, Boolean daemon) {
		// 获取threadNamePrefix对应线程池，不存在则创建
		ExecutorService threadPool = THREAD_POOLS.computeIfAbsent(threadNamePrefix, k -> createThreadPool(customThreadPoolConfig, threadNamePrefix, daemon));
		// 如果 threadPool 被 shutdown 的话就重新创建一个
		if (threadPool.isShutdown() || threadPool.isTerminated()) {
			THREAD_POOLS.remove(threadNamePrefix);
			threadPool = createThreadPool(customThreadPoolConfig, threadNamePrefix, daemon);
			THREAD_POOLS.put(threadNamePrefix, threadPool);
		}
		return threadPool;
	}

	/**
	 * shutDown所有线程池
	 */
	public static void shutDownAllThreadPool() {
		log.info("call shutDownAllThreadPool method");
		THREAD_POOLS.entrySet().parallelStream().forEach(entry -> {
			ExecutorService executorService = entry.getValue();
			executorService.shutdown();
			log.info("shut down thread pool [{}] [{}]", entry.getKey(), executorService.isTerminated());
			try {
				/*
					阻塞直到关闭请求后所有任务完成执行，
					或者发生超时，
					或者当前线程被中断（以先发生者为准）
				 */
				executorService.awaitTermination(10, TimeUnit.SECONDS);
			} catch (InterruptedException e) {
				log.error("Thread pool never terminated");
				executorService.shutdownNow();
			}
		});
	}

	/**
	 * 创建线程池
	 * @param customThreadPoolConfig 配置信息
	 * @param threadNamePrefix 线程名前缀，用于区分不同场景
	 * @param daemon 是否为守护进程
	 * @return 线程池
	 */
	private static ExecutorService createThreadPool(CustomThreadPoolConfig customThreadPoolConfig, String threadNamePrefix, Boolean daemon) {
		ThreadFactory threadFactory = createThreadFactory(threadNamePrefix, daemon);
		return new ThreadPoolExecutor(customThreadPoolConfig.getCorePoolSize(), customThreadPoolConfig.getMaximumPoolSize(),
				customThreadPoolConfig.getKeepAliveTime(), customThreadPoolConfig.getUnit(), customThreadPoolConfig.getWorkQueue(),
				threadFactory);
	}

	/**
	 * 创建 ThreadFactory
	 * 如果threadNamePrefix不为空则使用自定义ThreadFactory，
	 * 否则使用defaultThreadFactory
	 *
	 * @param threadNamePrefix 线程名前缀，用于区分不同场景
	 * @param daemon           指定是否为 Daemon Thread(守护线程)
	 * @return ThreadFactory
	 */
	public static ThreadFactory createThreadFactory(String threadNamePrefix, Boolean daemon) {
		if (threadNamePrefix != null) {
			ThreadFactoryBuilder tfb = new ThreadFactoryBuilder();

			if (daemon != null) {
				return new ThreadFactoryBuilder()
						.setNameFormat(threadNamePrefix + "-%d")
						.setDaemon(daemon)
						.build();
			} else {
				return new ThreadFactoryBuilder()
						.setNameFormat(threadNamePrefix + "-%d")
						.build();
			}
		}
		return Executors.defaultThreadFactory();
	}

	/**
	 * 打印线程池的状态
	 * @param threadPool 线程池对象
	 */
	public static void printThreadPoolStatus(ThreadPoolExecutor threadPool) {
		ScheduledExecutorService scheduledExecutorService = new ScheduledThreadPoolExecutor(1, createThreadFactory("print-thread-pool-status", false));
		scheduledExecutorService.scheduleAtFixedRate(() -> {
			log.info("============ThreadPool Status=============");
			log.info("ThreadPool Size: [{}]", threadPool.getPoolSize());
			log.info("Active Threads: [{}]", threadPool.getActiveCount());
			log.info("Number of Tasks : [{}]", threadPool.getCompletedTaskCount());
			log.info("Number of Tasks in Queue: {}", threadPool.getQueue().size());
			log.info("===========================================");
		}, 0, 1, TimeUnit.SECONDS);
	}

}
