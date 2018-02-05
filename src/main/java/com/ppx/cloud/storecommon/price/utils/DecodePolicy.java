package com.ppx.cloud.storecommon.price.utils;

import org.springframework.util.StringUtils;

public class DecodePolicy {
	public static String decode(String policy) {
		if (StringUtils.isEmpty(policy)) {
			return "";
		}
		
		switch (policy.substring(0,1)) {
			case "%" : {
				if (policy.indexOf("%:1,2:") >= 0) {
					return policy.replace("%:1,2:0.", "第二件") + "折";
				}
				if (policy.indexOf("2:") >= 0) {
					return policy.replace("%:0.", "单件").replace(",2:0.", "折,第二件") + "折";
				}
				if (policy.indexOf("%:1,2+:") >= 0) {
					return policy.replace("%:1,2+:", "第二件及以上") + "折";
				}
				if (policy.indexOf("2+:") >= 0) {
					return policy.replace("%:0.", "第一件").replace(",2+:0.", "折,第二件及以上") + "折";
				}
				return policy.replace("%:0.", "") + "折";
			}
			case "S" : return policy.replace("S:", "特价") + "元";
			case "A" : return policy.replace("A:", "均价") + "元";
			case "E" : {
				if (policy.indexOf("C:") >= 0) {
					return policy.replace("E:", "满").replace(",C:", "元，换购价") + "元";
				}
				return policy.replace("E:", "满").replace(",-:", "立减");
			}
			case "B" : return policy.replace("B:", "买").replace(",F:", "免");
			case "+" : return policy.replace("+:", "加") + "元多1件";
			case "y" : return policy.replace("y:", "").replace(",n:", "元") + "件";
			case "D" : return "组合价" + policy.substring(policy.indexOf(",P:") + 3) + "元";
		}

		return "?";
			
	}
}




