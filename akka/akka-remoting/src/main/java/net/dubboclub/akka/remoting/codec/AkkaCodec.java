package net.dubboclub.akka.remoting.codec;

import com.alibaba.dubbo.common.URL;
import com.alibaba.dubbo.common.io.Bytes;
import com.alibaba.dubbo.common.serialize.ObjectInput;
import com.alibaba.dubbo.common.serialize.ObjectOutput;
import com.alibaba.dubbo.common.serialize.Serialization;
import com.alibaba.dubbo.remoting.buffer.*;
import com.alibaba.dubbo.remoting.transport.CodecSupport;
import com.alibaba.dubbo.rpc.Result;
import com.alibaba.dubbo.rpc.RpcException;
import com.alibaba.dubbo.rpc.RpcResult;
import net.dubboclub.akka.remoting.message.Request;
import net.dubboclub.akka.remoting.message.Response;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.Serializable;

/**
 * Created by bieber on 2015/7/9.
 * AKKA协议解析器
 * 协议分为head和body
 * 头分配128位(16个字节)剩下的均是body
 * 头部协议规则
 * MAGIC(16)|FLAG(8)|RESPONSE_STATUS(8)|IDENTIFY(64)|LEN(32)
 */
public class AkkaCodec {

    private static final int HEADER_SIZE=16;

    private static final short MAGIC = (short)0xacca;
    //1000 0000
    private static final byte REQUEST_FLAG = (byte)0x80;
    //0100 0000
    private static final byte RESPONSE_FLAG = (byte)0x40;
    //0001 1111
    private static final byte SERIALIZATION_MASK = (byte)0x1f;

    private static final int ESTIMATED_LENGTH=64;

    public static final String DECODE_ERROR = "DECODE_ERROR";

    public static ChannelBuffer encode(URL url,Object message) throws IOException {
        if(message instanceof Request){
            return encodeRequest(url,message);
        }else if(message instanceof Response){
            return encodeResponse(url,message);
        }
        throw  new IOException("Unsupported encode message "+message+" for url "+url);
    }

    private static ChannelBuffer encodeResponse(URL url,Object message) throws IOException {
        ChannelBuffer buffer = new DynamicChannelBuffer(ESTIMATED_LENGTH);
        Response response = (Response) message;
        byte[] header = new byte[HEADER_SIZE];
        //set magic
        Bytes.short2bytes(MAGIC,header);
        //set flag
        Serialization serialization = CodecSupport.getSerialization(url);
        byte flag = RESPONSE_FLAG;
        flag = (byte)(flag | serialization.getContentTypeId());
        header[2]=flag;
        //set RESPONSE id
        Bytes.long2bytes(response.getId(),header,4);
        header[3]=response.getStatus();
        int savedWriteIndex = buffer.writerIndex();
        buffer.writerIndex(savedWriteIndex+HEADER_SIZE);
        ChannelBufferOutputStream bos = new ChannelBufferOutputStream(buffer);
        ObjectOutput objectOutput = serialization.serialize(url,bos);
        objectOutput.writeObject(response.getResult());
        objectOutput.flushBuffer();
        bos.flush();
        bos.close();
        int len = bos.writtenBytes();
        Bytes.int2bytes(len,header,12);
        buffer.writerIndex(savedWriteIndex);
        buffer.writeBytes(header);
        buffer.writerIndex(savedWriteIndex + HEADER_SIZE + len);
        return buffer;
    }

    private static ChannelBuffer encodeRequest(URL url,Object message) throws IOException {
        ChannelBuffer buffer = new DynamicChannelBuffer(ESTIMATED_LENGTH);
        Request request = (Request) message;
        byte[] header = new byte[HEADER_SIZE];
        //set magic
        Bytes.short2bytes(MAGIC,header);
        //set flag
        Serialization serialization = CodecSupport.getSerialization(url);
        byte flag = REQUEST_FLAG;
        flag = (byte)(flag | serialization.getContentTypeId());
        header[2]=flag;
        //set request id
        Bytes.long2bytes(request.getRequestId(),header,4);
        int savedWriteIndex = buffer.writerIndex();
        buffer.writerIndex(savedWriteIndex+HEADER_SIZE);
        ChannelBufferOutputStream bos = new ChannelBufferOutputStream(buffer);
        ObjectOutput objectOutput = serialization.serialize(url,bos);
        objectOutput.writeObject(request.getmData());
        objectOutput.flushBuffer();
        bos.flush();
        bos.close();
        int len = bos.writtenBytes();
        Bytes.int2bytes(len,header,12);
        buffer.writerIndex(savedWriteIndex);
        buffer.writeBytes(header);
        buffer.writerIndex(savedWriteIndex + HEADER_SIZE + len);
        return buffer;
    }

    public static Object decode(URL url,byte[] bytes) throws IOException {
        ChannelBuffer buffer = new HeapChannelBuffer(bytes);
        byte[] header = new byte[HEADER_SIZE];
        buffer.readBytes(header);
        //get flag
        byte flag = header[2];
        byte id = (byte)(flag&SERIALIZATION_MASK);
        Serialization serialization = CodecSupport.getSerialization(url,id);
        int len = Bytes.bytes2int(header,12);
        long mId = Bytes.bytes2long(header,4);
        if((flag&REQUEST_FLAG)!=0){
            Request request = new Request(mId);
            ChannelBufferInputStream bufferInputStream = new ChannelBufferInputStream(buffer);
            ObjectInput objectInput = serialization.deserialize(url, bufferInputStream);
            try {
                Object object = objectInput.readObject();
                request.setData(object);
            } catch (Throwable e) {
                request.setBroken(true);
                request.setData(e);
            }
            return request;
        }else if((flag&RESPONSE_FLAG)!=0){
            Response response = new Response(mId);
            ChannelBufferInputStream bufferInputStream = new ChannelBufferInputStream(buffer);
            ObjectInput objectInput = serialization.deserialize(url, bufferInputStream);
            response.setStatus(header[3]);
            try {
                Result result = (Result) objectInput.readObject();
                response.setResult(result);
            } catch (Throwable e) {
                response.setStatus(Response.BAD_RESPONSE);
                RpcResult result = new RpcResult();
                result.setException(e);
                response.setResult(result);
            }
            return response;
        }
        return DECODE_ERROR;
    }



}
