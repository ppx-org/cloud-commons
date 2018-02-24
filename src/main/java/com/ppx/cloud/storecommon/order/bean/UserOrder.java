package com.ppx.cloud.storecommon.order.bean;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.ppx.cloud.common.jdbc.annotation.Column;
import com.ppx.cloud.common.jdbc.annotation.Table;

@Table(name="user_order")
public class UserOrder {
	
	private Integer orderId;
	
	private String openid;
	
	private Integer storeId;
	
	private Date orderTime;
	
	private Integer orderStatus;
	
	private Float orderPrice;
	
	private Float payPrice;
	
	private String deliverCode;
	
	@Column(readonly=true)
	private List<OrderItem> listItem = new ArrayList<OrderItem>();

	public Integer getOrderId() {
		return orderId;
	}

	public void setOrderId(Integer orderId) {
		this.orderId = orderId;
	}

	public String getOpenid() {
		return openid;
	}

	public void setOpenid(String openid) {
		this.openid = openid;
	}

	public Integer getStoreId() {
		return storeId;
	}

	public void setStoreId(Integer storeId) {
		this.storeId = storeId;
	}

	public Date getOrderTime() {
		return orderTime;
	}

	public void setOrderTime(Date orderTime) {
		this.orderTime = orderTime;
	}

	public Integer getOrderStatus() {
		return orderStatus;
	}

	public void setOrderStatus(Integer orderStatus) {
		this.orderStatus = orderStatus;
	}

	public Float getOrderPrice() {
		return orderPrice;
	}

	public void setOrderPrice(Float orderPrice) {
		this.orderPrice = orderPrice;
	}

	public Float getPayPrice() {
		return payPrice;
	}

	public void setPayPrice(Float payPrice) {
		this.payPrice = payPrice;
	}

	public List<OrderItem> getListItem() {
		return listItem;
	}

	public void setListItem(List<OrderItem> listItem) {
		this.listItem = listItem;
	}
	
	public void addOrderItem(OrderItem orderItem) {
		listItem.add(orderItem);
	}

	public String getDeliverCode() {
		return deliverCode;
	}

	public void setDeliverCode(String deliverCode) {
		this.deliverCode = deliverCode;
	}
	
}
