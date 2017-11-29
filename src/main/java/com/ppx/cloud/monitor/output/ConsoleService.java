package com.ppx.cloud.monitor.output;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import org.springframework.util.StringUtils;

import com.ppx.cloud.common.exception.ErrorBean;
import com.ppx.cloud.common.exception.ErrorCode;
import com.ppx.cloud.monitor.AccessAnalysis;
import com.ppx.cloud.monitor.AccessLog;

/**
 * 控制台输出，在dev环境才打印
 * @author dengxz
 * @date 2017年11月13日
 */
public class ConsoleService {

	public static void print(AccessLog a) {
		List<String> infoList = new ArrayList<String>();

		String beginTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(a.getBeginTime());

		StringBuilder accessSb = new StringBuilder(a.getIp()).append("[").append(beginTime).append("]")
				.append(a.getSpendTime()).append(" ").append(a.getMethod()).append(" ").append(a.getUri());
		if (!StringUtils.isEmpty(a.getQueryString())) {
			accessSb.append("?").append(a.getQueryString());
		}
		
		// infoList.add("head:" + a.getReceived());

		infoList.add(accessSb.toString());
		if (!StringUtils.isEmpty(a.getParams())) {
			infoList.add("params:" + a.getParams());
		}

		if (a.getSqlList().size() > 0) {
			String sql = StringUtils.collectionToDelimitedString(a.getSqlList(), "\r\n        ");
			infoList.add("sqlText:" + sql);
			infoList.add("sqlArgs:" + a.getSqlArgMap());
			infoList.add("sqlSpendTime:" + a.getSqlSpendTime());
			infoList.add("sqlCount:" + a.getSqlCount());
		}

		if (!StringUtils.isEmpty(a.getInJson())) {
			infoList.add("inJson:" + a.getInJson());
		}

		if (!StringUtils.isEmpty(a.getOutJson())) {
			infoList.add("outJson:" + a.getOutJson());
		}
		if (a.getOutModelList().size() > 0) {
			infoList.add("outModel:" + a.getOutModelList());
		}
		if (a.getOutScriptList().size() > 0) {
			String outScript = StringUtils.collectionToDelimitedString(a.getOutScriptList(), "\r\n          ");
			infoList.add("outScript:" + outScript);
		}

		if (a.getException() != null) {
			ErrorBean error = ErrorCode.getErroCode(a.getException());
			infoList.add("Exception[" + error.getCode() + "]" + error.getInfo() + ":" + a.getException().getMessage());
		}

		// >>>>>>>>>>>>>> 警告信息 begin >>>>>>>>>>>>>>
		List<String> warnList = new ArrayList<String>();

		// 检查是否有未关闭的数据库连接
		String warn = AccessAnalysis.checkConnection(a.getGetConnTimes(), a.getReleaseConnTimes());
		if (!StringUtils.isEmpty(warn)) {
			warnList.add(warn);
		}

		// 检查for update有没有加上事务
		warn = AccessAnalysis.checkForUpdate(a.getSqlList(), a.getTransactionTimes());
		if (!StringUtils.isEmpty(warn)) {
			warnList.add(warn);
		}

		// 检查非安全SQL，没有加上where条件
		warn = AccessAnalysis.checkUnSafeSql(a.getSqlList());
		if (!StringUtils.isEmpty(warn)) {
			warnList.add(warn);
		}

		// 检查事务个数是否大于1
		warn = AccessAnalysis.checkTransactionTimes(a.getTransactionTimes());
		if (!StringUtils.isEmpty(warn)) {
			warnList.add(warn);
		}

		// 检查多个操作SQL是否没有使用事务
		warn = AccessAnalysis.checkNoTransaction(a.getSqlList(), a.getTransactionTimes());
		if (!StringUtils.isEmpty(warn)) {
			warnList.add(warn);
		}

		// 检查重复SQL
		warn = AccessAnalysis.checkDuplicateSql(a.getSqlList());
		if (!StringUtils.isEmpty(warn)) {
			warnList.add(warn);
		}

		// 检查返回Json是否规范
		warn = AccessAnalysis.checkOutJson(a.getOutJson());
		if (!StringUtils.isEmpty(warn)) {
			warnList.add(warn);
		}
		
		// 检查非安全SQL，没有加上where条件
		warn = AccessAnalysis.checkAntiSql(a.getSqlList());
		if (!StringUtils.isEmpty(warn)) {
			warnList.add(warn);
		}
		
		// 
		
	
		// <<<<<<<<<<<<<< 警告信息 end <<<<<<<<<<<<<<

		debugToConsole(infoList, warnList);
	}
	
	private static void debugToConsole(List<String> infoList, List<String> warnList) {
		StringBuilder sb = new StringBuilder();
		sb.append(StringUtils.collectionToDelimitedString(infoList, "\r\n"));
		System.out.println(sb);
		if (warnList.size() > 0) {
			StringBuilder warn = new StringBuilder();
			warn.append("警告信息>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>\r\n");
			warn.append(StringUtils.collectionToDelimitedString(warnList, "\r\n"));
			System.out.println(warn.append("\r\n"));
		}
	}
}
