package com.guardwarm.framework.compress.gzip;

import com.guardwarm.framework.compress.Compress;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 * 基于java.util.zip包实现压缩和解压缩
 * @author guardWarm
 * @date 2021-03-14 17:14
 */
public class GzipCompress implements Compress {
	/**
	 * 缓存区大小
	 */
	private static final int BUFFER_SIZE = 1024 * 4;

	@Override
	public byte[] compress(byte[] bytes) {
		if (bytes == null) {
			throw new NullPointerException("bytes is null");
		}

		// 优先使用try-with-resources
		try (ByteArrayOutputStream out = new ByteArrayOutputStream();
				GZIPOutputStream gzip = new GZIPOutputStream(out)) {
			gzip.write(bytes);
			gzip.flush();
			gzip.finish();
			return out.toByteArray();
		} catch (IOException e) {
			throw new RuntimeException("gzip compress error", e);
		}
	}

	@Override
	public byte[] decompress(byte[] bytes) {
		if (bytes == null) {
			throw new NullPointerException("bytes is null");
		}

		try (ByteArrayOutputStream out = new ByteArrayOutputStream();
				GZIPInputStream gunzip = new GZIPInputStream(new ByteArrayInputStream(bytes))) {
			byte[] buffer = new byte[BUFFER_SIZE];
			int n;
			while ((n = gunzip.read(buffer)) > -1) {
				out.write(buffer, 0, n);
			}
			return out.toByteArray();
		} catch (IOException e) {
			throw new RuntimeException("gzip decompress error", e);
		}
	}
}
