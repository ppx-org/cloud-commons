package com.ppx.cloud.monitor.console;

import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;

@Service
public class MonitorConfService {

	@Autowired
	private MongoTemplate mongoTemplate;

	public Date setAccessDebug(String serviceId, boolean debug) {
		Date now = new Date();
		Query query = new Query();
		query.addCriteria(Criteria.where("_id").in(serviceId));

		Update update = new Update();
		update.set("isAccessDebug", debug);
		update.set("lasted", now);
		mongoTemplate.updateFirst(query, update, "config");
		return now;
	}

	public Date setAccessWarning(String serviceId, boolean warning) {
		Date now = new Date();
		Query query = new Query();
		query.addCriteria(Criteria.where("_id").in(serviceId));

		Update update = new Update();
		update.set("isAccessWarning", warning);
		update.set("lasted", new Date());
		mongoTemplate.updateFirst(query, update, "config");
		return now;
	}

	public Date setGatherInterval(String serviceId, long gatherInterval) {
		Date now = new Date();
		Query query = new Query();
		query.addCriteria(Criteria.where("_id").in(serviceId));
		Update update = new Update();
		update.set("gatherInterval", gatherInterval);
		update.set("lasted", now);
		mongoTemplate.updateFirst(query, update, "config");
		return now;
	}

	public Date setDumpMaxTime(String serviceId, long dumpMaxTime) {
		Date now = new Date();
		Query query = new Query();
		query.addCriteria(Criteria.where("_id").in(serviceId));
		Update update = new Update();
		update.set("dumpMaxTime", dumpMaxTime);
		update.set("lasted", now);
		mongoTemplate.updateFirst(query, update, "config");
		return now;
	}

	// clean
	public void cleanError() {
		mongoTemplate.remove(new Query(), "error");
		mongoTemplate.remove(new Query(), "error_detail");
	}

	public void cleanGather() {
		mongoTemplate.remove(new Query(), "gather");
	}

	public void cleanDebug() {
		mongoTemplate.remove(new Query(), "debug");
	}

	public void cleanWarning() {
		mongoTemplate.remove(new Query(), "warning");
	}

	public void cleanStat() {
		mongoTemplate.remove(new Query(), "uri_stat");
		mongoTemplate.remove(new Query(), "sql_stat");
		mongoTemplate.remove(new Query(), "response");
	}

	public void orderService(String serviceIds) {
		String[] serviceId = serviceIds.split(",");
		for (int i = 0; i < serviceId.length; i++) {
			Update update = new Update();
			update.set("order", i);
			Query query = new Query();
			query.addCriteria(Criteria.where("_id").is(serviceId[i]));
			mongoTemplate.updateFirst(query, update, "service");
		}
	}

	public void display(String serviceId, int display) {
		Update update = new Update();
		update.set("display", display);
		
		Query query = new Query();
		query.addCriteria(Criteria.where("_id").is(serviceId));
		mongoTemplate.updateFirst(query, update, "service");
	}
}
