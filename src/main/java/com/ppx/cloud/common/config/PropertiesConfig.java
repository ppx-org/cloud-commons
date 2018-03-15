package com.ppx.cloud.common.config;

import javax.annotation.Resource;
import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.web.context.WebApplicationContext;

import com.zaxxer.hikari.HikariDataSource;

/**
 * tips:setRemoveAbandoned超时是否删除连接，在处理量大的数据时不能用true
 * 
 * @author dengxz
 * @date 2017年4月5日
 */
@Configuration
public class PropertiesConfig  {
	
	// 为了让firstConfigBean先运行 (@ComponentScan自动扫描之后@Order不生效)
	@Resource(name = "firstConfigRun")
	private Object firstConfigBean;
	
	@Autowired
	private Environment injectEnv;
	
	public static Environment env;
	
	@Autowired
	private WebApplicationContext injectApp;
	
	public static WebApplicationContext app;
	
	@Value("${spring.profiles.active}")
	private String active;
	
	
	// 根据-Dspring.profiles.active判断
	private static boolean isDevelop = false;

	// 访问日志调试级别，可动态改变
	private static boolean isAccessDebug = false;
	
	// 访问日志警告级别，可动态改变
	private static boolean isAccessWarning = false;
	
	// 采集间隔，可动态改变300秒
	private static long gatherInterval = 300000;
	
	// 请求超过时间就dump，可动态改变5 秒
	private static long dumpMaxTime = 5000;
	
	@Bean
	public Object config() {
		PropertiesConfig.env = injectEnv;
		PropertiesConfig.app = injectApp;
		
		if ("dev".equals(active)) {
			// 开发环境
			PropertiesConfig.isDevelop = true;
			PropertiesConfig.isAccessDebug = true;
			PropertiesConfig.isAccessWarning = true;
		}
		
		return null;
	}

	public static boolean isDevelopEnable() {
		return PropertiesConfig.isDevelop;
	}

	public static boolean isAccessDebugEnabled() {
		return PropertiesConfig.isAccessDebug;
	}

	public static void setAccessDebug(boolean isAccessDebug) {
		PropertiesConfig.isAccessDebug = isAccessDebug;
	}
	
	public static boolean isAccessWarningEnabled() {
		return isAccessWarning;
	}

	public static void setAccessWarning(boolean isAccessWarning) {
		PropertiesConfig.isAccessWarning = isAccessWarning;
	}

	public static long getGatherInterval() {
		return gatherInterval;
	}

	public static void setGatherInterval(long gatherInterval) {
		PropertiesConfig.gatherInterval = gatherInterval;
	}

	public static long getDumpMaxTime() {
		return dumpMaxTime;
	}

	public static void setDumpMaxTime(long dumpMaxTime) {
		PropertiesConfig.dumpMaxTime = dumpMaxTime;
	}

	
	
	
	
	@Bean
	public DataSource dataSource() throws Exception {
		
		
		HikariDataSource dataSource = new HikariDataSource();
		dataSource.setDriverClassName("com.mysql.jdbc.Driver");
        dataSource.setJdbcUrl(System.getProperty("spring.datasource.url"));
        dataSource.setUsername(System.getProperty("spring.datasource.username"));
        dataSource.setPassword(System.getProperty("spring.datasource.password"));
        
        
        dataSource.setMaximumPoolSize(100);
        return dataSource;
	}

}
