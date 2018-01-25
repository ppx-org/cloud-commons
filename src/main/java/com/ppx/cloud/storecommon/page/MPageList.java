package com.ppx.cloud.storecommon.page;

import java.util.ArrayList;
import java.util.List;


/**
 * 
 * @author dengxz
 * @date 2017年11月14日
 */
public class MPageList<T> {

	private List<T> list = new ArrayList<T>();
	
	private MQueryPage page = new MQueryPage();
	
	public MPageList() {
		
	}
	
	public MPageList(List<T> list, MQueryPage page) {
		this.list = list;
		this.page = page;
	}

	public List<T> getList() {
		return list;
	}

	public void setList(List<T> list) {
		this.list = list;
	}

	public MQueryPage getPage() {
		return page;
	}

	public void setPage(MQueryPage page) {
		this.page = page;
	}
	
	
	
}
