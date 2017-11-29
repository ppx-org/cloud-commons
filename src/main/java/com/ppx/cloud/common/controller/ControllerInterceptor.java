package com.ppx.cloud.common.controller;

import java.util.Date;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import com.ppx.cloud.common.config.PropertiesConfig;
import com.ppx.cloud.common.exception.ErrorBean;
import com.ppx.cloud.common.exception.ErrorCode;
import com.ppx.cloud.common.exception.custom.NotFoundException;
import com.ppx.cloud.monitor.AccessLog;
import com.ppx.cloud.monitor.AccessUtils;

/**
 * 拦截器
 * 
 * @author dengxz
 * @date 2017年11月2日
 */
public class ControllerInterceptor implements HandlerInterceptor {

	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
			throws Exception {
		// 访问日志
		AccessLog accessLog = new AccessLog();

		accessLog.setBeginTime(new Date());
		accessLog.setIp(AccessUtils.getIpAddress(request));
		accessLog.setMethod(request.getMethod());
		accessLog.setUri(request.getRequestURI());
		accessLog.setQueryString(request.getQueryString());
		if (PropertiesConfig.isAccessDebugEnabled()) {
			accessLog.setParams(AccessUtils.getParams(request));
			accessLog.setReceived(ControllerContext.getAccessLog().getReceived());
		}
		ControllerContext.setAccessLog(accessLog);

		// 判断是否为404或模板错误
		Integer statusCode = (Integer) request.getAttribute("javax.servlet.error.status_code");

		// 不支持uri带.的请求
		if (request.getRequestURI().indexOf(".") > 0) {
			statusCode = 404;
		}

		if (statusCode != null && statusCode != 200) {
			String requestUri = (String) request.getAttribute("javax.servlet.forward.request_uri");
			accessLog.setUri(requestUri);

			String type = "Not Found";
			Exception exception = (Exception) request.getAttribute("javax.servlet.error.exception");
			if (exception == null) {
				exception = new NotFoundException();
			} else {
				ErrorBean c = ErrorCode.getErroCode(exception);
				type = c.getInfo();
			}

			String accept = request.getHeader("accept");
			if (accept != null && accept.indexOf("text/html") >= 0) {
				// html返回
				ControllerReturn.returnErrorHtml(response, statusCode, type + ":" + requestUri);
			} else {
				// json返回
				ControllerReturn.returnErrorJson(response, statusCode, type);
			}

			accessLog.setException(exception);
			accessLog.setSpendTime(System.currentTimeMillis() - accessLog.getBeginTime().getTime());
			AccessUtils.writeQueue(accessLog);
			return false;
		}

		return true;
	}

	public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler,
			ModelAndView modelAndView) throws Exception {

	}

	public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex)
			throws Exception {
		AccessLog accessLog = ControllerContext.getAccessLog();
		accessLog.setSpendTime(System.currentTimeMillis() - accessLog.getBeginTime().getTime());
		AccessUtils.writeQueue(accessLog);
	}
}