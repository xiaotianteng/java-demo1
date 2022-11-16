package com.itheima.reggie.filter;
/*
* 检查用户是否完成登录   过滤器
* */


import com.alibaba.fastjson.JSON;
import com.itheima.reggie.common.BaseContext;
import com.itheima.reggie.common.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.AntPathMatcher;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebFilter(filterName = "loginCheckFilter",urlPatterns = "/*")
@Slf4j
public class LoginCheckFilter implements Filter {
    // 路径匹配器，支持通配符
    public static final AntPathMatcher PATH_MATCHER = new AntPathMatcher();

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest)servletRequest;
        HttpServletResponse response =(HttpServletResponse)servletResponse;
        // 获取本次请求的uri
        String requestURI = request.getRequestURI();

        log.info("拦截到请求：{}",requestURI);

        // 定义不需要处理的请求路径
        String[] urls = new String[]{
          "/employee/login",
          "/employee/logout",
          "/backend/**",
          "/front/**",
          "/common/**",
          "/user/sendMsg",
          "/user/login"
        };

        // 判断本次请求是否需要处理
        boolean check = check(urls, requestURI);
        if(check){
            log.info("本次请求不需要处理：{}",requestURI);
            filterChain.doFilter(request,response); // 放行
            return;
        }

        // 判断登录状态，如果已登录直接放行
        if(request.getSession().getAttribute("employee") != null){
            log.info("用户已登录，id为：{}",request.getSession().getAttribute("employee"));

            Long empId = (Long)request.getSession().getAttribute("employee");
            // 存入线程变量empId
            BaseContext.setCurrentId(empId);

            filterChain.doFilter(request,response); // 放行
            return;
        }



        // 判断登录状态，如果已登录直接放行
        if(request.getSession().getAttribute("user") != null){
            log.info("用户已登录，id为：{}",request.getSession().getAttribute("user"));

            Long userId = (Long)request.getSession().getAttribute("user");
            // 存入线程变量empId
            BaseContext.setCurrentId(userId);

            filterChain.doFilter(request,response); // 放行
            return;
        }






        log.info("用户未登录");
        // 如果未登录，返回未登录的结果，通过输出流方式向客户端响应数据
        response.getWriter().write(JSON.toJSONString(R.error("NOTLOGIN")));
        return;
    }


    public boolean check(String[] urls,String requestURI){
        for (String url : urls) {
            boolean match = PATH_MATCHER.match(url, requestURI);
             if(match){
                 return true;
             }
        }

        return false;
    }


}
