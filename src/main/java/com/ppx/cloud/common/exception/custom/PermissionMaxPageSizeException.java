package com.ppx.cloud.common.exception.custom;


/**
 * 
 * @author Administrator
 *
 */

/**
 * 
 * @author dengxz
 * @date 2017年3月28日
 */
@SuppressWarnings("serial")
public class PermissionMaxPageSizeException extends RuntimeException {
	
	public PermissionMaxPageSizeException(String error) {
		super("maxPageSize forbidden:" + error);
	}
	
	
}
