package com.ppx.cloud.monitor.console;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import com.ppx.cloud.common.config.PropertiesConfig;
import com.ppx.cloud.common.controller.ControllerReturn;
import com.ppx.cloud.monitor.AccessUtils;

@RestController
public class MonitorConfController {

	@Autowired
	private MonitorViewService monitorViewService;
	
	@Autowired
	private MonitorConfService monitorConfService;
	
	private String DELETE_PASSWORD = "ppx";

	private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

	@PostMapping @ResponseBody
	public Map<String, Object> setAccessDebug(@RequestParam String serviceId, @RequestParam boolean debug) {
		Date now = monitorConfService.setAccessDebug(serviceId, debug);
		// 本机立即生效
		if (AccessUtils.getServiceId().equals(serviceId)) {
			PropertiesConfig.setAccessDebug(debug);
		}
		return ControllerReturn.ok(sdf.format(now));
	}

	@PostMapping @ResponseBody
	public Map<String, Object> setAccessWarning(@RequestParam String serviceId, @RequestParam boolean warning) {
		Date now = monitorConfService.setAccessWarning(serviceId, warning);
		// 本机立即生效
		if (AccessUtils.getServiceId().equals(serviceId)) {
			PropertiesConfig.setAccessWarning(warning);
		}
		return ControllerReturn.ok(sdf.format(now));
	}

	@PostMapping @ResponseBody
	public Map<String, Object> setGatherInterval(@RequestParam String serviceId, @RequestParam long gatherInterval) {
		Date now = monitorConfService.setGatherInterval(serviceId, gatherInterval);
		// 本机立即生效
		if (AccessUtils.getServiceId().equals(serviceId)) {
			PropertiesConfig.setGatherInterval(gatherInterval);;
		}
		return ControllerReturn.ok(sdf.format(now));
	}

	@PostMapping @ResponseBody
	public Map<String, Object> setDumpMaxTime(@RequestParam String serviceId, @RequestParam long dumpMaxTime) {
		Date now = monitorConfService.setDumpMaxTime(serviceId, dumpMaxTime);
		// 本机立即生效
		if (AccessUtils.getServiceId().equals(serviceId)) {
			PropertiesConfig.setDumpMaxTime(dumpMaxTime);
		}
		return ControllerReturn.ok(sdf.format(now));
	}

	// clean
	@PostMapping @ResponseBody
	public Map<String, Object> cleanError(String deletePassword) {
		if (!DELETE_PASSWORD.equals(deletePassword)) {
			return ControllerReturn.fail(-1, "deletePassword error");
		}
		else {
			monitorConfService.cleanError();
			return ControllerReturn.ok();
		}		
	}

	// clean
	@PostMapping @ResponseBody
	public Map<String, Object> cleanGather(String deletePassword) {
		if (!DELETE_PASSWORD.equals(deletePassword)) {
			return ControllerReturn.fail(-1, "deletePassword error");
		}
		else {
			monitorConfService.cleanGather();
			return ControllerReturn.ok();
		}
	}

	// clean
	@PostMapping @ResponseBody
	public Map<String, Object> cleanDebug() {
		monitorConfService.cleanDebug();
		return ControllerReturn.ok();
	}

	// clean
	@PostMapping @ResponseBody
	public Map<String, Object> cleanWarning() {
		monitorConfService.cleanWarning();
		return ControllerReturn.ok();
	}

	// clean
	@PostMapping @ResponseBody
	public Map<String, Object> cleanStat() {
		monitorConfService.cleanStat();
		return ControllerReturn.ok();
	}
	
	@RequestMapping
	@ResponseBody
	public Map<String, Object> orderService(@RequestParam String serviceIds) {
		monitorConfService.orderService(serviceIds);		
		return ControllerReturn.ok(monitorViewService.listAllService());
	}	
	
	@RequestMapping @ResponseBody
	public Map<String, Object> display(@RequestParam String serviceId, @RequestParam int display) {
		monitorConfService.display(serviceId, display);	
		return ControllerReturn.ok(monitorViewService.listAllService());
	}
	
	
	
	@Autowired
	private WebApplicationContext app;
	
	@RequestMapping @ResponseBody
	public Map<String, Object> getResourceUri() {
		RequestMappingHandlerMapping r = (RequestMappingHandlerMapping)app.getBean("requestMappingHandlerMapping");
		Map<RequestMappingInfo, HandlerMethod> map = r.getHandlerMethods();
	    
		// 排除监控部分/monitorConf/ /monitorView/ /error
		List<String> returnList = new ArrayList<String>();
		returnList.add("/*");
		
		Set<String> controllerSet = new HashSet<String>();
		
	    Set<RequestMappingInfo> set =  map.keySet();
	    for (RequestMappingInfo info : set) {
	    	Set<String> uriSet = info.getPatternsCondition().getPatterns();
	    	for (Object uri : uriSet) {    	
	    		// 监控部分
	    		if (uri.toString().startsWith("/monitorConf/")) continue;
	    		if (uri.toString().startsWith("/monitorView/")) continue;
	    			
	    		// 权限部分
	    		if (uri.toString().startsWith("/grant/")) continue;
	    		if (uri.toString().startsWith("/index/")) continue;
	    		if (uri.toString().startsWith("/login/")) continue;
	    		if (uri.toString().startsWith("/resource/")) continue;
	    		
	    		if (uri.toString().startsWith("/error")) continue;
	    		
	    		String[] u = uri.toString().split("/");
	    		if (u.length == 3) {
	    			controllerSet.add("/" + u[1] + "/*");
	    		}
	    		returnList.add(uri.toString());
			}
		}
	 
	    returnList.addAll(controllerSet);
		return ControllerReturn.ok(returnList);
	}
	
}