package com.ppx.cloud.monitor;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.util.StringUtils;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import com.ppx.cloud.common.config.PropertiesConfig;


/**
 * 分析输入输出和sql等，返回警告信息
 * @author dengxz
 * @date 2017年11月12日
 */
public class AccessAnalysis {

	/**
	 * 检查是否存在没有关闭的数据库连接
	 * 
	 * @param getConnTimes
	 * @param releaseConnTimes
	 * @return
	 */
	public static String checkConnection(int getConnTimes, int releaseConnTimes) {
		if (getConnTimes != releaseConnTimes) {
			return "[高]存在没有关闭的数据库连接，获取:" + getConnTimes + ",释放：" + releaseConnTimes;
		}
		return "";
	}

	/**
	 * 非安全SQL检查
	 * 
	 * @param sqlList
	 * @return
	 */
	public static String checkUnSafeSql(List<String> sqlList) {
		for (String sql : sqlList) {
			String s = sql.trim().toLowerCase();
			if (s.startsWith("select") || s.startsWith("update") || s.startsWith("delete")) {
				if (s.indexOf("where") < 0 && s.indexOf("limit") < 0) {
					return "[高]SQL缺少where条件";
				}
			}
		}
		return "";
	}
	
	/**
	 * SQL注入检查
	 * 
	 * @param sqlList
	 * @return
	 */
	public static String checkAntiSql(List<String> sqlList) {
		for (String sql : sqlList) {
			String s = sql.trim().toLowerCase().replaceAll(" ", "");
			try {
				// =后面带有数字或单引号则警告
				Pattern pattern = Pattern.compile("(=|like|between)(.?)");
				Matcher matcher = pattern.matcher(s);
			    while (matcher.find()) {
			    	String m = matcher.group(2);
			    	int charInt = m.toCharArray()[0];
			    	// '=39 0=48 9=57
			    	if (charInt == 39 || (charInt >= 48 &&charInt <=57)) {
			    		return "[高]SQL可能存在注入风险:" + matcher.group();
			    	}
			    }
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return "";
	}

	/**
	 * 检查事务个数
	 * 
	 * @param transactionTimes
	 * @return
	 */
	public static String checkTransactionTimes(int transactionTimes) {
		if (transactionTimes > 1) {
			return "[高]事务个数大于1";
		}
		return "";
	}

	/**
	 * 检查多个操作SQL是否没有使用事务
	 * 
	 * @param sqlList
	 * @param transactionTimes
	 * @return
	 */
	public static String checkNoTransaction(List<String> sqlList, int transactionTimes) {
		if (transactionTimes == 0) {
			int opSqlTimes = 0;
			for (String sql : sqlList) {
				String s = sql.toLowerCase().trim();
				if (s.startsWith("insert") || s.startsWith("update") || s.startsWith("delete")) {
					opSqlTimes++;
				}
			}
			if (opSqlTimes >= 2) {
				return "[高]多个DML SQL没有使用事务";
			}
		}
		return "";
	}

	/**
	 * for udpate检查
	 * 
	 * @param sqlList
	 * @param transactionTimes
	 * @return
	 */
	public static String checkForUpdate(List<String> sqlList, int transactionTimes) {
		for (String sql : sqlList) {
			String s = sql.toLowerCase();
			Pattern p = Pattern.compile("for( *?)update");
			Matcher m = p.matcher(s);
			if (transactionTimes == 0 && m.find()) {
				return "[高]for update语句没有加上事务";
			}
		}
		return "";
	}

	/**
	 * 检查重复sql
	 * 
	 * @return
	 */
	public static String checkDuplicateSql(List<String> sqlList) {
		List<String> tmpList = new ArrayList<String>();
		for (String sql : sqlList) {
			Matcher numMatcher = Pattern.compile("[0-9]").matcher(sql);
			String returnSql = numMatcher.replaceAll("");
			Matcher quotesMatcher = Pattern.compile("\'([^\']*)\'").matcher(returnSql);
			returnSql = quotesMatcher.replaceAll("");
			tmpList.add(returnSql);
		}

		for (int i = 0; i < tmpList.size(); i++) {
			String sql = tmpList.get(i);
			for (int j = i + 1; j < tmpList.size(); j++) {
				if (sql.equals(tmpList.get(j))) {
					return "[中]一个请求中出现多条相同SQL";
				}
			}
		}
		return "";
	}

	/**
	 * 检查输出json是否标准
	 * 
	 * @param json
	 * @return
	 */
	public static String checkOutJson(String json) {
		if (!StringUtils.isEmpty(json)) {
			if (json.indexOf("actionStatus") < 0 || json.indexOf("errorCode") < 0) {
				return "[中]返回json格式不规范";
			}
		}
		return "";
	}
	
	public static String checkPostSupportGet(String uri, String requestMethod) {
		if (!"POST".equals(requestMethod)) return "";
		
		RequestMappingHandlerMapping r = (RequestMappingHandlerMapping)PropertiesConfig.app.getBean("requestMappingHandlerMapping");
		Map<RequestMappingInfo, HandlerMethod> map = r.getHandlerMethods();
		for (RequestMappingInfo info : map.keySet()) {
			String methodUri = (String)info.getPatternsCondition().getPatterns().toArray()[0];
			if (uri.endsWith(methodUri)) {
				Object[] method = info.getMethodsCondition().getMethods().toArray();
				if (method.length != 1 || !"POST".equals(method)) {
					return "[中]POST请求了支持GET的接口";
				}
			}
		}
		
		return "";
	}
	
	
	
}