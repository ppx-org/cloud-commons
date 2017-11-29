package com.ppx.cloud.monitor;

import java.io.File;
import java.lang.management.OperatingSystemMXBean;
import java.lang.reflect.Method;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.management.MBeanServer;
import javax.management.ObjectInstance;
import javax.management.ObjectName;
import javax.servlet.http.HttpServletRequest;

import org.apache.tomcat.util.modeler.Registry;
import org.springframework.util.StringUtils;

import com.ppx.cloud.common.config.PropertiesConfig;

/**
 * 访问日志工具类
 * @author dengxz
 * @date 2017年11月12日
 */
public class AccessUtils {
	
	private static String serviceId = "";

	private static OperatingSystemMXBean operatingSystemMXBean;

	public static OperatingSystemMXBean getOperatingSystemMXBean() {
		return operatingSystemMXBean;
	}

	public static void setOperatingSystemMXBean(OperatingSystemMXBean operatingSystemMXBean) {
		AccessUtils.operatingSystemMXBean = operatingSystemMXBean;
	}
	
	
	public static Map<String, Object> getRequestInfo() {
		Map<String, Object> info = new HashMap<String, Object>();
		
		// 并发数|最大处理时间|最大时线程名称｜最大时Uri
		int concurrentN = 0;
		long maxProcessingTime = 0;
		String maxWorkerThreadName = "";
		String maxUri = "";		
		List<String> maxMsg = new ArrayList<String>();
		
		MBeanServer mBeanServer = Registry.getRegistry(null, null).getMBeanServer();
		try {
			Set<ObjectInstance> set = mBeanServer.queryMBeans(new ObjectName("*:type=RequestProcessor,*"), null);
			Iterator<ObjectInstance> iterator = set.iterator();
			while (iterator.hasNext()) {
				ObjectInstance oi = (ObjectInstance) iterator.next();
				Object currentUri = mBeanServer.getAttribute(oi.getObjectName(), "currentUri");
				if (currentUri != null) {
					concurrentN++;														
				}
				Long requestProcessingTime = (Long)mBeanServer.getAttribute(oi.getObjectName(), "requestProcessingTime");
				if (currentUri != null && requestProcessingTime > maxProcessingTime) {
					maxProcessingTime = requestProcessingTime;
					Object threadName = mBeanServer.getAttribute(oi.getObjectName(), "workerThreadName");
					if (threadName != null) {
						maxWorkerThreadName = threadName.toString();
					}							
					maxUri = currentUri.toString();
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		// 超过指定时间，则输出线程信息
		if (maxProcessingTime > PropertiesConfig.getDumpMaxTime() && !"".equals(maxWorkerThreadName)) {											
			Thread t = AccessUtils.getThread(maxWorkerThreadName);
			StackTraceElement[] stackTraceElements = t.getStackTrace();					
			for (StackTraceElement e : stackTraceElements) {
				if (e.getClassName().startsWith("com.ppx") && e.getLineNumber() > 0) {
					maxMsg.add(e.getClassName() + "(" + e.getLineNumber() + ")");
				}
			}
		}
		info.put("concurrentN", concurrentN);
		info.put("maxProcessingTime", maxProcessingTime);
		info.put("maxUri", maxUri);
		if (maxMsg.size() > 0) {
			info.put("maxMsg", maxMsg);
		}
		return info;
	}
	
	public static Thread getThread(String threadName) {
		Map<Thread, StackTraceElement[]> map = Thread.getAllStackTraces();
        Iterator<Thread> it = map.keySet().iterator();  
        while (it.hasNext()) {  
            Thread t = (Thread) it.next(); 
            if (t.getName().equals(threadName)) return t; 
        }
        return null;
	}
	
	public static long getTotalPhysicalMemorySize() {		
		try {
			Method m = operatingSystemMXBean.getClass().getDeclaredMethod("getTotalPhysicalMemorySize");
			m.setAccessible(true);
			long totalPhysicalMemorySize = (Long)m.invoke(operatingSystemMXBean);
			return totalPhysicalMemorySize;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return -1;
	}
	
	public static long getFreePhysicalMemorySize() {		
		try {
			Method m = operatingSystemMXBean.getClass().getDeclaredMethod("getFreePhysicalMemorySize");
			m.setAccessible(true);
			long totalPhysicalMemorySize = (Long)m.invoke(operatingSystemMXBean);
			return totalPhysicalMemorySize;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return -1;
	}
	
	public static double getSystemCpuLoad() {		
		try {
			Method m = operatingSystemMXBean.getClass().getDeclaredMethod("getSystemCpuLoad");
			m.setAccessible(true);
			Double systemCpuLoad = (Double)m.invoke(operatingSystemMXBean);
			return systemCpuLoad;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return -1;
	}
	
	/*
	 * Limitations:
	 * 	The getSystemLoadAverage() and disk space querying methods are only available under Java 6. Also,
 	 * 	some JMX functionality may not be available to all platforms 
	 * 	(i.e. it's been reported that getSystemLoadAverage() returns -1 on Windows).
	 */
	public static double getProcessCpuLoad() {
		try {
			Method m = operatingSystemMXBean.getClass().getDeclaredMethod("getProcessCpuLoad");
			m.setAccessible(true);
			Double processCpuLoad = (Double)m.invoke(operatingSystemMXBean);
			return processCpuLoad;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return -1;
	}
	
	public static Map<String, Long> getTotalSpace() {	
		Map<String, Long> returnMap = new HashMap<String, Long>();
		// 获取磁盘分区列表
		File[] roots = File.listRoots();
		 for (File file : roots) {
			 returnMap.put(file.getPath(), file.getTotalSpace() / 1024 / 1024);
		}
		return returnMap;
	}

	public static Map<String, Long> getUsableSpace() {	
		Map<String, Long> returnMap = new HashMap<String, Long>();
		// 获取磁盘分区列表
		File[] roots = File.listRoots();
		 for (File file : roots) {
			 returnMap.put(file.getPath(), file.getUsableSpace() / 1024 / 1024);
		}
		return returnMap;
	}
	
	/**
	 * 取得微服务ID, ip:port组成
	 * 
	 * @return
	 */
	public static String getServiceId() {
		if (!StringUtils.isEmpty(serviceId)) {
			return serviceId;
		}
		
		String ip = "";
		String port = PropertiesConfig.env.getProperty("server.port");

		String osName = System.getProperty("os.name");
		if (osName.toLowerCase().indexOf("windows") > -1) {
			// windows >>>>>>>>>>>>>>>>>>>>>
			try {
				InetAddress inetAddress = InetAddress.getLocalHost();
				ip = inetAddress.getHostAddress();
			} catch (Exception e) {
				e.printStackTrace();
				ip = "127.0.0.1";
			}
		} else {
			// linux >>>>>>>>>>>>>>>>>>>>>>>>>
			try {
				for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en
						.hasMoreElements();) {
					NetworkInterface intf = en.nextElement();
					String name = intf.getName();
					if (!name.contains("docker") && !name.contains("lo")) {
						for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr
								.hasMoreElements();) {
							InetAddress inetAddress = enumIpAddr.nextElement();
							if (!inetAddress.isLoopbackAddress()) {
								String ipaddress = inetAddress.getHostAddress().toString();
								if (!ipaddress.contains("::") && !ipaddress.contains("0:0:")
										&& !ipaddress.contains("fe80")) {
									ip = ipaddress;
								}
							}
						}
					}
				}
			} catch (SocketException e) {
				e.printStackTrace();
				ip = "127.0.0.1";
			}
		}
		return ip + ":" + port;
		
	}

	// 写入队列
	public synchronized static void writeQueue(AccessLog accessLog) {
		// 超过就发不再写入队列，记录在outnumber
		boolean isOffer = AccessQueue.offer(accessLog);
		if (!isOffer) {
			AccessQueueConsumer.increaseOutnumber();
		}
	}

	/**
	 * 获取用户真实IP地址，不使用request.getRemoteAddr();的原因是有可能用户使用了代理软件方式避免真实IP地址,
	 * 
	 * 可是，如果通过了多级反向代理的话，X-Forwarded-For的值并不止一个，而是一串IP值，究竟哪个才是真正的用户端的真实IP呢？
	 * 答案是取X-Forwarded-For中第一个非unknown的有效IP字符串。
	 * 
	 * 如：X-Forwarded-For：192.168.1.110, 192.168.1.120, 192.168.1.130,
	 * 192.168.1.100
	 * 
	 * 用户真实IP为： 192.168.1.110
	 * 
	 * @param request
	 * @return
	 */
	public static String getIpAddress(HttpServletRequest request) {
		String ip = request.getHeader("x-forwarded-for");

		if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
			ip = request.getHeader("Proxy-Client-IP");
		}
		if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
			ip = request.getHeader("WL-Proxy-Client-IP");
		}
		if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
			ip = request.getHeader("HTTP_CLIENT_IP");
		}
		if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
			ip = request.getHeader("HTTP_X_FORWARDED_FOR");
		}
		if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
			ip = request.getRemoteAddr();
		}
		return ip;
	}

	/**
	 * 读取请求参数(包括get和post)
	 * 
	 * @param request
	 * @return
	 */
	public static String getParams(HttpServletRequest request) {
		List<String> params = new ArrayList<String>();
		Enumeration<String> pNames = request.getParameterNames();
		while (pNames.hasMoreElements()) {
			String name = pNames.nextElement();
			String[] v = request.getParameterValues(name);
			for (String s : v) {
				params.add(name + "=" + s);
			}
		}
		return StringUtils.collectionToDelimitedString(params, "&");
	}

}