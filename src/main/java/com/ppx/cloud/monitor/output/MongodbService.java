package com.ppx.cloud.monitor.output;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.mongodb.core.FindAndModifyOptions;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.index.Index;
import org.springframework.data.mongodb.core.index.IndexOperations;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.DBObject;
import com.mongodb.util.JSON;
import com.ppx.cloud.common.config.PropertiesConfig;
import com.ppx.cloud.common.exception.ErrorBean;
import com.ppx.cloud.common.exception.ErrorCode;
import com.ppx.cloud.monitor.AccessLog;
import com.ppx.cloud.monitor.AccessUtils;
import com.ppx.cloud.monitor.persistence.AccessEntity;
import com.ppx.cloud.monitor.persistence.DebugAccessEntity;

/**
 * 输出到monogdb
 * @author dengxz
 * @date 2017年11月13日
 */
@Service
public class MongodbService {
	
	@Autowired
	private MongoTemplate mongoTemplate;

	public void access(AccessEntity access, AccessLog a) {
		// 正常访问日志(access_)
		access.setBeginTime(a.getBeginTime());
		access.setSpendTime(a.getSpendTime());
		access.setIp(a.getIp());
		access.setUri(a.getUri());
		access.setQueryString(a.getQueryString());
		access.setUserId(a.getUserId());
		
		long useMemory = (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / 1024 / 1024;
		access.setUseMemory(useMemory);
		
		access.setServiceId(AccessUtils.getServiceId());
		
		// 访问日志里异常处理
		if (a.getException() != null) {
			ErrorBean error = ErrorCode.getErroCode(a.getException());
			error.setInfo(error.getInfo() + ":" + a.getException().getMessage());
			access.setError(error);
		}
		mongoTemplate.insert(access, "access_" + new SimpleDateFormat("yyyy-MM-dd").format(new Date()));
	}
	
	public void error(AccessEntity access, DebugAccessEntity debugAccess, AccessLog a) {
		ErrorBean error = ErrorCode.getErroCode(a.getException());
		// 类型为0的异常不处理
		if (error.getCode() == 0) {
			return;
		}
			
		// 存放少量异常信息com.ppx开头
		List<StackTraceElement> listStack = new ArrayList<StackTraceElement>();
		StackTraceElement[] stackTraceElement = a.getException().getStackTrace();
		for (StackTraceElement element : stackTraceElement) {
			if (element.getClassName().startsWith("com.ppx") && element.getLineNumber() > 0) {
				listStack.add(element);
			}
		}
		
		Exception myException = null;
		// 如果没有找到com.ppx的异常，则打印所有
		if (!listStack.isEmpty()) {
			myException = new Exception(
					a.getException().getClass().getName() + "" + a.getException().getMessage());
			myException.setStackTrace(listStack.toArray(new StackTraceElement[listStack.size()]));
		} else {
			myException = a.getException();
		}
		
		Map<String, Object> errorMap = new HashMap<String, Object>();
		ObjectId _id = new ObjectId();
		
		errorMap.put("_id", _id);
		errorMap.put("serviceId", AccessUtils.getServiceId());
		errorMap.put("beginTime", a.getBeginTime());
		errorMap.put("uri", a.getUri());
		errorMap.put("quaryString", a.getQueryString());
		errorMap.put("code", error.getCode());
		errorMap.put("msg", myException.getMessage());
		mongoTemplate.insert(errorMap, "error");
		
		DBObject myExceptionBson  = null;
		try {
			String myExceptionJson = new ObjectMapper().writeValueAsString(myException);
			myExceptionBson = (DBObject)JSON.parse(myExceptionJson);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		
		Map<String, Object> detailErrorMap = new HashMap<String, Object>();
		detailErrorMap.put("_id", _id);
		detailErrorMap.put("exception", myExceptionBson);
		if (!PropertiesConfig.isAccessDebugEnabled()) {
			setDebugAccess(debugAccess, a);
		} 
		debugAccess.setError(null);
		detailErrorMap.put("access", debugAccess);
		mongoTemplate.insert(detailErrorMap, "error_detail");
	}

	/**
	 * uri统计
	 * @param a
	 */
	public void uriStat(AccessLog a) {
		Query query = new Query();
		Criteria criteria = Criteria.where("_id").is(a.getUri());
		query.addCriteria(criteria);

		Update update = new Update();
		update.inc("times", 1);
		update.inc("totalTime", a.getSpendTime());
		update.max("maxTime", a.getSpendTime());
		update.setOnInsert("firsted", a.getBeginTime());
		update.set("lasted", a.getBeginTime());
		distribute(update, a.getSpendTime());

		// 插入或更新数据
	  	Map<?, ?> map = mongoTemplate.findAndModify(query, update, FindAndModifyOptions.options().upsert(true).returnNew(true),
				Map.class, "uri_stat");
	  	
	  	// 更新平均值
	  	Update newUpdate = new Update();
	  	newUpdate.set("avgTime", (Long)map.get("totalTime") / (Integer)map.get("times"));
	  	// 最大详情
	  	if ((Long)map.get("maxTime") == a.getSpendTime()) {
	  		// 最大值对应对象
	  		Map<String, Object> maxMap = new LinkedHashMap<String, Object>();
	  		maxMap.put("serviceId", AccessUtils.getServiceId());
	  		maxMap.put("queryString", a.getQueryString());
	  		maxMap.put("maxed", a.getBeginTime());
	  		maxMap.put("sqlList", a.getSqlList());
	  		maxMap.put("sqlCount", a.getSqlCount());
	  		maxMap.put("sqlArgMap", a.getSqlArgMap());
	  		newUpdate.set("maxDetail", maxMap);	  		
	  	}
	  	mongoTemplate.updateFirst(query, newUpdate, "uri_stat");	
	}
	
	/**
	 * sql统计
	 * @param a
	 */
	public void sqlStat(AccessLog a) {
		// 一个请求对应多个sql
		for (int i = 0; i < a.getSqlList().size(); i++) {
			Query query = new Query();
			Criteria criteria = Criteria.where("_id").is(a.getSqlList().get(i));
			query.addCriteria(criteria);

			Update update = new Update();
			update.inc("times", 1);
			
			// sql开始执行时间
			Date sqlBeginTime = null;
			if (a.getSqlList().size() == a.getSqlBeginTime().size()) {
				sqlBeginTime = a.getSqlBeginTime().get(i);
			}
			update.setOnInsert("firsted", sqlBeginTime);			
			update.set("lasted", sqlBeginTime);
			
			// sql执行时间
			long spendTime = 0;
			if (a.getSqlList().size() == a.getSqlSpendTime().size()) {
				spendTime = a.getSqlSpendTime().get(i);
			}
			update.inc("totalTime", spendTime);
			update.max("maxTime", spendTime);
			distribute(update, spendTime);
			
			// sql结果集
			if (a.getSqlList().size() == a.getSqlCount().size()) {
				update.max("maxSqlCount", a.getSqlCount().get(i));
	  		}
			
			// uri
			update.addToSet("uri", a.getUri());

			// 插入或更新数据
		  	Map<?, ?> map = mongoTemplate.findAndModify(query, update, FindAndModifyOptions.options().upsert(true).returnNew(true),
					Map.class, "sql_stat");
		  	
		  	// 更新平均值
		  	Update newUpdate = new Update();
		  	newUpdate.set("avgTime", (Long)map.get("totalTime") / (Integer)map.get("times"));
		  	// 最大详情
		  	if ((Long)map.get("maxTime") >= spendTime) {
		  		// 最大值对应对象
		  		Map<String, Object> maxMap = new LinkedHashMap<String, Object>();
		  		maxMap.put("serviceId", AccessUtils.getServiceId());
		  		maxMap.put("uri", a.getUri());
		  		maxMap.put("queryString", a.getQueryString());
		  		maxMap.put("maxed", sqlBeginTime);
		  		if (a.getSqlList().size() == a.getSqlCount().size()) {
		  			maxMap.put("sqlCount", a.getSqlCount().get(i));
		  		}
		  		maxMap.put("sqlArgMap", a.getSqlArgMap().get(i));
		  		  		
		  		newUpdate.set("maxDetail", maxMap);	  		
		  	}
		  	mongoTemplate.updateFirst(query, newUpdate, "sql_stat");	
		}
	}
	
	public void response(AccessLog a) {
		String HH = new SimpleDateFormat("yyyyMMddHH").format(new Date());
		
		// 有异常和静态uri不统计
		// 机器ID yyyyMMddHH小时 访问量 总时间
		Query query = new Query();
		Criteria criteria = Criteria.where("serviceId")
				.is(AccessUtils.getServiceId()).and("HH").is(HH);
		query.addCriteria(criteria);
		
		Update update = new Update();
		update.inc("times", 1);
		update.inc("totalTime", a.getSpendTime());		
		
		// 插入或更新数据
	  	Map<?, ?> map = mongoTemplate.findAndModify(query, update, FindAndModifyOptions.options().upsert(true).returnNew(true),
				Map.class, "response");
	  	
	  	// 更新平均值
	  	Update newUpdate = new Update();
	  	long avgTime = (Long)map.get("totalTime") / (Integer)map.get("times");
	  	newUpdate.set("avgTime", avgTime);
	  	mongoTemplate.updateFirst(query, newUpdate, "response");
	  	
	  	// 更新到service
	  	Update serviceUpdate = new Update();
	  	serviceUpdate.set("lastResponse", avgTime);
	  	upsertService(AccessUtils.getServiceId(), serviceUpdate);
	}
	
	public void debug(DebugAccessEntity debugAccess) {
		mongoTemplate.insert(debugAccess, "debug");
	}
	
	public void setDebugAccess(DebugAccessEntity debugAccess, AccessLog a) {	
		
		debugAccess.setBeginTime(a.getBeginTime());
		debugAccess.setSpendTime(a.getSpendTime());
		debugAccess.setIp(a.getIp());
		debugAccess.setUri(a.getUri());
		debugAccess.setQueryString(a.getQueryString());
		debugAccess.setUserId(a.getUserId());
		debugAccess.setServiceId(AccessUtils.getServiceId());
		
		long useMemory = (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / 1024 / 1024;
		debugAccess.setUseMemory(useMemory);
		
		debugAccess.setServiceId(AccessUtils.getServiceId());
		// 访问日志里异常处理
		if (a.getException() != null) {
			ErrorBean error = ErrorCode.getErroCode(a.getException());
			error.setInfo(error.getInfo() + ":" + a.getException().getMessage());
			debugAccess.setError(error);
		}

		// SQL部分
		debugAccess.setSqlList(a.getSqlList().isEmpty() ? null : a.getSqlList());
		debugAccess.setSqlArgMap(a.getSqlArgMap().isEmpty() ? null : a.getSqlArgMap());
		debugAccess.setSqlSpendTime(a.getSqlSpendTime().isEmpty() ? null : a.getSqlSpendTime());
		debugAccess.setSqlCount(a.getSqlCount().isEmpty() ? null : a.getSqlCount());
		debugAccess.setSqlBeginTime(a.getSqlBeginTime().isEmpty() ? null : a.getSqlBeginTime());

		// PropertiesConfig.isAccessDebugEnabled()
		debugAccess.setParams(StringUtils.isEmpty(a.getParams()) ? null : a.getParams());
		debugAccess.setInJson(StringUtils.isEmpty(a.getInJson()) ? null : a.getInJson());
		debugAccess.setOutJson(StringUtils.isEmpty(a.getOutJson()) ? null : a.getOutJson());
		debugAccess.setOutModelList(a.getOutModelList().isEmpty() ? null : a.getOutModelList());
		debugAccess.setOutScriptList(a.getOutScriptList().isEmpty() ? null : a.getOutScriptList());
		debugAccess.setReceived(StringUtils.isEmpty(a.getReceived()) ? null : a.getReceived());
	}

	private void distribute(Update update, long t) {
		if (t < 10) {
			update.inc("ms0_10", 1);
		} else if (t < 100) {
			update.inc("ms10_100", 1);
		} else if (t < 1000) {
			update.inc("ms100_s1", 1);
		} else if (t < 3000) {
			update.inc("s1_3", 1);
		} else if (t < 10000) {
			update.inc("s3_10", 1);
		} else {
			update.inc("s10_", 1);
		}
	}
	
	// 启动日志
	public void insertStartLog(Map<String, Object> map) {		
		mongoTemplate.insert(map, "start");
	}
	
	// 机器信息
	public void upsertService(String serviceId, Update update) {
		Query query = new Query();
		Criteria criteria = Criteria.where("_id").is(serviceId);
		query.addCriteria(criteria);
		
		mongoTemplate.upsert(query, update, "service");
	}
	
	// 采集日志
	public void insertGatherLog(Map<String, Object> map) {		
		mongoTemplate.insert(map, "gather");
	}
	
	// warning
	public void warning(AccessLog a, BitSet content) {		
		Query query = new Query();
		Criteria criteria = Criteria.where("serviceId").is(AccessUtils.getServiceId())
				.and("uri").is(a.getUri());
		query.addCriteria(criteria);
		
		Update update = new Update();
		update.setOnInsert("beginTime", a.getBeginTime());
		update.set("lasted", a.getBeginTime());
		update.bitwise("content").or(content.toLongArray()[0]);
		
		mongoTemplate.upsert(query, update, "warning");
	}
	
	public void updateConfig(String serviceId, Update update) {
		Query query = new Query();
		query.addCriteria(Criteria.where("_id").is(serviceId));
		mongoTemplate.upsert(query, update, "config");
	}
	
	public Map<?, ?> getConfig(String serviceId){
		Query query = new Query();
		query.addCriteria(Criteria.where("_id").is(serviceId));
		return mongoTemplate.findOne(query, Map.class, "config");
	}
	
	private static String lastIndexDate = "";
	// 创建access索引 
	public void createAccessIndex() {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		String today = sdf.format(new Date());
		if (!MongodbService.lastIndexDate.equals(today)) {
			// access索引
			IndexOperations accessOp = mongoTemplate.indexOps("access_" + today);
			accessOp.ensureIndex(new Index().on("serviceId", Direction.DESC));
			accessOp.ensureIndex(new Index().on("beginTime", Direction.DESC));
			accessOp.ensureIndex(new Index().on("uri", Direction.DESC));
			accessOp.ensureIndex(new Index().on("spendTime", Direction.DESC));
			MongodbService.lastIndexDate = today;
		}
	}
	
	// 创建(不包括access)
	public void createIndex() {		
		// error索引
		IndexOperations errorOp = mongoTemplate.indexOps("error");
		errorOp.ensureIndex(new Index().on("serviceId", Direction.DESC));
		errorOp.ensureIndex(new Index().on("beginTime", Direction.DESC));
		errorOp.ensureIndex(new Index().on("code", Direction.DESC));		
	
		// gather索引
		IndexOperations gatherOp = mongoTemplate.indexOps("gather");
		gatherOp.ensureIndex(new Index().on("serviceId", Direction.DESC));
		gatherOp.ensureIndex(new Index().on("gatherTime", Direction.DESC));
		gatherOp.ensureIndex(new Index().on("requestInfo.maxProcessingTime", Direction.DESC));
		
		// uri索引
		IndexOperations uriOp = mongoTemplate.indexOps("uri_stat");
		uriOp.ensureIndex(new Index().on("avgTime", Direction.DESC));
		uriOp.ensureIndex(new Index().on("times", Direction.DESC));
		uriOp.ensureIndex(new Index().on("lasted", Direction.DESC));
		uriOp.ensureIndex(new Index().on("maxTime", Direction.DESC));
		
		// sql索引
		IndexOperations sqlOp = mongoTemplate.indexOps("sql_stat");
		sqlOp.ensureIndex(new Index().on("avgTime", Direction.DESC));
		sqlOp.ensureIndex(new Index().on("maxTime", Direction.DESC));
		sqlOp.ensureIndex(new Index().on("lasted", Direction.DESC));
		sqlOp.ensureIndex(new Index().on("maxSqlCount", Direction.DESC));
		sqlOp.ensureIndex(new Index().on("uri", Direction.DESC));
		
		// response索引
		IndexOperations responseOp = mongoTemplate.indexOps("response");
		responseOp.ensureIndex(new Index().on("serviceId", Direction.DESC));
		responseOp.ensureIndex(new Index().on("HH", Direction.DESC));
		responseOp.ensureIndex(new Index().on("avgTime", Direction.DESC));
				
		// warning索引
		IndexOperations warningOp = mongoTemplate.indexOps("warning");
		Index warningCombine = new Index();
		warningCombine.on("serviceId", Direction.DESC);
		warningCombine.on("uri", Direction.DESC);
		warningOp.ensureIndex(warningCombine);
		warningOp.ensureIndex(new Index().on("lasted", Direction.DESC));
		
		// debug索引
		IndexOperations debugOp = mongoTemplate.indexOps("debug");
		debugOp.ensureIndex(new Index().on("serviceId", Direction.DESC));
		debugOp.ensureIndex(new Index().on("beginTime", Direction.DESC));
		
	}
}
