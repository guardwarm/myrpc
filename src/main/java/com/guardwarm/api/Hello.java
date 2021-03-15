package com.guardwarm.api;

import lombok.*;

import java.io.Serializable;

/**
 * @author guardWarm
 * @date 2021-03-14 23:47
 */
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
@ToString
public class Hello implements Serializable {
	private String message;
	private String description;
}
