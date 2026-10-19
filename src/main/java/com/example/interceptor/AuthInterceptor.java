package com.example.interceptor;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.springframework.web.servlet.HandlerInterceptor;

import com.example.model.User;

/**
 * Sends visitors who are not logged in to the login page.
 * With adminOnly set, also keeps teachers out of the administrator pages.
 */
public class AuthInterceptor implements HandlerInterceptor {

    public static final String SESSION_USER = "currentUser";

    private boolean adminOnly;

    public void setAdminOnly(boolean adminOnly) {
        this.adminOnly = adminOnly;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws IOException {
        HttpSession session = request.getSession(false);
        User user = session == null ? null : (User) session.getAttribute(SESSION_USER);
        if (user == null) {
            response.sendRedirect(response.encodeRedirectURL(request.getContextPath() + "/login"));
            return false;
        }
        if (adminOnly && !user.isAdmin()) {
            response.sendRedirect(response.encodeRedirectURL(request.getContextPath() + "/teacherDashboard"));
            return false;
        }
        return true;
    }
}
