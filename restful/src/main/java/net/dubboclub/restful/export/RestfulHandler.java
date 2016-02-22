package net.dubboclub.restful.export;

import com.alibaba.dubbo.remoting.http.HttpHandler;
import net.dubboclub.restful.export.mapping.ServiceMappingContainer;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Created by bieber on 2015/11/5.
 */
public class RestfulHandler implements HttpHandler {

    private ServiceMappingContainer serviceMappingContainer;

    public RestfulHandler(ServiceMappingContainer serviceMappingContainer) {
        this.serviceMappingContainer = serviceMappingContainer;
    }

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {

    }
}
