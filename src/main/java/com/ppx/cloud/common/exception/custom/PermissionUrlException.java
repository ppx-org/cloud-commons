package com.ppx.cloud.common.exception.custom;
/**
 * 非法URL异常
 * @author dengxiangzhong
 *
 */
@SuppressWarnings("serial")
public class PermissionUrlException extends RuntimeException {
	
	public PermissionUrlException() {
		super("url forbidden");
	}
	
	public PermissionUrlException(String msg) {
		super(msg);
	}
}


