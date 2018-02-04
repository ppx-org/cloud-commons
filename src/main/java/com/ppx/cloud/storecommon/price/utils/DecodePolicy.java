package com.ppx.cloud.storecommon.price.utils;

import org.springframework.util.StringUtils;

public class DecodePolicy {
	public static String decode(String policy) {
		if (StringUtils.isEmpty(policy)) {
			return "";
		}
		System.out.println("policy:" + policy);
		
		switch (policy.substring(0,1)) {
			case "%" : return policy.replace("%:0.", "") + "折";
			case "E" : return policy.replace("E:", "满").replace(",-:", "立减");
			case "B" : return policy.replace("B:", "买").replace(",F:", "免");
			case "+" : return policy.replace("+:", "加") + "元多1件";
			case "y" : return policy.replace("y:", "").replace(",n:", "元") + "件";
		}
		// %:0.95
//		if (policy.startsWith("%")) {
//			return policy.replace("%:0.", "") + "折";
//			
//			if (policy.indexOf("2:") > 0) {
//				
//			}
//			
//		}
		
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
