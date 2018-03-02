package com.ppx.cloud.monitor;

import java.util.BitSet;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.sql.DataSource;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Component;
import org.springframework.web.context.WebApplicationContext;

import com.ppx.cloud.common.config.PropertiesConfig;
import com.ppx.cloud.monitor.output.ConsoleService;
import com.ppx.cloud.monitor.output.MongodbService;
import com.ppx.cloud.monitor.persistence.AccessEntity;
import com.ppx.cloud.monitor.persistence.DebugAccessEntity;
import com.zaxxer.hikari.HikariDataSource;


/**
 * 队列消费者,用于日志输出
 * @author dengxz
 * @date 2017年11月12日
 */
@Component
public class AccessQueueConsumer {

	private static final Logger log = LoggerFactory.getLogger(AccessQueueConsumer.class);
	
	@Autowired
	private MongodbService mongodbService;
	
	@Autowired
	private WebApplicationContext app;

	// 超过队列长度时，记录个数, 每次采集时输出
	private static int outnumber = 0;
	
	// 最后的日志队列异常，如果不为空就采集时输出
	private String lastQueueException;

	public static void increaseOutnumber() {
		outnumber++;
	}

	public void start() {
		// 启动后调用
		log.info(">>>>>>>>>>>>>>>>>>>>>AccessQueueThread().start()>>>>>>>>>>>>>>>>>>>>>");
		new AccessQueueThread().start();
	}
	
	class AccessQueueThread extends Thread {
		public void run() {
			// 定时采集
			timingGather();	
			
			while (true) {
				// 加上try内防止线程死掉
				try {
					consumeAccessLog();			
					threadRun();
				} catch (Exception e) {
					// 记录最后一队列输出的异常到采集
					lastQueueException = e.getMessage();							
					if (e.getStackTrace().length > 2) {
						lastQueueException +=  "\r\n" + e.getStackTrace()[0] + "\r\n" + e.getStackTrace()[1];
					}
					e.printStackTrace();
				}
				
				try {
					Thread.sleep(10);
				} catch (Exception e) {}
			}
		}

		private void consumeAccessLog() {
			AccessLog a;
			while ((a = AccessQueue.getQueue().poll()) != null) {
				if (PropertiesConfig.isDevelopEnable()) {
					ConsoleService.print(a);
				}
				
				// 监控页面的查看不输出
				if (a.getUri().indexOf("/monitorView/") < 0 || a.getException() != null) {
					logToMongodb(a);
				}
				threadRun();
			}
		}
	}

	private void logToMongodb(AccessLog a) {
		AccessEntity access = new AccessEntity();
		DebugAccessEntity debugAccess = new DebugAccessEntity();
		// 访问日志
		mongodbService.access(access, a);
		mongodbService.createAccessIndex();
		// uri统计
		mongodbService.uriStat(a);
		// 响应时间统计(有异常的不统计响应时间)
		if (a.getException() == null) {
			mongodbService.response(a);
		}
		// sql统计
		mongodbService.sqlStat(a);
		
		// debug访问日志 >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
		if (PropertiesConfig.isAccessDebugEnabled()) {
			// isAccessDebugEnabled 模式下的访问日志（debug）
			mongodbService.setDebugAccess(debugAccess, a);
			mongodbService.debug(debugAccess);	
		}
				
		// 异常处理
		if (a.getException() != null) {
			mongodbService.error(access, debugAccess, a);
		}
		
		
		// warning访问日志 >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
		if (PropertiesConfig.isAccessWarningEnabled()) {
			// >>>>>>>>>>>>>> 警告信息 begin >>>>>>>>>>>>>>
			BitSet bs = new BitSet();

			// 检查是否有未关闭的数据库连接
			String warn = AccessAnalysis.checkConnection(a.getGetConnTimes(), a.getReleaseConnTimes());
			if (!StringUtils.isEmpty(warn)) {
				BitSet b = new BitSet();
				b.set(1);
				bs.xor(b);
			}

			// 检查for update有没有加上事务
			warn = AccessAnalysis.checkForUpdate(a.getSqlList(), a.getTransactionTimes());
			if (!StringUtils.isEmpty(warn)) {
				BitSet b = new BitSet();
				b.set(2);
				bs.xor(b);
			}

			// 检查非安全SQL，没有加上where条件
			warn = AccessAnalysis.checkUnSafeSql(a.getSqlList());
			if (!StringUtils.isEmpty(warn)) {
				BitSet b = new BitSet();
				b.set(3);
				bs.xor(b);
			}

			// 检查事务个数是否大于1
			warn = AccessAnalysis.checkTransactionTimes(a.getTransactionTimes());
			if (!StringUtils.isEmpty(warn)) {
				BitSet b = new BitSet();
				b.set(4);
				bs.xor(b);
			}

			// 检查多个操作SQL是否没有使用事务
			warn = AccessAnalysis.checkNoTransaction(a.getSqlList(), a.getTransactionTimes());
			if (!StringUtils.isEmpty(warn)) {
				BitSet b = new BitSet();
				b.set(5);
				bs.xor(b);
			}
			
			// 检查注入SQL
			warn = AccessAnalysis.checkAntiSql(a.getSqlList());
			if (!StringUtils.isEmpty(warn)) {
				BitSet b = new BitSet();
				b.set(6);
				bs.xor(b);
			}
			
			if (!bs.isEmpty()) {
				mongodbService.warning(a, bs);
			}			
		}
	}
	
	private static Date lastGatherTime = new Date();
	private static Date lastGetConfTime = new Date();
	
	private void threadRun() {
		// 采集间隔
		Date currentDate = new Date();
		if (currentDate.getTime() - lastGatherTime.getTime() >= PropertiesConfig.getGatherInterval()) {
			AccessQueueConsumer.lastGatherTime = currentDate;
			timingGather();
		}
		
		// 30秒，同步配置数据
		if (currentDate.getTime() - lastGetConfTime.getTime() >= 30000) {
			AccessQueueConsumer.lastGetConfTime = currentDate;
			Map<?, ?> map = mongodbService.getConfig(AccessUtils.getServiceId());
			if (map != null) {
				PropertiesConfig.setAccessDebug((Boolean)map.get("isAccessDebug"));
				PropertiesConfig.setAccessWarning((Boolean)map.get("isAccessWarning"));
				PropertiesConfig.setGatherInterval((Long)map.get("gatherInterval"));
				PropertiesConfig.setDumpMaxTime((Long)map.get("dumpMaxTime"));
			}	
		}
	}
	
	/**
	 * 定时采集
	 */
	public void timingGather() {
		// 最大连接数
		
		HikariDataSource ds = (HikariDataSource)app.getBean(DataSource.class);
		int dsActive = 0;
		if (ds.getHikariPoolMXBean() != null) {
			dsActive = ds.getHikariPoolMXBean().getActiveConnections();
		}
				
		Map<String, Object> gatherMap = new LinkedHashMap<String, Object>();
		gatherMap.put("serviceId", AccessUtils.getServiceId());
		gatherMap.put("gatherTime", new Date());
		gatherMap.put("maxMemory", Runtime.getRuntime().maxMemory() / 1024 / 1024);
		gatherMap.put("totalMemory", Runtime.getRuntime().totalMemory() / 1024 / 1024);
		gatherMap.put("freeMemory", Runtime.getRuntime().freeMemory() / 1024 / 1024);
		long userMemory = (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / 1024 / 1024;
		gatherMap.put("useMemory", userMemory);
		
		Map<String, Long> usableSpace = AccessUtils.getUsableSpace();
		gatherMap.put("getUsableSpace", usableSpace);		
		// 数据库连接数
		gatherMap.put("dsActive", dsActive);
		
		// cpu
		double processCpuLoad = AccessUtils.getProcessCpuLoad();
		gatherMap.put("processCpuLoad", processCpuLoad);		
		gatherMap.put("systemCpuLoad", AccessUtils.getSystemCpuLoad());
		
		// 请求,包括超时时dump
		Map<String, Object> requestInfo = AccessUtils.getRequestInfo();
		gatherMap.put("requestInfo", requestInfo);
		
		// 内存配置信息 isAccessDebug isAccessWarning gatherInterval dumpMaxTime
		Map<String, Object> configMap = new HashMap<String, Object>();
		configMap.put("isAccessDebug", PropertiesConfig.isAccessDebugEnabled());
		configMap.put("isAccessWarning", PropertiesConfig.isAccessWarningEnabled());
		configMap.put("gatherInterval", PropertiesConfig.getGatherInterval());
		configMap.put("dumpMaxTime", PropertiesConfig.getDumpMaxTime());
		gatherMap.put("config", configMap);
		
		// 采集队列长度，和超出个数
		gatherMap.put("queueSize", AccessQueue.getQueue().size());
		gatherMap.put("queueOutnumber", outnumber);
		if (StringUtils.isNotEmpty(lastQueueException)) {
			gatherMap.put("lastQueueException", lastQueueException);
			lastQueueException = null;
		}
		mongodbService.insertGatherLog(gatherMap);
		
		// 更新服务collection,加上更新时间
		// 最新 堆使用内存  响应时间由响应时间里更新  数据库连接 硬盘 CPU
		Update lastUpdate = new Update();
		lastUpdate.set("lastUseMemory", userMemory);
		lastUpdate.set("lastDsActive", dsActive);
		lastUpdate.set("lastUsableSpace", usableSpace);	
		lastUpdate.set("lastProcessCpuLoad", processCpuLoad);
		lastUpdate.set("lasted", new Date());
		lastUpdate.set("lastConcurrentN", requestInfo.get("concurrentN"));
		mongodbService.upsertService(AccessUtils.getServiceId(), lastUpdate);
	}
	
	
	
}
