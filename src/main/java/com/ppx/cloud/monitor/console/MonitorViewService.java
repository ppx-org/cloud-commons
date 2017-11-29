package com.ppx.cloud.monitor.console;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;
import java.util.Map;

import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.domain.Sort.Order;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestParam;

import com.ppx.cloud.common.exception.custom.DateException;
import com.ppx.cloud.monitor.persistence.AccessEntity;

@Service
public class MonitorViewService {
	@Autowired
	private MongoTemplate mongoTemplate;

	private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

	@SuppressWarnings("rawtypes")
	public List<Map> listConfig() {
		Query query = new Query().with(Sort.by(Direction.DESC, "firsted"));
		return mongoTemplate.find(query, Map.class, "config");
	}

	@SuppressWarnings("rawtypes")
	public List<Map> listService() {
		Query query = new Query();
		Criteria criteria = Criteria.where("display").is(1).and("type").is("service");
		query.addCriteria(criteria);
		List<Order> orders = new ArrayList<Order>();
		orders.add(new Order(Direction.ASC, "order"));
		orders.add(new Order(Direction.DESC, "firsted"));
		query.with(Sort.by(orders));

		List<Map> list = mongoTemplate.find(query, Map.class, "service");
		return list;
	}

	@SuppressWarnings("rawtypes")
	public List<Map> listAllService() {
		Query query = new Query();
		List<Order> orders = new ArrayList<Order>();
		orders.add(new Order(Direction.ASC, "order"));
		orders.add(new Order(Direction.DESC, "firsted"));
		query.with(Sort.by(orders));

		return mongoTemplate.find(query, Map.class, "service");
	}

	@SuppressWarnings("rawtypes")
	public List<Map> listStart(SpringDataPageable pageable, String serviceId) {
		Query query = new Query();
		if (!StringUtils.isEmpty(serviceId)) {
			Criteria criteria = Criteria.where("serviceId").is(serviceId);
			query.addCriteria(criteria);
		}

		String collectionName = "start";
		long totalRows = mongoTemplate.count(query, collectionName);
		pageable.setTotalRows(totalRows);
		pageable.setSort(Sort.by(Direction.DESC, "_id"));
		query.with(pageable);

		return mongoTemplate.find(query, Map.class, collectionName);
	}

	public List<AccessEntity> listAccess(SpringDataPageable pageable, String serviceId, String date, String beginTime,
			String endTime, String uri) {
		Query query = new Query();
		if (!StringUtils.isEmpty(serviceId)) {
			query.addCriteria(Criteria.where("serviceId").is(serviceId));
		}

		try {
			if (!StringUtils.isEmpty(beginTime) || !StringUtils.isEmpty(endTime)) {
				Criteria criteria = Criteria.where("beginTime");
				if (!StringUtils.isEmpty(beginTime)) {
					criteria.gte(sdf.parse(date + " " + beginTime));
				}
				if (!StringUtils.isEmpty(endTime)) {
					criteria.lte(sdf.parse(date + " " + endTime));
				}
				query.addCriteria(criteria);
			}
		} catch (ParseException e) {
			throw new DateException(e);
		}

		if (!StringUtils.isEmpty(uri)) {
			Criteria criteria = Criteria.where("uri").is(uri);
			query.addCriteria(criteria);
		}

		String collectionName = "access_" + date;
		long totalRows = mongoTemplate.count(query, collectionName);
		pageable.setTotalRows(totalRows);

		List<Order> orders = new ArrayList<Order>();
		if (StringUtils.isEmpty(pageable.getOrderName())) {
			orders.add(new Order(Direction.DESC, "beginTime"));
		} else {
			orders.add(new Order(pageable.getOrderDirection(), pageable.getOrderName()));
		}

		pageable.setSort(Sort.by(orders));
		query.with(pageable);

		return mongoTemplate.find(query, AccessEntity.class, collectionName);
	}

	// >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public List<Map> listError(SpringDataPageable pageable, String serviceId, String date, String beginTime,
			String endTime) {
		Query query = new Query();
		if (!StringUtils.isEmpty(serviceId)) {
			query.addCriteria(Criteria.where("serviceId").is(serviceId));
		}
		try {
			if (!StringUtils.isEmpty(date)) {
				Criteria criteria = Criteria.where("beginTime");
				criteria.gte(sdf.parse(date + " " + (beginTime.isEmpty() ? "00:00:00" : beginTime)));
				criteria.lte(sdf.parse(date + " " + (endTime.isEmpty() ? "23:59:59" : endTime)));
				query.addCriteria(criteria);
			}
		} catch (ParseException e) {
			throw new DateException(e);
		}

		String collectionName = "error";
		long totalRows = mongoTemplate.count(query, collectionName);
		pageable.setTotalRows(totalRows);

		List<Order> orders = new ArrayList<Order>();
		if (StringUtils.isEmpty(pageable.getOrderName())) {
			orders.add(new Order(Direction.DESC, "beginTime"));
		} else {
			orders.add(new Order(pageable.getOrderDirection(), pageable.getOrderName()));
		}

		pageable.setSort(Sort.by(orders));
		query.with(pageable);

		List<Map> list = mongoTemplate.find(query, Map.class, collectionName);
		for (Map map : list) {
			// 为了在查看时传入_id
			map.put("_id", ((ObjectId) map.get("_id")).toHexString());
		}

		return list;
	}

	@SuppressWarnings("rawtypes")
	public List<Map> listGather(SpringDataPageable pageable, String serviceId, String date, String beginTime,
			String endTime) {
		Query query = new Query();
		if (!StringUtils.isEmpty(serviceId)) {
			query.addCriteria(Criteria.where("serviceId").is(serviceId));
		}
		try {
			if (!StringUtils.isEmpty(date)) {
				Criteria criteria = Criteria.where("gatherTime");
				criteria.gte(sdf.parse(date + " " + (beginTime.isEmpty() ? "00:00:00" : beginTime)));
				criteria.lte(sdf.parse(date + " " + (endTime.isEmpty() ? "23:59:59" : endTime)));
				query.addCriteria(criteria);
			}
		} catch (ParseException e) {
			throw new DateException(e);
		}
		String collectionName = "gather";
		long totalRows = mongoTemplate.count(query, collectionName);
		pageable.setTotalRows(totalRows);

		List<Order> orders = new ArrayList<Order>();
		if (StringUtils.isEmpty(pageable.getOrderName())) {
			orders.add(new Order(Direction.DESC, "_id"));
		} else {
			orders.add(new Order(pageable.getOrderDirection(), pageable.getOrderName()));
		}

		pageable.setSort(Sort.by(orders));
		query.with(pageable);

		return mongoTemplate.find(query, Map.class, collectionName);
	}

	@SuppressWarnings("rawtypes")
	public Map viewErrorDetail(@RequestParam String _id) {
		Query query = new Query();
		Criteria criteria = Criteria.where("_id").is(new ObjectId(_id));
		query.addCriteria(criteria);
		return mongoTemplate.findOne(query, Map.class, "error_detail");
	}

	// >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>

	@SuppressWarnings("rawtypes")
	public List<Map> listUriStat(SpringDataPageable pageable, String uri, Long beginAvgTime, Long endAvgTime) {
		Query query = new Query();
		if (!StringUtils.isEmpty(uri)) {
			query.addCriteria(Criteria.where("_id").is(uri));
		}

		if (!StringUtils.isEmpty(beginAvgTime) || !StringUtils.isEmpty(endAvgTime)) {
			Criteria criteria = Criteria.where("avgTime");
			if (!StringUtils.isEmpty(beginAvgTime)) {
				criteria.gte(beginAvgTime);
			}
			if (!StringUtils.isEmpty(endAvgTime)) {
				criteria.lte(endAvgTime);
			}
			query.addCriteria(criteria);
		}

		String collectionName = "uri_stat";
		long totalRows = mongoTemplate.count(query, collectionName);
		pageable.setTotalRows(totalRows);

		List<Order> orders = new ArrayList<Order>();
		if (StringUtils.isEmpty(pageable.getOrderName())) {
			orders.add(new Order(Direction.DESC, "lasted"));
		} else {
			orders.add(new Order(pageable.getOrderDirection(), pageable.getOrderName()));
		}
		pageable.setSort(Sort.by(orders));
		query.with(pageable);

		return mongoTemplate.find(query, Map.class, collectionName);
	}

	@SuppressWarnings("rawtypes")
	public List<Map> listSqlStat(SpringDataPageable pageable, String sql, Long beginAvgTime, Long endAvgTime) {
		Query query = new Query();
		if (!StringUtils.isEmpty(sql)) {
			query.addCriteria(Criteria.where("_id").is(sql));
		}

		if (!StringUtils.isEmpty(beginAvgTime) || !StringUtils.isEmpty(endAvgTime)) {
			Criteria criteria = Criteria.where("avgTime");
			if (!StringUtils.isEmpty(beginAvgTime)) {
				criteria.gte(beginAvgTime);
			}
			if (!StringUtils.isEmpty(endAvgTime)) {
				criteria.lte(endAvgTime);
			}
			query.addCriteria(criteria);
		}

		String collectionName = "sql_stat";
		long totalRows = mongoTemplate.count(query, collectionName);
		pageable.setTotalRows(totalRows);

		List<Order> orders = new ArrayList<Order>();
		if (StringUtils.isEmpty(pageable.getOrderName())) {
			orders.add(new Order(Direction.DESC, "lasted"));
		} else {
			orders.add(new Order(pageable.getOrderDirection(), pageable.getOrderName()));
		}

		pageable.setSort(Sort.by(orders));
		query.with(pageable);

		return mongoTemplate.find(query, Map.class, collectionName);
	}

	@SuppressWarnings("rawtypes")
	public List<Map> listResponse(SpringDataPageable pageable, String serviceId, String beginHH, String endHH,
			Long beginAvgTime, Long endAvgTime) {
		Query query = new Query();
		if (!StringUtils.isEmpty(serviceId)) {
			query.addCriteria(Criteria.where("serviceId").is(serviceId));
		}

		if (!StringUtils.isEmpty(beginHH) || !StringUtils.isEmpty(endHH)) {
			Criteria criteria = Criteria.where("HH");
			if (!StringUtils.isEmpty(beginHH)) {
				criteria.gte(beginHH);
			}
			if (!StringUtils.isEmpty(endHH)) {
				criteria.lte(endHH);
			}
			query.addCriteria(criteria);
		}

		if (!StringUtils.isEmpty(beginAvgTime) || !StringUtils.isEmpty(endAvgTime)) {
			Criteria criteria = Criteria.where("avgTime");
			if (!StringUtils.isEmpty(beginAvgTime)) {
				criteria.gte(beginAvgTime);
			}
			if (!StringUtils.isEmpty(endAvgTime)) {
				criteria.lte(endAvgTime);
			}
			query.addCriteria(criteria);
		}

		String collectionName = "response";
		long totalRows = mongoTemplate.count(query, collectionName);
		pageable.setTotalRows(totalRows);

		List<Order> orders = new ArrayList<Order>();
		if (StringUtils.isEmpty(pageable.getOrderName())) {
			orders.add(new Order(Direction.DESC, "_id"));
		} else {
			orders.add(new Order(pageable.getOrderDirection(), pageable.getOrderName()));
		}
		pageable.setSort(Sort.by(orders));
		query.with(pageable);
		return mongoTemplate.find(query, Map.class, collectionName);
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public List<Map> listWarning(SpringDataPageable pageable, String serviceId) {
		Query query = new Query();
		if (!StringUtils.isEmpty(serviceId)) {
			query.addCriteria(Criteria.where("serviceId").is(serviceId));
		}

		String collectionName = "warning";
		long totalRows = mongoTemplate.count(query, collectionName);
		pageable.setTotalRows(totalRows);

		List<Order> orders = new ArrayList<Order>();
		if (StringUtils.isEmpty(pageable.getOrderName())) {
			orders.add(new Order(Direction.DESC, "lasted"));
		} else {
			orders.add(new Order(pageable.getOrderDirection(), pageable.getOrderName()));
		}

		pageable.setSort(Sort.by(orders));
		query.with(pageable);
		List<Map> list = mongoTemplate.find(query, Map.class, collectionName);
		for (Map map : list) {
			Long content = (Long) map.get("content");
			if (content != null) {
				long[] l = { content };
				map.put("content", BitSet.valueOf(l).toString());
			}
		}
		return list;
	}

	public List<AccessEntity> listDebug(SpringDataPageable pageable, String serviceId, String date, String beginTime,
			String endTime, String uri) {
		Query query = new Query();
		if (!StringUtils.isEmpty(serviceId)) {
			query.addCriteria(Criteria.where("serviceId").is(serviceId));
		}
		try {
			if (!StringUtils.isEmpty(date)) {
				Criteria criteria = Criteria.where("beginTime");
				criteria.gte(sdf.parse(date + " " + (beginTime.isEmpty() ? "00:00:00" : beginTime)));
				criteria.lte(sdf.parse(date + " " + (endTime.isEmpty() ? "23:59:59" : endTime)));
				query.addCriteria(criteria);
			}
		} catch (ParseException e) {
			throw new DateException(e);
		}
		if (!StringUtils.isEmpty(uri)) {
			query.addCriteria(Criteria.where("uri").is(uri));
		}

		String collectionName = "debug";
		long totalRows = mongoTemplate.count(query, collectionName);
		pageable.setTotalRows(totalRows);

		List<Order> orders = new ArrayList<Order>();
		if (StringUtils.isEmpty(pageable.getOrderName())) {
			orders.add(new Order(Direction.DESC, "beginTime"));
		} else {
			orders.add(new Order(pageable.getOrderDirection(), pageable.getOrderName()));
		}

		pageable.setSort(Sort.by(orders));
		query.with(pageable);
		return mongoTemplate.find(query, AccessEntity.class, collectionName);
	}

	@SuppressWarnings("rawtypes")
	public List<Map> querySqlByUri(String uri, SpringDataPageable pageable) {
		Query query = new Query();
		query.addCriteria(Criteria.where("uri").in(uri));

		String collectionName = "sql_stat";
		long totalRows = mongoTemplate.count(query, collectionName);
		pageable.setTotalRows(totalRows);
		pageable.setSort(Sort.by(Direction.DESC, "_id"));
		query.with(pageable);
		return mongoTemplate.find(query, Map.class, collectionName);
	}

	@SuppressWarnings("rawtypes")
	public Map viewDebugDetail(@RequestParam String _id) {
		Query query = new Query();
		query.addCriteria(Criteria.where("_id").is(new ObjectId(_id)));
		return mongoTemplate.findOne(query, Map.class, "debug");
	}

	public String dbStats() {
		// CommandResult r = mongoTemplate.getDb().command("db.stats");
		return "{}";
	}
}
