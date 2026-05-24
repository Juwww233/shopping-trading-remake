package com.example.shopping3.interceptor;

import com.example.shopping3.annotation.NoAuth;
import com.example.shopping3.util.SessionUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class AuthInterceptor implements HandlerInterceptor {

    @Autowired
    private SessionUtil sessionUtil;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response,
                             Object handler) throws Exception {
        if (!(handler instanceof HandlerMethod hm)) {
            return true;
        }

        if (hm.getMethodAnnotation(NoAuth.class) != null ||
                hm.getBeanType().getAnnotation(NoAuth.class) != null) {
            return true;
        }

        String sessionId = request.getHeader("X-Session-Id");
        if (sessionId == null || sessionUtil.getSession(sessionId) == null) {
            response.setContentType("application/json;charset=UTF-8");
            response.setStatus(401);
            response.getWriter().write("{\"code\":500,\"msg\":\"未登录或会话过期\",\"data\":null}");
            return false;
        }

        request.setAttribute("currentUser", sessionUtil.getSession(sessionId));
        sessionUtil.refreshSession(sessionId);
        return true;
    }
}