package com.ppx.cloud.common.page;

public class LimitRecord {

	private String colName;

	private Object colValue;

	public LimitRecord(String colName, Object colValue) {
		this.colName = colName;
		this.colValue = colValue;
	}

	public String getColName() {
		return colName;
	}

	public void setColName(String colName) {
		this.colName = colName;
	}

	public Object getColValue() {
		return colValue;
	}

	public void setColValue(Object colValue) {
		this.colValue = colValue;
	}

}
