package com.ppx.cloud.monitor.console;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import com.ppx.cloud.common.config.PropertiesConfig;
import com.ppx.cloud.common.controller.ControllerReturn;
import com.ppx.cloud.monitor.AccessUtils;
import com.ppx.cloud.monitor.persistence.AccessEntity;


@Controller
public class MonitorViewController {
	
	@Autowired
	private MonitorViewService monitorViewService;
	
	private String title = "微服务实时多机监控平台V0.1";
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	@GetMapping
    public ModelAndView index() {
		ModelAndView mv = new ModelAndView();		
		Map<String, Object> configMap = new HashMap<String, Object>();
		List<Map> configList = monitorViewService.listConfig();
		for (Map map : configList) {
			configMap.put(map.get("_id") + "debug", map.get("isAccessDebug"));
			configMap.put(map.get("_id") + "warning", map.get("isAccessWarning"));
		}
		
		List<Map> serviceList = monitorViewService.listService();
		for (Map map : serviceList) {
			map.put("isAccessDebug", configMap.get(map.get("_id") + "debug"));
			map.put("isAccessWarning", configMap.get(map.get("_id") + "warning"));
		}
		mv.addObject("listMachine", serviceList);	
		mv.addObject("currentServiceId", AccessUtils.getServiceId());
		mv.addObject("title", title);
		return mv;
	}
	
	@GetMapping
    public ModelAndView service() {
		ModelAndView mv = new ModelAndView();
		mv.addObject("listJson", listAllService());
		mv.addObject("title", title);
		return mv;
	}
	
	@SuppressWarnings("rawtypes")
	@PostMapping @ResponseBody
	public Map<String, Object> listAllService() {	
		List<Map> list = monitorViewService.listAllService();
		return ControllerReturn.ok(list);
	}
	
	@GetMapping
    public ModelAndView start() {
		ModelAndView mv = new ModelAndView();	
		mv.addObject("listJson", listStart(new SpringDataPageable(), null));
		mv.addObject("listMachine", monitorViewService.listAllService());
		mv.addObject("title", title);
		return mv;
	}
	
	@SuppressWarnings("rawtypes")
	@PostMapping @ResponseBody
	public Map<String, Object> listStart(SpringDataPageable page, String serviceId) {	
		List<Map> list = monitorViewService.listStart(page, serviceId);
		return ControllerReturn.ok(list, page);
	}
	
	@GetMapping
    public ModelAndView access() {
		ModelAndView mv = new ModelAndView();
		String d = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
		mv.addObject("date", d);
		mv.addObject("listJson", listAccess(new SpringDataPageable(), null, d, null, null, null));
		mv.addObject("listMachine", monitorViewService.listAllService());
		mv.addObject("title", title);
		return mv;
	}
	
	@PostMapping @ResponseBody
	public Map<String, Object> listAccess(SpringDataPageable page, String serviceId, String date, String beginTime, 
			String endTime, String uri) {		
		List<AccessEntity> list = monitorViewService.listAccess(page, serviceId, date, beginTime, endTime, uri);
		return ControllerReturn.ok(list, page);
	}
	
	// >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>.
	
	@GetMapping
    public ModelAndView error() {
		ModelAndView mv = new ModelAndView();
		mv.addObject("listJson", listError(new SpringDataPageable(), null, null, null, null));	
		mv.addObject("listMachine", monitorViewService.listService());
		mv.addObject("title", title);
		return mv;
	}
	
	@SuppressWarnings("rawtypes")
	@PostMapping @ResponseBody
	public Map<String, Object> listError(SpringDataPageable page, String serviceId, String date, 
			String beginTime, String endTime) {	
		List<Map> list = monitorViewService.listError(page, serviceId, date, beginTime, endTime);
		return ControllerReturn.ok(list, page);
	}
	
	@GetMapping
    public ModelAndView gather() {
		ModelAndView mv = new ModelAndView();
		mv.addObject("listJson", listGather(new SpringDataPageable(), null, null, null, null));	
		mv.addObject("listMachine", monitorViewService.listService());
		mv.addObject("title", title);
		return mv;
	}
	
	@SuppressWarnings("rawtypes")
	@PostMapping @ResponseBody
	public Map<String, Object> listGather(SpringDataPageable page, String serviceId, String date, String beginTime, String endTime) {	
		List<Map> list = monitorViewService.listGather(page, serviceId, date, beginTime, endTime);
		return ControllerReturn.ok(list, page);
	}
	
	
	// >>>>>>>>>>>>>>>>>>>>>>>>>>统计>>>>>>>>>>>>>>>>>>>>>>>>
	
	@GetMapping
    public ModelAndView uriStat() {
		ModelAndView mv = new ModelAndView();
		mv.addObject("listJson", listUriStat(new SpringDataPageable(), null, null, null));		
		mv.addObject("title", title);
		return mv;
	}
	
	@SuppressWarnings("rawtypes")
	@PostMapping @ResponseBody
	public Map<String, Object> listUriStat(SpringDataPageable page, String uri, Long beginAvgTime, Long endAvgTime) {	
		List<Map> list = monitorViewService.listUriStat(page, uri, beginAvgTime, endAvgTime);
		return ControllerReturn.ok(list, page);
	}	
	
	@GetMapping
    public ModelAndView sqlStat() {
		ModelAndView mv = new ModelAndView();		
		Map<String, Object> map = listSqlStat(new SpringDataPageable(), null, null, null);	
		mv.addObject("listJson", map);
		mv.addObject("title", title);
		return mv;
	}
	
	@SuppressWarnings("rawtypes")
	@PostMapping @ResponseBody
	public Map<String, Object> listSqlStat(SpringDataPageable page, String sql, Long beginAvgTime, Long endAvgTime) {	
		List<Map> list = monitorViewService.listSqlStat(page, sql, beginAvgTime, endAvgTime);
		return ControllerReturn.ok(list, page);
	}
	
	@GetMapping
    public ModelAndView response() {
		ModelAndView mv = new ModelAndView();
		mv.addObject("listJson", listResponse(new SpringDataPageable(), null, null, null, null, null));		
		mv.addObject("listMachine", monitorViewService.listService());
		mv.addObject("title", title);
		return mv;
	}
	
	@SuppressWarnings("rawtypes")
	@PostMapping @ResponseBody
	public Map<String, Object> listResponse(SpringDataPageable page, String serviceId, String beginHH, String endHH,
			Long beginAvgTime, Long endAvgTime) {	
		List<Map> list = monitorViewService.listResponse(page, serviceId, beginHH, endHH,
				beginAvgTime, endAvgTime);
		return ControllerReturn.ok(list, page);
	}

	
	// >>>>>>>>>>>>>>>>warning debug>>>>>>>>>>>>>>>>>>>
	
	@GetMapping
    public ModelAndView warning() {
		ModelAndView mv = new ModelAndView();	
		mv.addObject("listJson", listWarning(new SpringDataPageable(), null));
		mv.addObject("listMachine", monitorViewService.listService());
		mv.addObject("title", title);
		return mv;
	}
	
	@SuppressWarnings("rawtypes")
	@PostMapping @ResponseBody
	public Map<String, Object> listWarning(SpringDataPageable page, String serviceId) {	
		List<Map> list = monitorViewService.listWarning(page, serviceId);
		return ControllerReturn.ok(list, page);
	}
	
	@GetMapping
    public ModelAndView debug() {
		ModelAndView mv = new ModelAndView();	
		mv.addObject("listJson", listDebug(new SpringDataPageable(), null, null, null, null, null));
		mv.addObject("listMachine", monitorViewService.listService());
		mv.addObject("title", title);
		return mv;
	}
	
	@PostMapping @ResponseBody
	public Map<String, Object> listDebug(SpringDataPageable page, String serviceId, String date, String beginTime,
			String endTime, String uri) {	
		List<AccessEntity> list = monitorViewService.listDebug(page, serviceId, date, beginTime, endTime, uri);
		return ControllerReturn.ok(list, page);
	}
	
	@GetMapping
    public ModelAndView set() {
		ModelAndView mv = new ModelAndView();
		mv.addObject("isAccessDebug", PropertiesConfig.isAccessDebugEnabled());
		mv.addObject("listJson", monitorViewService.listConfig());	
		mv.addObject("title", title);
		mv.addObject("dbStats", monitorViewService.dbStats());		
		return mv;
	}

	// 通过uri查看sql
	@GetMapping
	public ModelAndView viewSqlByUri(@RequestParam String uri) {
		ModelAndView mv = new ModelAndView();
		mv.addObject("listJson", querySqlByUri(uri, new SpringDataPageable()));
		mv.addObject("uri", uri);
		return mv;
	}

	@SuppressWarnings("rawtypes")
	@PostMapping @ResponseBody
	public Map<String, Object> querySqlByUri(@RequestParam String uri,SpringDataPageable page) {
		List<Map> list = monitorViewService.querySqlByUri(uri, page);
		return ControllerReturn.ok(list, page);
	}
	
	// 查看异常详情
	@GetMapping
	public ModelAndView viewErrorDetail(@RequestParam String _id) {
		ModelAndView mv = new ModelAndView();
		mv.addObject("detail", monitorViewService.viewErrorDetail(_id));
		return mv;
	}
	
	// 查看debug详情
	@GetMapping
	public ModelAndView viewDebugDetail(@RequestParam String _id) {
		ModelAndView mv = new ModelAndView();
		mv.addObject("detail", monitorViewService.viewDebugDetail(_id));
		return mv;
	}
	
}