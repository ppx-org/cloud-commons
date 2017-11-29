package com.ppx.cloud.common.exception.custom;

/**
 * 二次提交异常
 * @author dengxiangzhong
 *
 */
@SuppressWarnings("serial")
public class PermissionResubmitException extends RuntimeException {

	public PermissionResubmitException() {
		super("resubmit forbidden");
	}
	
	
}
