package com.guardwarm.common.extension;

import java.lang.annotation.*;

/**
 * service provider interface
 * @author guardWarm
 * @date 2021-03-14 11:12
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface SPI {
}
