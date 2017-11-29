package com.ppx.cloud.common.exception;

import org.springframework.beans.TypeMismatchException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.jdbc.CannotGetJdbcConnectionException;
import org.springframework.transaction.CannotCreateTransactionException;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.util.NestedServletException;

import com.ppx.cloud.common.exception.custom.DateException;
import com.ppx.cloud.common.exception.custom.JsonException;
import com.ppx.cloud.common.exception.custom.NotFoundException;
import com.ppx.cloud.common.exception.custom.PermissionParamsException;
import com.ppx.cloud.common.exception.custom.PermissionResubmitException;
import com.ppx.cloud.common.exception.custom.PermissionUrlException;



/**
 * 分类异常，不是所有的异常都要全部信息
 *  9:紧急异常，需要紧急处理，打印
 *  0:不需要修改后端代码，不打印
 *  1:java异常，需要修改，打印
 *  2:jdbc部分异常，需要修改，打印
 *  3:模板错误
 *  4:mongodb异常
 *  -1:未知异常，打印
 * @author dengxz
 * @date 2017年4月1日
 */
public class ErrorCode {

	public static ErrorBean getErroCode(Exception e) {
		
		// 不需要修改后端代码，不输出异常，只在访问日志中显示
		if (e instanceof NotFoundException) {
			return new ErrorBean(0, e.getClass().getSimpleName());
		}
		else if (e instanceof MissingServletRequestParameterException) {
			// 找到uri，参数缺少			
			return new ErrorBean(0, e.getClass().getSimpleName());
		} else if (e instanceof TypeMismatchException) {
			// 找到uri和参数，参数类型不匹配
			return new ErrorBean(0, e.getClass().getSimpleName());
		} else if (e instanceof EmptyResultDataAccessException) {
			// 单条记录查询结果为空,防攻击时打印大量错误日志
			// Spring中使用JdbcTemplate的queryForObject方法，当查不到数据时会抛出如下异常：EmptyResultDataAccessException
			return new ErrorBean(0, e.getClass().getSimpleName());
		} else if (e instanceof BindException) {
			String msg = e.getClass().getSimpleName();
			// 参数类型错误或 查询每页记录超过最大数时
			int i = e.getMessage().indexOf("PermissionMaxPageSizeException");
			msg = (i >= 0) ? "maxPageSize forbidden" : msg;
			return new ErrorBean(0, msg);
		} else if (e instanceof PermissionResubmitException) {
			// 重复提交异常
			return new ErrorBean(0, e.getClass().getSimpleName());
		} else if (e instanceof PermissionUrlException) {
			// 权限异常,超权url时报的异常
			return new ErrorBean(0, e.getClass().getSimpleName());
		} else if (e instanceof PermissionParamsException) {
			// 权限异常,参数超权的报的异常，如修改不是自己数据时报的异常（开发人员需要关注）
			return new ErrorBean(0, e.getClass().getSimpleName());
		}
		
		
		// 紧急异常，需要紧急处理
		else if (e instanceof CannotGetJdbcConnectionException) {
			// 数据库连接不上(如没有启动，或死掉)
			return new ErrorBean(9, e.getClass().getSimpleName() + ":" + e.getMessage());
		} else if (e instanceof CannotCreateTransactionException) {
			// 数据库连接池已满
			return new ErrorBean(9, e.getClass().getSimpleName());
		}
		if (e instanceof NestedServletException) {
			String msg = e.getMessage();
			// 判断是否内存溢出
			// e.getMessage().indexOf("java.lang.OutOfMemoryError");
			
			// 判断是否找不到类
			// e.getMessage().indexOf("java.lang.NoClassDefFoundError");
			if (msg.indexOf("org.thymeleaf.exceptions.TemplateProcessingException") >= 0) {
				return new ErrorBean(3, "TemplateProcessingException");
			}
			return new ErrorBean(9, msg);
		}
		
		
		
		// >>>>>>>>>>>>>>>java异常
		else if (e instanceof DateException) {
			// 日期处理异常
			return new ErrorBean(1, e.getClass().getSimpleName());
		}
		else if (e instanceof JsonException) {
			// json处理异常
			return new ErrorBean(1, e.getClass().getSimpleName());
		}
		else if ("java.lang".equals(e.getClass().getPackage().getName())) {
			// java异常 NullPointerException等
			return new ErrorBean(1, e.getClass().getSimpleName());
		}		
		
		// >>>>>>>>>>>>>>>jdbc异常
		else if ("org.springframework.jdbc".equals(e.getClass().getPackage().getName())) {
			// 表不存，sql语法错误
			return new ErrorBean(2, e.getClass().getSimpleName());
		}
		else if ("org.springframework.dao".equals(e.getClass().getPackage().getName())) {
			// 必须栏插入空值，重复数据，Integer field返回int
			return new ErrorBean(2, e.getClass().getSimpleName());
		}
		
		// >>>>>>>>>>>>>mongodb异常
		else if ("org.springframework.data.mongodb".equals(e.getClass().getPackage().getName())) {
			// 必须栏插入空值，重复数据，Integer field返回int
			return new ErrorBean(4, e.getClass().getSimpleName());
		}
		
		
		// >>>>>>>>>>>>>>>>>需要接收json参数 如：@RequestBody TestBean testBean
		else if (e instanceof HttpMessageNotReadableException) {
			return new ErrorBean(0, e.getClass().getSimpleName());
		}
		return new ErrorBean(-1, e.getClass().getSimpleName());
	}
}
