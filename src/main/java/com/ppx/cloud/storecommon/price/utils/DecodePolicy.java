package com.ppx.cloud.storecommon.price.utils;

import org.springframework.util.StringUtils;

public class DecodePolicy {
	public static String decode(String policy) {
		if (StringUtils.isEmpty(policy)) {
			return "";
		}
		
		return policy
			.replace("S:", "special")
			.replace("C:", "change")
			.replace("A:", "avg")
			.replace("%:", "disc")
			.replace("2:", "second")
			.replace("2+:", "secondAndMore")
			.replace("E:", "E¥")
			.replace("-:", "M¥")
			.replace(":Y", "yen")
			.replace(":N", "件")
			.replace("+:", "add¥")
			.replace("m:1", "MoreOne")
			.replace(":", "").replace(",", "");
			
	}
	
}
