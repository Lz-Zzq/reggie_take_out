package com.itheima.reggie.filter;

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

/**
 * 检查用户是否已经登陆 过滤器
 */
//拦截器的名称,拦截器的拦截范围
@WebFilter(filterName = "loginCheckFilter", urlPatterns = "/*")
@Slf4j
public class LoginCheckFilter implements Filter {
    //路径匹配器,支持通配符
    public static final AntPathMatcher PATH_MATCHER = new AntPathMatcher();

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {

        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;

        //1.获取本次请求的URI
        String requestURI = request.getRequestURI();

        log.info("拦截到请求路径{}", requestURI);

        //2. 定义不需要处理的请求路径
        String[] urls = new String[]{
                "/employee/login",
                "/employee/logout",
                "/backend/**",
                "/front/**",
                "/common/**",
                "/user/sendMsg",//移动端发送信息
                "/user/login"//移动端登陆
        };

        //3.判断本次请求是否需要处理
        boolean check = check(urls, requestURI);

        //4.如果不需要处理,那么直接放行
        if (check) {
            log.info("本次请求{}不需要处理", requestURI);
            filterChain.doFilter(request, response);
            return;
        }

        //5.1 PC端 判断登陆状态,如果已经登陆,直接放行
        if (request.getSession().getAttribute("employee") != null) {
            log.info("用户已经登陆,用户id为{}", request.getSession().getAttribute("employee"));

            Long employee = (Long) request.getSession().getAttribute("employee");
            BaseContext.setCurrentId(employee);

            long id = Thread.currentThread().getId();
            log.info("线程id:{}", id);

            filterChain.doFilter(request, response);
            return;
        }

        //5.2 移动端
        if (request.getSession().getAttribute("user") != null) {
            log.info("用户已经登陆,用户id为{}", request.getSession().getAttribute("user"));

            Long user = (Long) request.getSession().getAttribute("user");
            BaseContext.setCurrentId(user);

            long id = Thread.currentThread().getId();
            log.info("线程id:{}", id);

            filterChain.doFilter(request, response);
            return;
        }


        log.info("用户未登录");

        //6.如果未登录则返回登陆结果,通过输出流方式想客户端页面响应数据p
        response.getWriter().write(JSON.toJSONString(R.error("NOTLOGIN")));
    }

    /**
     * 路径匹配,检查本次请求是否需要放行
     */
    public boolean check(String[] urls, String requestURI) {
        for (String url : urls) {
            boolean match = PATH_MATCHER.match(url, requestURI);
            if (match) {
                return true;
            }
        }
        return false;
    }

}
