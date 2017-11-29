package com.ppx.cloud.common.page;

import java.util.ArrayList;
import java.util.List;


/**
 * 
 * @author dengxz
 * @date 2017年11月14日
 */
public class PageList<T> {

	private List<T> list = new ArrayList<T>();
	
	private Page page = new Page();
	
	public PageList() {
		
	}
	
	public PageList(List<T> list, Page page) {
		this.list = list;
		this.page = page;
	}

	public List<T> getList() {
		return list;
	}

	public void setList(List<T> list) {
		this.list = list;
	}

	public Page getPage() {
		return page;
	}

	public void setPage(Page page) {
		this.page = page;
	}
	
	
	
}
