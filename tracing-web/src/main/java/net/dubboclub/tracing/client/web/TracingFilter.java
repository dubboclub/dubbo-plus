package net.dubboclub.tracing.client.web;

import net.dubboclub.tracing.core.Span;
import net.dubboclub.tracing.core.Tracer;
import net.dubboclub.tracing.core.exception.TracingException;

import javax.servlet.*;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * TracingFilter
 * Created by bieber.bibo on 16/11/9
 */

public class TracingFilter implements Filter {

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        //do nothing
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest httpServletRequest = (HttpServletRequest) servletRequest;
        Cookie[] cookies = httpServletRequest.getCookies();
        Map<String,String> attachments = new HashMap<String,String>();
        for(Cookie cookie:cookies){
            attachments.put(cookie.getName(),cookie.getValue());
        }
        Tracer.startTracing(attachments);
        Span span = Tracer.startSpan(Span.SpanType.RESPONSE,httpServletRequest.getRequestURI(),attachments.get(Tracer.RPC_ID_KEY));
        try{
            span.setApplication(servletRequest.getServletContext().getContextPath());
            span.setBizType(Span.SpanBizType.WEB);
            Tracer.addRemoteHostAnnotation(httpServletRequest.getRemoteAddr());
            Tracer.addAnnotation("User-Agent",httpServletRequest.getHeader("User-Agent"));
            Tracer.addHostAnnotation(servletRequest.getLocalAddr());
            filterChain.doFilter(servletRequest,servletResponse);
        }catch (Exception e){
            Tracer.addErrorAnnotation(e);
            throw new TracingException(e);
        }finally {
            Tracer.stopSpan();
            Tracer.stopTracing();
        }
    }

    @Override
    public void destroy() {
        //do nothing
    }

}
