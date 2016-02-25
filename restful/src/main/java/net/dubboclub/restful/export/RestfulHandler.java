package net.dubboclub.restful.export;

import com.alibaba.dubbo.common.logger.Logger;
import com.alibaba.dubbo.common.logger.LoggerFactory;
import com.alibaba.dubbo.remoting.http.HttpHandler;
import com.alibaba.dubbo.rpc.RpcContext;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import net.dubboclub.restful.exception.NotFoundServiceException;
import net.dubboclub.restful.export.mapping.*;
import net.dubboclub.restful.export.mapping.ServiceHandler;
import net.dubboclub.restful.util.ClassUtils;
import org.apache.commons.lang.StringUtils;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.nio.charset.CharsetDecoder;
import java.util.Enumeration;

/**
 * Created by bieber on 2015/11/5.
 */
public class RestfulHandler implements HttpHandler {

    private ServiceMappingContainer serviceMappingContainer;

    private static final String ALL = "all";

    private Logger logger = LoggerFactory.getLogger(RestfulHandler.class);

    private String contextPath = "/";

    public RestfulHandler(ServiceMappingContainer serviceMappingContainer) {
        this.serviceMappingContainer = serviceMappingContainer;
    }

    /**
     * 所有restful的入口
     * http://ip:port/contextpath/${path}/${method}/${version}/${group}
     * @param request
     * @param response
     * @throws IOException
     * @throws ServletException
     */
    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        String requestUri = request.getRequestURI();
        requestUri = requestUri.substring(requestUri.indexOf(contextPath)+contextPath.length());
        String[] fragments = StringUtils.split(requestUri,"/");
        if(fragments.length==0){
            logger.error("request uri ["+requestUri+"] is incorrect.");
            response.sendError(404);
        }
        String path = fragments[0];
        RequestEntity entity = null;
        byte[] requestContent = copyBytesFromRequest(request);
        try{
            JSONObject jsonObject  = (JSONObject) JSON.parse(requestContent);
            entity = new RequestEntity(jsonObject);
        }catch (Exception e){
            logger.error("Fail to parse request content to json,request uri ["+requestUri+"]");
        }
        if(entity==null){
            entity = new RequestEntity();
        }
        if(fragments.length>=2){
            entity.setMethod(fragments[1]);
        }
        if(fragments.length>=3&&!fragments[2].equals(ALL)){
            entity.setVersion(fragments[2]);
        }
        if(fragments.length>=4&&!fragments[3].equals(ALL)){
            entity.setGroup(fragments[3]);
        }
        readAttachment(request);
        try{
            ServiceHandler serviceHandler = serviceMappingContainer.mappingService(path, entity);
            MethodHandler methodHandler = serviceHandler.mapping(entity);
            if(methodHandler!=null){
                try {
                    Object ret = methodHandler.invoke(convertArgs(entity.getArgs(), methodHandler.getArgTypes()));
                    rendingResponse(response,ret);
                } catch (Exception e) {
                    logger.error("Fail to invoke method ["+methodHandler.toString()+"]",e);
                    response.sendError(500);
                }
            }else{
                logger.warn("Not found method in service ["+serviceHandler.getServiceType().getName()+"],request entity ["+entity.toString()+"].");
                response.sendError(404);
            }
        }catch (NotFoundServiceException e){
            logger.error("Not found service for request uri ["+requestUri+"]",e);
            response.sendError(404);
        }
    }

    private void readAttachment(HttpServletRequest request){
        Enumeration<String> headerNames = request.getHeaderNames();
        while(headerNames.hasMoreElements()){
            String name = headerNames.nextElement();
            if(name.startsWith("protocol_")){
                RpcContext.getContext().setAttachment(name.replaceFirst("protocol_",""),request.getHeader(name));
            }
        }
    }


    private void rendingResponse(HttpServletResponse response,Object ret) throws IOException {
        if(ret!=null){
            try {
                response.getOutputStream().write(JSON.toJSONBytes(ret));
            } catch (IOException e) {
                logger.error("Fail to rending response",e);
                response.sendError(500);
            }
        }
    }

    private Object[] convertArgs(String[] args,Class<?>[] argTypes){
        Object[] objects = new Object[argTypes.length];
        for(int i=0;i<argTypes.length;i++){
            if(!StringUtils.isEmpty(args[i])){
                if(ClassUtils.isBasicType(argTypes[i])){
                    objects[i]=ClassUtils.caseBasicType(argTypes[i],args[i]);
                }else{
                    objects[i]=JSON.parseObject(args[i],argTypes[i]);
                }
            }
        }
        return objects;
    }

    /**
     * 将request body的byte字节数组复制到内存中
     * @param httpServletRequest
     * @return
     * @throws IOException
     */
    private byte[] copyBytesFromRequest(HttpServletRequest httpServletRequest) throws IOException {
        InputStream inputStream = httpServletRequest.getInputStream();
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int offset=-1;
        while((offset=inputStream.read(buffer,0,1024))>0){
            byteArrayOutputStream.write(buffer,0,offset);
        }
        return byteArrayOutputStream.toByteArray();
    }


}
