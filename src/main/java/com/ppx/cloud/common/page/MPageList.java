package com.ppx.cloud.common.page;

import java.util.ArrayList;
import java.util.List;




public class MPageList<T> {

	private List<T> list = new ArrayList<T>();
	
	private MPage page = new MPage();
	
	public MPageList() {
		
	}
	
	public MPageList(List<T> list, MPage page) {
		this.list = list;
		this.page = page;
	}

	public List<T> getList() {
		return list;
	}

	public void setList(List<T> list) {
		this.list = list;
	}

	public MPage getPage() {
		return page;
	}

	public void setPage(MPage page) {
		this.page = page;
	}
	
	
	
}
