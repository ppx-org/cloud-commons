package com.ppx.cloud.common.exception;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.servlet.ModelAndView;

import com.ppx.cloud.common.controller.ControllerContext;
import com.ppx.cloud.common.controller.ControllerReturn;
import com.ppx.cloud.monitor.AccessLog;



/**
 * 自定义异常处理
 * 分类异常，不是所有的异常都要全部信息，如NoSuchRequestHandlingMethodException, MissingServletRequestParameterException
 * 和自定义的异常，不用打印到控制台，提高系统性能
 * @author dengxz
 * @date 2017年4月5日
 */
@ControllerAdvice
public class CustomExceptionHandler implements HandlerExceptionResolver {

	@ExceptionHandler(value = Exception.class)
	public ModelAndView resolveException(HttpServletRequest request, HttpServletResponse response, Object object,
			Exception exception) {
		
		AccessLog accessLog = ControllerContext.getAccessLog();
		if (accessLog == null) {
			exception.printStackTrace();
			return null;
		}
		accessLog.setException(exception);	
		
		// 以0开发头的异常，是不需要修改的代码，不打印到控制台输出
		ErrorBean error = ErrorCode.getErroCode(exception);
		// errorCode=0的异常，不需要修改后端代码，不打印
		if (error.getCode() != 0) {
			exception.printStackTrace();
		}
		response.setStatus(500);
		
		if ("PermissionUrlException".equals(error.getInfo()) || "PermissionParamsException".equals(error.getInfo())) {
			response.setStatus(403);
		}
		
		
		String accept = request.getHeader("accept");
		if (accept != null && accept.indexOf("text/html") >= 0) {
			ControllerReturn.returnErrorHtml(response, error.getCode(), error.getInfo());
		}
		else {			
			ControllerReturn.returnErrorJson(response, error.getCode(), error.getInfo());
		}
		
		return null;
	}

}
