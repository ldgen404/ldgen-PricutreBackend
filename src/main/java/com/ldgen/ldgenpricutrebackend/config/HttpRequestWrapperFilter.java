package com.ldgen.ldgenpricutrebackend.config;

import cn.hutool.http.ContentType;
import cn.hutool.http.Header;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

/**
 * 请求包装过滤器
 *
 * @author pine
 */
@Order(1)
@Component
public class HttpRequestWrapperFilter implements Filter {

    /**
     * 执行过滤操作
     *
     * 该方法用于过滤请求，判断请求的Content-Type，如果为JSON，则使用RequestWrapper包装请求对象
     * 这样做是为了在不改变原始逻辑的前提下，对特定类型的请求进行预处理或增强处理
     *
     * @param request 请求对象，用于获取请求信息
     * @param response 响应对象，用于向客户端返回结果
     * @param chain 过滤链，用于将请求传递给下一个过滤器或目标资源
     * @throws ServletException 如果过滤过程中发生Servlet异常
     * @throws IOException 如果过滤过程中发生I/O异常
     */
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws ServletException, IOException {
        // 检查请求是否为HttpServletRequest实例
        if (request instanceof HttpServletRequest) {
            HttpServletRequest servletRequest = (HttpServletRequest) request;
            // 获取请求的Content-Type头信息
            String contentType = servletRequest.getHeader(Header.CONTENT_TYPE.getValue());
            // 判断请求的Content-Type是否为JSON
            if (ContentType.JSON.getValue().equals(contentType)) {
                // 可以再细粒度一些，只有需要进行空间权限校验的接口才需要包一层
                // 对于JSON类型的请求，使用RequestWrapper进行包装，以进行进一步的处理
                chain.doFilter(new RequestWrapper(servletRequest), response);
            } else {
                // 对于非JSON类型的请求，直接传递给下一个过滤器或目标资源
                chain.doFilter(request, response);
            }
        }
    }

}
