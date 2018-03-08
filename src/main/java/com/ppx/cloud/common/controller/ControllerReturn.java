package com.ppx.cloud.common.controller;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletResponse;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ppx.cloud.common.page.MPageList;
import com.ppx.cloud.common.page.PageList;

/**
 * 统一返回JSON、成功失败页面
 * actionStatus:OK表示处理成功，FAIL表示失败，如果为FAIL，ErrorInfo带上失败原因
 * errorCode:0为成功，其他为失败
 * errorInfo:失败原因
 * @author dengxz
 * @date 2017年11月4日
 */
public class ControllerReturn {
	
	private static Map<String, Object> staticMap = new HashMap<String, Object>();
	static {
		staticMap.put("actionStatus", "OK");
		staticMap.put("errorCode", "0");
		staticMap.put("result", "1");
	}
	
	
	/**
	 * 成功时返回的json 
	 * @param obj 默认使用对象名称，需要改名可以使用Map<String, Object>传入
	 * @return
	 */
	public static Map<String, Object> ok(Object... obj) {
		if (obj == null || "1".equals(obj.toString())) {
			return staticMap;
		}
		
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("actionStatus", "OK");
		map.put("errorCode", "0");
		for (Object o : obj) {
			if (o instanceof PageList) {
				PageList<?> pl = (PageList<?>)o;
				String keyName = pl.getList().getClass().getSimpleName();
				keyName = keyName.substring(0,1).toLowerCase() + keyName.substring(1);
				map.put(keyName, pl.getList());
				
				keyName = pl.getPage().getClass().getSimpleName();
				keyName = keyName.substring(0,1).toLowerCase() + keyName.substring(1);
				map.put(keyName, pl.getPage());
			}
			else if (o instanceof MPageList) {
				MPageList<?> pl = (MPageList<?>)o;
				String keyName = pl.getList().getClass().getSimpleName();
				keyName = keyName.substring(0,1).toLowerCase() + keyName.substring(1);
				map.put(keyName, pl.getList());
				
				keyName = pl.getPage().getClass().getSimpleName();
				keyName = keyName.substring(0,1).toLowerCase() + keyName.substring(1);
				map.put(keyName, pl.getPage());
			}
			else if (o instanceof Map) {
				@SuppressWarnings("unchecked")
				Map<String, Object> m = (Map<String, Object>) o;
				Set<String> set = m.keySet();
				for (String key : set) {
					map.put(key, m.get(key));
				}
			} else {
				String keyName = o.getClass().getSimpleName();
				keyName = "Integer".equals(keyName) || "Long".equals(keyName) || "Float".equals(keyName) || "Double".equals(keyName) ? "result" : keyName;
				keyName = keyName.substring(0,1).toLowerCase() + keyName.substring(1);
				map.put(keyName, o);
			}
		}
		return map;
	}
	
	/**
	 * 失败时返回的json 
	 * @param obj
	 * @return
	 */
	public static Map<String, Object> fail(int errorCode, String errorInfo) {
		Map<String, Object> m = new HashMap<String, Object>(3);
		m.put("actionStatus", "FAIL");
		m.put("errorCode", errorCode);
		m.put("errorInfo", errorInfo);
		return m;
	}
	
	/**
	 * 返回错误(HMTL格式)
	 * @param response
	 * @param errorCode
	 * @param errorInfo
	 */
	public static void returnErrorHtml(HttpServletResponse response, Integer errorCode, String errorInfo) {
		try (PrintWriter printWriter = response.getWriter()) {
			printWriter.write("[" + errorCode + "]" + "System Message[" + errorInfo + "]");
		} catch (Exception e) {	
			e.printStackTrace();
		} 
	}
	
	/**
	 * 返回错误(JSON格式)
	 * @param response
	 * @param errorCode
	 * @param errorInfo
	 */
	public static void returnErrorJson(HttpServletResponse response, Integer errorCode, String errorInfo) {
		response.setCharacterEncoding("UTF-8");
		response.setContentType("application/json");
		Map<String, Object> map = ControllerReturn.fail(errorCode, errorInfo);
		try (PrintWriter printWriter = response.getWriter()) {
			String returnJson = new ObjectMapper().writeValueAsString(map);
			printWriter.write(returnJson);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static void returnJson(HttpServletResponse response, String json) {
		response.setCharacterEncoding("UTF-8");
		response.setContentType("application/json");
		try (PrintWriter printWriter = response.getWriter()) {
			printWriter.write(json);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
