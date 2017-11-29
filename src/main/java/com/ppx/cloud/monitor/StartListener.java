package com.ppx.cloud.monitor;

import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.core.env.Environment;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import com.ppx.cloud.common.config.PropertiesConfig;
import com.ppx.cloud.monitor.output.MongodbService;



@Service
public class StartListener implements ApplicationListener<ContextRefreshedEvent> {
	
	@Autowired
	private AccessQueueConsumer accessQueueConsumer;

	@Autowired
	private MongodbService mongodbService;
	
	@Autowired
	private WebApplicationContext app;
	
	@Autowired
	public Environment env;

	@Override
	public void onApplicationEvent(ContextRefreshedEvent event)  {
		
		// 初始化对象
		OperatingSystemMXBean operatingSystemMXBean = (OperatingSystemMXBean)ManagementFactory.getOperatingSystemMXBean();
		AccessUtils.setOperatingSystemMXBean(operatingSystemMXBean);
		Properties p = System.getProperties();
		
		/* 
		 * 创建服务信息
		 * 微服务ID(由机器IP和端口组成) artifactId version osName 物理内存 硬盘大小
		 */
		Update machineUpdate = new Update();				
		machineUpdate.set("artifactId", env.getProperty("info.app.artifactId"));
		machineUpdate.set("version", env.getProperty("info.app.version"));
		machineUpdate.set("osName", p.getProperty("os.name"));
		machineUpdate.set("totalPhysicalMemory", AccessUtils.getTotalPhysicalMemorySize());
		machineUpdate.set("freePhysicalMemory", AccessUtils.getFreePhysicalMemorySize());
		machineUpdate.set("totalSpace", AccessUtils.getTotalSpace());
		machineUpdate.set("springDatasourceUrl", env.getProperty("spring.datasource.url"));
		machineUpdate.set("maxActive", env.getProperty("spring.datasource.hikari.maximum-pool-size"));
		machineUpdate.set("springDataMongodbHost", env.getProperty("spring.data.mongodb.host"));
		machineUpdate.set("maxMemory", Runtime.getRuntime().maxMemory() / 1024 / 1024);
		machineUpdate.set("availableProcessors", Runtime.getRuntime().availableProcessors());
		Date now = new Date();
		machineUpdate.setOnInsert("firsted", now);
		machineUpdate.set("startLasted", now);
		machineUpdate.set("order", -1); // 排序
		machineUpdate.set("display", 1); // 显示/隐藏
		machineUpdate.set("type", "service");
		
		try {
			mongodbService.upsertService(AccessUtils.getServiceId(), machineUpdate);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		
		
		// isAccessDebug isAccessWarning
		Update configUpdate = new Update();
		if (PropertiesConfig.isDevelopEnable()) {
			configUpdate.set("isAccessDebug", true);
			configUpdate.set("isAccessWarning", true);
		}
		else {
			configUpdate.set("isAccessDebug", false);
			configUpdate.set("isAccessWarning", false);
		}
		
		configUpdate.setOnInsert("gatherInterval", PropertiesConfig.getGatherInterval());
		configUpdate.setOnInsert("dumpMaxTime", PropertiesConfig.getDumpMaxTime());
		configUpdate.setOnInsert("firsted", now);
		
		try {
			mongodbService.updateConfig(AccessUtils.getServiceId(), configUpdate);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		
		
		// 启动日志		
		Map<String, Object> startMap = new LinkedHashMap<String, Object>();
		startMap.put("serviceId", AccessUtils.getServiceId());
		startMap.put("profiles", env.getProperty("spring.profiles.active"));
		startMap.put("startTime", new Date(ManagementFactory.getRuntimeMXBean().getStartTime()));
		startMap.put("artifactId", env.getProperty("info.app.artifactId"));
		startMap.put("version", env.getProperty("info.app.version"));
		startMap.put("springDatasourceUrl", env.getProperty("spring.datasource.url"));
		startMap.put("maxActive", env.getProperty("spring.datasource.hikari.maximum-pool-size"));
		startMap.put("maxIdel", 1);
		startMap.put("springDataMongodbHost", env.getProperty("spring.data.mongodb.host"));
		
		startMap.put("javaHome", p.getProperty("java.home"));
		startMap.put("javaRuntimeVersion", p.getProperty("java.runtime.version"));
		startMap.put("PID", p.getProperty("PID"));		
		startMap.put("beanDefinitionCount", app.getBeanDefinitionCount());
		startMap.put("contextSpendTime", System.currentTimeMillis() - app.getStartupDate());
		startMap.put("jvmSpendTime", System.currentTimeMillis() - ManagementFactory.getRuntimeMXBean().getStartTime());
		
		startMap.put("maxMemory", Runtime.getRuntime().maxMemory() / 1024 / 1024);
		startMap.put("totalMemory", Runtime.getRuntime().totalMemory() / 1024 / 1024);
		startMap.put("freeMemory", Runtime.getRuntime().freeMemory() / 1024 / 1024);
		
		// 服务个数
		RequestMappingHandlerMapping requestMappingHandlerMapping = (RequestMappingHandlerMapping)app.getBean("requestMappingHandlerMapping");
		startMap.put("handlerMethodsSize", requestMappingHandlerMapping.getHandlerMethods().size());
		
		try {
			mongodbService.insertStartLog(startMap);
		} catch (Exception e) {
			e.printStackTrace();
		}
						
		
		
		// 创建索引(除access,access需求根据日期不同集合创建索引)
		mongodbService.createIndex();
		
		
		// 启动日志处理进程
		accessQueueConsumer.start();
		
	}
}
