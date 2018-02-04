package com.ppx.cloud.storecommon.price.utils;

import org.springframework.util.StringUtils;

public class DecodePolicy {
	public static String decode(String policy) {
		if (StringUtils.isEmpty(policy)) {
			return "";
		}
		
		return policy
			.replace("S:", "特价")
			.replace("C:", "change")
			.replace("A:", "avg")
			.replace("%:", "折扣")
			.replace("2:", "second")
			.replace("2+:", "secondAndMore")
			.replace("E:", "满")
			.replace("-:", "立减")
			.replace(":Y", "yen")
			.replace(":N", "件")
			.replace("+:", "add¥")
			.replace("m:1", "MoreOne")
			.replace(":", "").replace(",", "");
			
	}
	
}
