package com.ppx.cloud.monitor.persistence;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DebugAccessEntity extends AccessEntity {
	
	private List<String> sqlList = new ArrayList<String>();
	private Map<Integer, List<Object>> sqlArgMap = new HashMap<Integer, List<Object>>();
	private List<Long> sqlSpendTime = new ArrayList<Long>();
	private List<Date> sqlBeginTime = new ArrayList<Date>();
	private List<Long> sqlCount = new ArrayList<Long>();
	
	// (PropertiesConfig.isAccessDebugEnabled()控制)
	private String params;
	private String inJson;
	private String outJson;
	
	private List<String> outModelList = new ArrayList<String>();
	private List<String> outScriptList = new ArrayList<String>();
	private String received;

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

	public List<String> getSqlList() {
		return sqlList;
	}

	public void setSqlList(List<String> sqlList) {
		this.sqlList = sqlList;
	}

	public List<Long> getSqlSpendTime() {
		return sqlSpendTime;
	}

	public void setSqlSpendTime(List<Long> sqlSpendTime) {
		this.sqlSpendTime = sqlSpendTime;
	}

	public List<String> getOutModelList() {
		return outModelList;
	}

	public void setOutModelList(List<String> outModelList) {
		this.outModelList = outModelList;
	}

	public List<String> getOutScriptList() {
		return outScriptList;
	}

	public void setOutScriptList(List<String> outScriptList) {
		this.outScriptList = outScriptList;
	}

	public String getReceived() {
		return received;
	}

	public void setReceived(String received) {
		this.received = received;
	}

	public List<Long> getSqlCount() {
		return sqlCount;
	}

	public void setSqlCount(List<Long> sqlCount) {
		this.sqlCount = sqlCount;
	}

	public Map<Integer, List<Object>> getSqlArgMap() {
		return sqlArgMap;
	}

	public void setSqlArgMap(Map<Integer, List<Object>> sqlArgMap) {
		this.sqlArgMap = sqlArgMap;
	}

	public List<Date> getSqlBeginTime() {
		return sqlBeginTime;
	}

	public void setSqlBeginTime(List<Date> sqlBeginTime) {
		this.sqlBeginTime = sqlBeginTime;
	}

	
	
	
	
	
}
