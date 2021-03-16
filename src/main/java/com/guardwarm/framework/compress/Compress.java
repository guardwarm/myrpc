package com.guardwarm.framework.compress;

import com.guardwarm.common.extension.SPI;

/**
 * @author guardWarm
 * @date 2021-03-14 17:05
 */
@SPI
public interface Compress {

	byte[] compress(byte[] bytes);

	byte[] decompress(byte[] bytes);
}
