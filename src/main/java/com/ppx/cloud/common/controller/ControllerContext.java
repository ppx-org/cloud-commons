package com.ppx.cloud.common.controller;

import com.ppx.cloud.monitor.AccessLog;

/**
 * 本地线程上下文，记录日志
 * @author dengxz
 * @date 2017年11月4日
 */
public class ControllerContext {

	public static ThreadLocal<AccessLog> threadLocalAccess = new ThreadLocal<AccessLog>();

	public static void setAccessLog(AccessLog log) {
		threadLocalAccess.set(log);
	}

	public static AccessLog getAccessLog() {
		return threadLocalAccess.get();
	}
	
}
