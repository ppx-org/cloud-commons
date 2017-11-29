package com.ppx.cloud.common.exception;

/**
 * 错误代码Bean
 * 
 * @author dengxiangzhong
 *
 */
public class ErrorBean {
	// 0为成功，其他为失败
	private int code;
	// 失败原因
	private String info;

	public ErrorBean(int code, String info) {
		this.code = code;
		this.info = info;
	}

	public int getCode() {
		return code;
	}

	public void setCode(int code) {
		this.code = code;
	}

	public String getInfo() {
		return info;
	}

	public void setInfo(String info) {
		this.info = info;
	}

}
