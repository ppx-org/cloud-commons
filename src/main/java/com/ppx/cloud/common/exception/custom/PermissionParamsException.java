package com.ppx.cloud.common.exception.custom;

/**
 * 
 * @author dengxz
 * @date 2017年3月28日
 */
@SuppressWarnings("serial")
public class PermissionParamsException extends RuntimeException {
		
	public PermissionParamsException(String msg) {
		super("params forbidden:" + msg);
	}
	
	
}
