package com.ppx.cloud.common.exception.custom;


/**
 * 日期处理异常
 * @author dengxz
 * @date 2017年3月29日
 */
@SuppressWarnings("serial")
public class JsonException extends RuntimeException {
	
	public JsonException(Exception e) {
		super(e);
	}
}


