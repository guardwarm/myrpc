package com.guardwarm.framework.loadbalance.loadbalancer;

import com.guardwarm.framework.loadbalance.AbstractLoadBalance;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 参考dubbo
 * 基于一致性哈希的获取策略
 * @author guardWarm
 * @date 2021-03-14 19:04
 */
public class ConsistentHashLoadBalance extends AbstractLoadBalance {
	private final ConcurrentHashMap<String, ConsistentHashSelector> selectors
			= new ConcurrentHashMap<>();

	@Override
	protected String doSelect(List<String> serviceAddresses, String rpcServiceName) {
		int identityHashCode = System.identityHashCode(serviceAddresses);

		ConsistentHashSelector selector = selectors.get(rpcServiceName);

		// check for updates
		if (selector == null || selector.identityHashCode != identityHashCode) {
			// 每个serviceAddress生产的虚拟节点个数
			int REPLICA_NUMBER = 160;
			selectors.put(rpcServiceName, new ConsistentHashSelector(serviceAddresses, REPLICA_NUMBER, identityHashCode));
			selector = selectors.get(rpcServiceName);
		}

		return selector.select(rpcServiceName);
	}

	static class ConsistentHashSelector {
		/**
		 * key--hash值
		 * value--服务名
		 * 每个服务名对应160个hash值
		 */
		private final TreeMap<Long, String> virtualInvokers;

		private final int identityHashCode;

		ConsistentHashSelector(List<String> invokers, int replicaNumber, int identityHashCode) {
			this.virtualInvokers = new TreeMap<>();
			this.identityHashCode = identityHashCode;

			for (String invoker : invokers) {
				/*
				4————md5加密完为16位，生成hash值只使用了4位，
				所以为了减少hash次数，每个md5加密要生成4个hash值
				 */
				for (int i = 0; i < replicaNumber / 4; i++) {
					byte[] digest = md5(invoker + i);
					for (int h = 0; h < 4; h++) {
						long m = hash(digest, h);
						virtualInvokers.put(m, invoker);
					}
				}
			}
		}

		/**
		 * md5加密
		 * @param key 待加密字符串
		 * @return 加密后的字节数组
		 */
		static byte[] md5(String key) {
			MessageDigest md;
			try {
				md = MessageDigest.getInstance("MD5");
				byte[] bytes = key.getBytes(StandardCharsets.UTF_8);
				md.update(bytes);
			} catch (NoSuchAlgorithmException e) {
				throw new IllegalStateException(e.getMessage(), e);
			}

			return md.digest();
		}

		/**
		 * hash函数
		 * @param digest md5加密后的字节数组，固定长度为16位
		 * @param idx 用哪四个字节
		 * @return hash值
		 */
		static long hash(byte[] digest, int idx) {
			return (  (long) (digest[3 + idx * 4] & 255) << 24
					| (long) (digest[2 + idx * 4] & 255) << 16
					| (long) (digest[1 + idx * 4] & 255) << 8
					| (long) (digest[idx * 4] & 255)  ) & 4294967295L;
		}

		public String select(String rpcServiceName) {
			byte[] digest = md5(rpcServiceName);
			return selectForKey(hash(digest, 0));
		}

		public String selectForKey(long hashCode) {
			// 第一个比给定hash值大的服务节点
			Map.Entry<Long, String> entry
					= virtualInvokers
					.tailMap(hashCode, true)
					.firstEntry();

			if (entry == null) {
				// 找不到第一个比他大的就返回总的第一个节点
				entry = virtualInvokers.firstEntry();
			}


			return entry.getValue();
		}
	}
}
