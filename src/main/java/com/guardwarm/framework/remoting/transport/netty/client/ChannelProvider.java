package com.guardwarm.framework.remoting.transport.netty.client;

import io.netty.channel.Channel;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author guardWarm
 * @date 2021-03-14 23:06
 */
@Slf4j
public class ChannelProvider {
	private final Map<String, Channel> channelMap;

	public ChannelProvider() {
		channelMap = new ConcurrentHashMap<>();
	}

	public Channel get(InetSocketAddress inetSocketAddress) {
		String key = inetSocketAddress.toString();
		if (channelMap.containsKey(key)) {
			Channel channel = channelMap.get(key);
			// 确定连接是否可用
			if (channel != null && channel.isActive()) {
				// 直接获取连接
				return channel;
			} else {
				// 移除连接
				channelMap.remove(key);
			}
		}
		return null;
	}

	public void set(InetSocketAddress inetSocketAddress, Channel channel) {
		String key = inetSocketAddress.toString();
		channelMap.put(key, channel);
	}

	public void remove(InetSocketAddress inetSocketAddress) {
		String key = inetSocketAddress.toString();
		channelMap.remove(key);
		log.info("Channel map size :[{}]", channelMap.size());
	}
}
