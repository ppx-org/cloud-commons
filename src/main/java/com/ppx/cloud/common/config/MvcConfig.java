package com.ppx.cloud.common.config;

import java.util.List;

import javax.annotation.Resource;

import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurationSupport;

import com.ppx.cloud.common.controller.ControllerInterceptor;
import com.ppx.cloud.common.json.ObjectMappingCustomer;  

/**
 * Mvc配置
 * @author dengxz
 * @date 2017年11月2日
 */
@Configuration
public class MvcConfig extends WebMvcConfigurationSupport {
	
	// 为了让firstConfigBean先运行 (@ComponentScan自动扫描之后@Order不生效)
	@Resource(name = "firstConfigRun")
	private Object firstConfigBean;
	

	@Override
	public void configureMessageConverters(List<HttpMessageConverter<?>> converters) {
		super.configureMessageConverters(converters);	      
		MappingJackson2HttpMessageConverter m = new MappingJackson2HttpMessageConverter();
		m.setObjectMapper(new ObjectMappingCustomer());
        converters.add(m);
	}
	
	@Override
	public void addViewControllers(ViewControllerRegistry registry) {
		super.addViewControllers(registry);
	}	
	
	@Override
	public void addInterceptors(InterceptorRegistry registry) {
		super.addInterceptors(registry);
		registry.addInterceptor(new ControllerInterceptor()).excludePathPatterns("/static/");
		registry.addInterceptor(getGrantInterceptorObject()).excludePathPatterns("/static/");	
	}
	
	@Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/static/**").addResourceLocations("classpath:/static/");
        
        // 单机模式  图片
        registry.addResourceHandler("/img/**").addResourceLocations("file:" + System.getProperty("file.imgFilePath"));
        super.addResourceHandlers(registry);
    }
	
	private HandlerInterceptor getGrantInterceptorObject() {
		// 分布式
		// return new PortalGrantInterceptor();
		
		// 单机模式
		try {
			return (HandlerInterceptor)Class.forName("com.ppx.cloud.grant.common.GrantInterceptor").newInstance();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	
}  
