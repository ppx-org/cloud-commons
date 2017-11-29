package com.ppx.cloud.monitor.console;

import java.io.Serializable;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;

public class SpringDataPageable implements Serializable, Pageable {
	private static final long serialVersionUID = 1;
	// 当前页
	private Integer pageNumber = 1;
	// 当前页面条数
	private Integer pageSize = 15;
	// 排序条件
	private Sort sort;

	private long totalRows;
	
	private String orderName;

	private String orderType;

	// 当前页面
	@Override
	public int getPageNumber() {
		return pageNumber;
	}

	// 每一页显示的条数
	@Override
	public int getPageSize() {
		return pageSize;
	}

	// 第二页所需要增加的数量
	@Override
	public long getOffset() {
		return (getPageNumber() - 1) * getPageSize();
	}

	@Override
	public Sort getSort() {
		return sort;
	}

	public void setPageNumber(Integer pageNumber) {
		this.pageNumber = pageNumber;
	}

	public void setPageSize(Integer pageSize) {
		this.pageSize = pageSize;
	}

	public void setSort(Sort sort) {
		this.sort = sort;
	}

	@Override
	public Pageable first() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean hasPrevious() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public Pageable next() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Pageable previousOrFirst() {
		// TODO Auto-generated method stub
		return null;
	}

	public long getTotalRows() {
		return totalRows;
	}

	public void setTotalRows(long totalRows) {
		this.totalRows = totalRows;
	}
	
	public String getOrderName() {
		return orderName;
	}

	public void setOrderName(String orderName) {
		this.orderName = orderName;
	}

	public String getOrderType() {
		return orderType;
	}

	public void setOrderType(String orderType) {
		this.orderType = orderType;
	}
	
	public Direction getOrderDirection() {
		if ("desc".equals(this.orderType)) {
			return Direction.DESC;
		}
		else if ("asc".equals(this.orderType)) {
			return Direction.ASC;
		}
		else return null;
	}

}