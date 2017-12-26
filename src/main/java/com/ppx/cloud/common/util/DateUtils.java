package com.ppx.cloud.common.util;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class DateUtils {
	
	public final static String DATE_PATTERN = "yyyy-MM-dd";
	
	public final static String TIME_PATTERN = "yyyy-MM-dd HH:mm:ss";
	
	public final static String MAX_DATE = "3000-01-01";
	
	public final static String MAX_DATE_DESC = "-";
	
	public static String today() {
		try {
			return new SimpleDateFormat(DATE_PATTERN).format(new Date());
		} catch (Exception e) {
			e.printStackTrace();
		}
		return "";
	}
	
	public static String tomorrow() {
		try {
			Calendar c = Calendar.getInstance();
			c.add(Calendar.DATE, 1);
			return new SimpleDateFormat(DATE_PATTERN).format(c.getTime());
		} catch (Exception e) {
			e.printStackTrace();
		}
		return "";
	}
	
	public static String getMaxDateDesc(Date date) {
		if (date == null) return "";
		
		String d = new SimpleDateFormat(DATE_PATTERN).format(date);
		if (MAX_DATE.equals(d)) {
			return MAX_DATE_DESC;
		}
		return d;
	}
	
}
