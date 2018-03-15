package com.ppx.cloud.monitor.console;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.servlet.ModelAndView;

import com.ppx.cloud.common.controller.ControllerReturn;

@Controller
public class MonitorCacheController {
	
	
	@Autowired
	private CacheManager cacheManager;
	
	@Autowired
	private WebApplicationContext app;
	
	@GetMapping
    public ModelAndView index() {
		ModelAndView mv = new ModelAndView();
		
		
		
		return mv;
	}
	
	@PostMapping @ResponseBody
	public Map<String, Object> listCacheName() {
		
		Collection<String> c = cacheManager.getCacheNames();
		
		
		List<String> list = new ArrayList<String>();
		list.addAll(c);
		
		return ControllerReturn.ok(list);
	}
	
	
	@PostMapping @ResponseBody
	public Map<String, Object> listKey(String cacheName) {
		StringRedisTemplate stringRedisTemplate = app.getBean(StringRedisTemplate.class);
		
		Set<String> set = stringRedisTemplate.keys(cacheName + "*");
		
		
		return ControllerReturn.ok(set);
	}
	
	@PostMapping @ResponseBody
	public Map<String, Object> getValue(String key) {
		StringRedisTemplate stringRedisTemplate = app.getBean(StringRedisTemplate.class);
		
		String value = stringRedisTemplate.opsForValue().get(key);
		
		
		return ControllerReturn.ok(value);
	}
	
	
}
