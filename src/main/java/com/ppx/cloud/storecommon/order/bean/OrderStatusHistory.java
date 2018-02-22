package com.ppx.cloud.storecommon.order.bean;

import java.util.Date;

import com.ppx.cloud.common.jdbc.annotation.Id;

public class OrderStatusHistory {

	@Id
	private Integer historyId;
	
	private Integer orderId;
	
	private Integer historyStatus;
	
	private Date created;
	
	private Integer creator;
	
	private String openid;
	
	private String historyComment;

	public Integer getHistoryId() {
		return historyId;
	}

	public void setHistoryId(Integer historyId) {
		this.historyId = historyId;
	}

	public Integer getOrderId() {
		return orderId;
	}

	public void setOrderId(Integer orderId) {
		this.orderId = orderId;
	}

	public Integer getHistoryStatus() {
		return historyStatus;
	}

	public void setHistoryStatus(Integer historyStatus) {
		this.historyStatus = historyStatus;
	}

	public Date getCreated() {
		return created;
	}

	public void setCreated(Date created) {
		this.created = created;
	}

	public Integer getCreator() {
		return creator;
	}

	public void setCreator(Integer creator) {
		this.creator = creator;
	}

	public String getOpenid() {
		return openid;
	}

	public void setOpenid(String openid) {
		this.openid = openid;
	}

	public String getHistoryComment() {
		return historyComment;
	}

	public void setHistoryComment(String historyComment) {
		this.historyComment = historyComment;
	}
	
	
	
	
	
}
