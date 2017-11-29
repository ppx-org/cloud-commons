package com.ppx.cloud.monitor;

import java.util.LinkedList;
import java.util.Queue;

/**
 * 日志队列
 * @author dengxz
 * @date 2017年11月12日
 */
public class AccessQueue {
	// QueueConsumer.outnumber
	private static int QUEUE_MAX_SIZE = 2000;
	
	private static Queue<AccessLog> queue = new LinkedList<AccessLog>(); 
	
	public static boolean offer(AccessLog accessLog) { 
		if (queue.size() > QUEUE_MAX_SIZE) return false;
		return queue.offer(accessLog);
	}
	
	public static Queue<AccessLog> getQueue() {
		return queue;
	}
	
	
	
}