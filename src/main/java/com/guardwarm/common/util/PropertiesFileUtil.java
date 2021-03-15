package com.guardwarm.common.util;

import lombok.extern.slf4j.Slf4j;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

/**
 * 读取配置文件信息
 * @author guardWarm
 * @date 2021-03-14 10:33
 */
@Slf4j
public final class PropertiesFileUtil {
	private PropertiesFileUtil() {
	}

	public static Properties readPropertiesFile(String fileName) {
		// 配置文件目录路径
		URL url = Thread.currentThread().getContextClassLoader().getResource("");
		String rpcConfigPath = "";
		if (url != null) {
			// 具体配置文件路径
			rpcConfigPath = url.getPath() + fileName;
		}
		Properties properties = null;
		try (InputStreamReader inputStreamReader = new InputStreamReader(
				new FileInputStream(rpcConfigPath), StandardCharsets.UTF_8)) {
			properties = new Properties();
			properties.load(inputStreamReader);
		} catch (IOException e) {
			log.error("occur exception when read properties file [{}]", fileName);
		}
		return properties;
	}
}
