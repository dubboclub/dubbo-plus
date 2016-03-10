package net.dubboclub.restful.util;

import net.dubboclub.restful.export.mapping.MethodHandler;
import net.dubboclub.restful.export.mapping.ServiceHandler;
import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.StringUtils;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @date: 2016/3/4.
 * @author:bieber.
 * @project:dubbo-side.
 * @package:net.dubboclub.restful.util.
 * @version:1.0.0
 * @fix:
 * @description: 描述功能
 */
public class ServicesUtil {

    private static final byte[] TD="td".getBytes();

    private static final byte[] SLASH="/".getBytes();

    private static final byte[] TR="tr".getBytes();

    private static final byte[] GREAT_THAN=">".getBytes();

    private static final byte[] LESS_THAN="<".getBytes();


    private static final byte[] QUOT="\"".getBytes();

    private static final byte[] ROWSPAN="rowspan".getBytes();

    private static final byte[] SPACE=" ".getBytes();

    private static final byte[] EQUALITY="=".getBytes();

    private static final String STYPE="<style type=\"text/css\">\n" +
            "    table,td,th{\n" +
            "        border:1px solid gray;\n" +
            "        border-collapse: collapse;\n" +
            "        width: 90%;\n" +
            "        font-size: 12px;\n" +
            "        \n" +
            "    }\n" +
            "    \n" +
            "    path{\n" +
            "        color:forestgreen;\n" +
            "        font-weight: bolder;\n" +
            "    }\n" +
            "    \n" +
            " \n" +
            "    \n" +
            "    body{\n" +
            "        text-align: center;\n" +
            "        padding-left: 5%;\n" +
            "        padding-right: 5%;\n" +
            "    }\n" +
            "    \n" +
            "    td{\n" +
            "        height: 14px;\n" +
            "        width: auto\n" +
            "    }\n" +
            "    serviceType{\n" +
            "        color: deeppink;\n" +
            "    }\n" +
            "    methodName{\n" +
            "        color: firebrick\n" +
            "    }\n" +
            "    args{\n" +
            "        color:darkorchid;\n" +
            "    }\n" +
            "    group{\n" +
            "        color: darkseagreen;\n" +
            "    }\n" +
            "    version{\n" +
            "        color: darkorange;\n" +
            "    }\n" +
            "</style>";

    private static final String HEADER="  <head>\n" +
            "        <title>Restful service list</title>\n" +
            "        <meta http-equiv=\"Content-Type\" content=\"text/html;charset=utf-8\">\n" +
            "    </head>";


    public static void writeServicesHtml(OutputStream outputStream,ConcurrentHashMap<String, ServiceHandler> serviceHandlerConcurrentHashMap) throws IOException {
        writeHtmlStart(outputStream);
        writeHeader(outputStream);
        writeStyle(outputStream);
        writeBodyStart(outputStream);
        writeHead(outputStream);
        writeTableStart(outputStream);
        writeServiceHead(outputStream);
        for(Map.Entry<String,ServiceHandler> entry:serviceHandlerConcurrentHashMap.entrySet()){
            writeService(outputStream,entry.getKey(),entry.getValue());
        }
        writeTableEnd(outputStream);
        writeBodyEnd(outputStream);
        writeHtmlEnd(outputStream);
    }

    private static void writeHtmlStart(OutputStream outputStream) throws IOException {
        outputStream.write(encodeBytes("<html>"));
    }

    private static void writeHeader(OutputStream outputStream) throws IOException {
        outputStream.write(encodeBytes(HEADER));
    }

    private static void writeHtmlEnd(OutputStream outputStream) throws IOException {
        outputStream.write(encodeBytes("</html>"));
    }

    private static void writeTableStart(OutputStream outputStream) throws IOException {
        outputStream.write(encodeBytes("<table>"));
    }

    private static void writeStyle(OutputStream outputStream) throws IOException {
        outputStream.write(encodeBytes(STYPE));
    }

    private static void writeBodyStart(OutputStream outputStream) throws IOException {
        outputStream.write(encodeBytes("<body>"));
    }

    private static void writeBodyEnd(OutputStream outputStream) throws IOException {
        outputStream.write(encodeBytes("</body>"));
    }

    private static void writeTableEnd(OutputStream outputStream) throws IOException {
        outputStream.write(encodeBytes("</table>"));
    }

    private static void writeHead(OutputStream outputStream) throws IOException {
        outputStream.write(encodeBytes("<h1>Dubbo Restful Services Info</h1>"));
    }

    private static void writeServiceHead(OutputStream outputStream) throws IOException {
        outputStream.write(encodeBytes("<thead><tr><th>Service Type</th><th colspan='2'>Methods</th><th>Service Path</th><th>Group</th><th>Version</th></tr></thead>"));
    }


    private static void writeMethodHead(OutputStream outputStream) throws IOException {
        outputStream.write(encodeBytes("<th>MethodName</th><th>Args</th>"));
    }

    private static void writeService(OutputStream outputStream,String path,ServiceHandler serviceHandler) throws IOException {
        startTR(outputStream);
        int size= serviceHandler.getMethodHandlerList().size()+1;
        writeTD(outputStream,serviceHandler.getServiceType().getName(),size,"serviceType");
        writeMethodHead(outputStream);
        writeTD(outputStream,path,size,"path");
        writeTD(outputStream, StringUtils.isEmpty(serviceHandler.getGroup())?"*":serviceHandler.getGroup(),size,"group");
        writeTD(outputStream, StringUtils.isEmpty(serviceHandler.getVersion())?"*":serviceHandler.getVersion(),size,"version");
        endTR(outputStream);
        for(int i=0;i<serviceHandler.getMethodHandlerList().size();i++){
            startTR(outputStream);
            writeMethod(outputStream, (MethodHandler) serviceHandler.getMethodHandlerList().get(i),serviceHandler.getPath(),serviceHandler.getVersion(),serviceHandler.getGroup());
            endTR(outputStream);
        }
    }

    private static void writeMethod(OutputStream outputStream,MethodHandler methodHandler,String servicePath,String version,String group) throws IOException {
        writeTD(outputStream,generateMethodInvokeUrl(methodHandler.getMethodName(),servicePath,version,group),0,"methodname");
        StringBuffer stringBuffer = new StringBuffer();
        for(int i=0;i<methodHandler.getArgTypes().length;i++){
            if(methodHandler.getArgTypes()[i].isArray()){
                stringBuffer.append(methodHandler.getArgTypes()[i].getComponentType().getName()).append("[]").append(",");
            }else{
                stringBuffer.append(methodHandler.getArgTypes()[i].getName()).append(",");
            }

        }
        if(stringBuffer.length()<=0){
            stringBuffer.append("");
        }else{
            stringBuffer.setLength(stringBuffer.length()-1);
        }
        writeTD(outputStream,stringBuffer.toString(),0,"args");
    }

    private static String generateMethodInvokeUrl(String methodName,String servicePath,String version,String group){
        StringBuffer stringBuffer = new StringBuffer("<a ");
        stringBuffer.append("href='").append(servicePath).append("/")
                .append(methodName).append("/")
                .append(StringUtils.isEmpty(version) ? "all" : version)
                .append("/").append(StringUtils.isEmpty(group) ? "all" : group)
                .append("'>").append(methodName).append("</a>");
        return stringBuffer.toString();
    }

    private static void startTR(OutputStream outputStream) throws IOException {
        outputStream.write(LESS_THAN);
        outputStream.write(TR);
        outputStream.write(GREAT_THAN);
    }

    private static void endTR(OutputStream outputStream) throws IOException {
        outputStream.write(LESS_THAN);
        outputStream.write(SLASH);
        outputStream.write(TR);
        outputStream.write(GREAT_THAN);
    }

    private static void writeTD(OutputStream outputStream,String message,int rowspan,String customTag) throws IOException {
        outputStream.write(LESS_THAN);
        outputStream.write(TD);
        if(rowspan>0){
            outputStream.write(SPACE);
            outputStream.write(ROWSPAN);
            outputStream.write(EQUALITY);
            outputStream.write(QUOT);
            outputStream.write(encodeBytes(rowspan+""));
            outputStream.write(QUOT);
        }
        outputStream.write(GREAT_THAN);
        if(!StringUtils.isEmpty(customTag)){
            outputStream.write(LESS_THAN);
            outputStream.write(encodeBytes(customTag));
            outputStream.write(GREAT_THAN);
        }
        outputStream.write(encodeBytes(message));
        if(!StringUtils.isEmpty(customTag)){
            outputStream.write(LESS_THAN);
            outputStream.write(SLASH);
            outputStream.write(encodeBytes(customTag));
            outputStream.write(GREAT_THAN);
        }
        outputStream.write(LESS_THAN);
        outputStream.write(SLASH);
        outputStream.write(TD);
        outputStream.write(GREAT_THAN);
    }


    private static byte[] encodeBytes(String message){
        return message.getBytes();
    }
}
