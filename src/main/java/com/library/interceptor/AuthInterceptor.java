package com.library.interceptor;

import com.library.entity.User;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@Component
public class AuthInterceptor implements HandlerInterceptor {
    
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, 
                           Object handler) throws Exception {
        String uri = request.getRequestURI();
        HttpSession session = request.getSession(false);
        
        // 管理员功能检查
        if (uri.contains("/admin")) {
            if (session == null) {
                response.sendRedirect("/library/login");
                return false;
            }
            User user = (User) session.getAttribute("user");
            if (user == null || user.getRole() != User.Role.ADMIN) {
                response.sendRedirect("/library/dashboard");
                return false;
            }
        }
        
        // 借阅功能检查（需要登录）
        if (uri.startsWith("/library/borrow") && !uri.contains("/admin")) {
            if (session == null) {
                response.sendRedirect("/library/login");
                return false;
            }
            User user = (User) session.getAttribute("user");
            if (user == null || user.getRole() == User.Role.GUEST) {
                response.sendRedirect("/library/login");
                return false;
            }
        }
        
        return true;
    }
}

