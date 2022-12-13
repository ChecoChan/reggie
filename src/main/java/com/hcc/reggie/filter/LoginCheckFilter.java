package com.hcc.reggie.filter;


import com.alibaba.fastjson.JSON;
import com.hcc.reggie.common.BaseContext;
import com.hcc.reggie.common.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.AntPathMatcher;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

/**
 * 检查用户是否已经完成登录
 */
@Slf4j
@WebFilter(filterName = "loginCheckFilter", urlPatterns = "/*")
public class LoginCheckFilter implements Filter {
    // 路径匹配器，支持通配符
    public static final AntPathMatcher PATH_MATCHER = new AntPathMatcher();

    /**
     * 1.获取本次请求 URI
     * 2.判断本次请求是否需要处理
     * 3.如果不需要处理，则直接放行
     * 4.判断登录状态，如果已经登录，则直接放行
     * 5.如果未登录则返回未登录
     */
    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;
        HttpSession session = request.getSession();

        // 1. 获取本次请求URI
        String requestURI = request.getRequestURI();
        log.info("拦截请求：{}", requestURI);

        // 2. 判断本次请求是否需要处理
        // 直接放行的请求页面
        String[] urls = new String[] {
                // 后台资源页面
                "/backend/**",
                // 员工登入
                "/employee/login",
                // 员工等出
                "/employee/logout",
                // 移动端资源页面
                "/front/**",
                // 移动端发送短信
                "/user/sendMsg",
                // 移动端登录
                "/user/login"
        };
        boolean check = check(urls, requestURI);

        // 3. 如果不需要处理，则直接放行
        if (check) {
            log.info("本次请求：{} 不需要处理", requestURI);
            filterChain.doFilter(request, response);
            return;
        }

        // 4-1. 判断后台登录状态，如果已经登录，则直接放行
        if (session.getAttribute("employee") != null) {
            // 保存当前用户 id
            Long empId = (Long) session.getAttribute("employee");
            BaseContext.setCurrentId(empId);

            log.info("员工已登录，员工 id 为 {}", empId);
            filterChain.doFilter(request, response);
            return;
        }
        // 4-2. 判断用户登录状态，如果已经登录，则直接放行
        if (session.getAttribute("user") != null) {
            // 保存当前用户 id
            Long userId = (Long) session.getAttribute("user");
            BaseContext.setCurrentId(userId);

            log.info("用户已登录，用户 id 为 {}", userId);
            filterChain.doFilter(request, response);
            return;
        }

        // 5. 如果未登录则返回未登录
        log.info("用户未登录");
        // 通过输出流的方式向客户端页面响应数据
        response.getWriter().write(JSON.toJSONString(R.error("NOTLOGIN")));
    }

    /**
     * 路径匹配方法
     */
    public boolean check(String[] urls, String requestURI) {
        for (String url : urls) {
            boolean match = PATH_MATCHER.match(url, requestURI);
            if (match)
                return true;
        }
        return false;
    }
}
