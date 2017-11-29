package com.ppx.cloud.common.exception.custom;


/**
 * 404异常
 * @author dengxz
 * @date 2017年3月29日
 */
@SuppressWarnings("serial")
public class NotFoundException extends RuntimeException {
	
	public NotFoundException() {
		super("Not Found");
	}
}


