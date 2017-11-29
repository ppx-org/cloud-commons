package com.ppx.cloud.common.grant;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.util.StringUtils;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import com.auth0.jwt.JWT;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.ppx.cloud.common.util.CookieUtils;


/**
 * 
 * @author dengxz
 * @date 2017年11月4日
 */
public class PortalGrantInterceptor implements HandlerInterceptor {

	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
			throws Exception {
		
		
		// 从cookie中取得token
		String token = CookieUtils.getCookieMap(request).get(PortalGrantUtils.PPXTOKEN);
		
		// 直接解码，已经在网关已经验证
		DecodedJWT jwt = JWT.decode(token);
		Integer userId = jwt.getClaim("USER_ID").asInt();
		String userAccount = jwt.getClaim("USER_ACCOUNT").asString();
		String userName = jwt.getClaim("USER_NAME").asString();

		LoginUser u = new LoginUser();
		u.setUserId(userId);
		u.setUserAccount(userAccount);
		u.setUserName(userName);
		PortalGrantContext.setLoginUser(u);
		
		// 操作权限
		String permitAction = request.getHeader(PortalGrantUtils.PERMITACTION);
		if (!StringUtils.isEmpty(permitAction)) {
			String[] permitActionArray = permitAction.split(",");
			for (String p : permitActionArray) {
				request.setAttribute(p.replace("/", "_"), true);
			}
		}
		
		return true;
	}

	public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler,
			ModelAndView modelAndView) throws Exception {

	}

	public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex)
			throws Exception {
	}
}