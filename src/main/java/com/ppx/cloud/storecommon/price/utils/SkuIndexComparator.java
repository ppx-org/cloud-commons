package com.ppx.cloud.storecommon.price.utils;

import java.util.Comparator;

import com.ppx.cloud.storecommon.price.bean.SkuIndex;

public class SkuIndexComparator implements Comparator<SkuIndex> {

	@Override
	public int compare(SkuIndex o1, SkuIndex o2) {
		if (o1.getProgId() < o2.getProgId()) {
			return -1;
		}
		else if (o1.getProgId() > o2.getProgId()) {
			return 1;
		}
		else {
			if (o1.getPrice() > o2.getPrice()) {
				return -1;
			}
			else if (o1.getPrice() < o2.getPrice()) {
				return 1;
			}
		}
		return 0;
	}
	
	
}
