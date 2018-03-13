package com.ppx.cloud.monitor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 访问日志Bean
 * @author dengxz
 * @date 2017年11月12日
 */
public class AccessLog {
	// private static int INPUT_MAX_LEN = 1000;

	// ------------
	private String ip;
	private Date beginTime;
	private long spendTime;
	private String method;
	private String uri;
	private String queryString;	
	private Exception exception;
	private String userId;
	
	private List<String> sqlList = new ArrayList<String>();
	private Map<Integer, List<Object>> sqlArgMap = new HashMap<Integer, List<Object>>();	
	private List<Long> sqlSpendTime = new ArrayList<Long>();
	private List<Date> sqlBeginTime = new ArrayList<Date>();
	private List<Long> sqlCount = new ArrayList<Long>();
	
	private int transactionTimes = 0;
	private int getConnTimes = 0;
	private int releaseConnTimes = 0;
	
	private String cacheKey;
	
	// 输入参数(PropertiesConfig.isAccessDebugEnabled()控制)
	private String params;	
	// 输入json(PropertiesConfig.isAccessDebugEnabled()控制)
	private String inJson;
	// 输出json(PropertiesConfig.isAccessDebugEnabled()控制)
	private String outJson;
	// 模板上model对象(PropertiesConfig.isAccessDebugEnabled()控制)
	private List<String> outModelList = new ArrayList<String>();
	// 模板上输出的脚本(PropertiesConfig.isAccessDebugEnabled()控制)
	private List<String> outScriptList = new ArrayList<String>();	
	// http请求内容received(PropertiesConfig.isAccessDebugEnabled()控制)
	private String received;
	
	
	public void addSql(String sql) {
		// MongoDB索引字段的长度不能大于1024字节
		if (sql != null && sql.length() > 1024) {
			sql = sql.substring(0, 1024);
		}
		sqlList.add(sql);
	}
	
	public String getIp() {
		return ip;
	}

	public void setIp(String ip) {
		this.ip = ip;
	}

	public Date getBeginTime() {
		return beginTime;
	}

	public void setBeginTime(Date beginTime) {
		this.beginTime = beginTime;
	}

	public long getSpendTime() {
		return spendTime;
	}

	public void setSpendTime(long spendTime) {
		this.spendTime = spendTime;
	}

	public String getMethod() {
		return method;
	}

	public void setMethod(String method) {
		this.method = method;
	}

	public String getUri() {
		return uri;
	}

	public void setUri(String uri) {
		this.uri = uri;
	}

	public String getQueryString() {
		return queryString;
	}

	public void setQueryString(String queryString) {
		this.queryString = queryString;
	}
	

	public Exception getException() {
		return exception;
	}

	public void setException(Exception exception) {
		this.exception = exception;
	}

	public List<String> getSqlList() {
		return sqlList;
	}

	public void setSqlList(List<String> sqlList) {
		this.sqlList = sqlList;
	}

	public String getParams() {
		return params;
	}

	public void setParams(String params) {
		this.params = params;
	}

	public String getInJson() {
		return inJson;
	}

	public void setInJson(String inJson) {
		this.inJson = inJson;
	}

	public String getOutJson() {
		return outJson;
	}

	public void setOutJson(String outJson) {
		this.outJson = outJson;
	}

	public List<String> getOutModelList() {
		return outModelList;
	}

	public void setOutModelList(List<String> outModelList) {
		this.outModelList = outModelList;
	}

	public void addOutModel(String outModel) {
		this.outModelList.add(outModel);
	}

	public List<Long> getSqlSpendTime() {
		return sqlSpendTime;
	}

	public void setSqlSpendTime(List<Long> sqlSpendTime) {
		this.sqlSpendTime = sqlSpendTime;
	}
	
	public void addSqlSpendTime(long sqlSpendTime) {
		this.sqlSpendTime.add(sqlSpendTime);
	}
	
	public List<Long> getSqlCount() {
		return sqlCount;
	}

	public void setSqlCount(List<Long> sqlCount) {
		this.sqlCount = sqlCount;
	}
	
	public void addSqlCount(long sqlCount) {
		this.sqlCount.add(sqlCount);
	}

	public List<String> getOutScriptList() {
		return outScriptList;
	}

	public void setOutScriptList(List<String> outScriptList) {
		this.outScriptList = outScriptList;
	}

	
	public void addOutScript(String outScript) {
		this.outScriptList.add(outScript);
	}

	public int getTransactionTimes() {
		return transactionTimes;
	}

	public void setTransactionTimes(int transactionTimes) {
		this.transactionTimes = transactionTimes;
	}

	public int getGetConnTimes() {
		return getConnTimes;
	}

	public void setGetConnTimes(int getConnTimes) {
		this.getConnTimes = getConnTimes;
	}

	public int getReleaseConnTimes() {
		return releaseConnTimes;
	}

	public void setReleaseConnTimes(int releaseConnTimes) {
		this.releaseConnTimes = releaseConnTimes;
	}
	
	public void addTransactionTimes() {
		this.transactionTimes++;
	}

	public void addReleaseConnTimes() {
		this.releaseConnTimes++;
	}
	
	public void addGetConnTimes() {
		this.getConnTimes++;
	}

	public String getReceived() {
		return received;
	}

	public void setReceived(String received) {
		this.received = received;
	}

	public Map<Integer, List<Object>> getSqlArgMap() {
		return sqlArgMap;
	}

	public void setSqlArgMap(Map<Integer, List<Object>> sqlArgMap) {
		this.sqlArgMap = sqlArgMap;
	}
	
	public void addSqlArg(Integer index, Object[] args) {
		sqlArgMap.put(index, Arrays.asList(args));
	}
	
	public String getUserId() {
		return userId;
	}
	
	public void setUserId(String userId) {
		this.userId = userId;
	}



	public List<Date> getSqlBeginTime() {
		return sqlBeginTime;
	}

	public void setSqlBeginTime(List<Date> sqlBeginTime) {
		this.sqlBeginTime = sqlBeginTime;
	}
	
	public void addSqlBeginTime(Date sqlBeginTime) {
		this.sqlBeginTime.add(sqlBeginTime);
	}

	public String getCacheKey() {
		return cacheKey;
	}

	public void setCacheKey(String cacheKey) {
		this.cacheKey = cacheKey;
	}
	

}
