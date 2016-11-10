package net.dubboclub.tracing.core.utils;

import net.dubboclub.tracing.core.exception.TracingException;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;
import java.util.regex.Pattern;

/**
 * NetUtils
 * Created by bieber.bibo on 16/11/9
 */

public class NetUtils {

    private static volatile InetAddress LOCAL_ADDRESS = null;

    public static final String LOCALHOST = "127.0.0.1";

    public static final String ANYHOST = "0.0.0.0";

    public static final Pattern IP_PATTERN =  Pattern.compile("\\d{1,3}(\\.\\d{1,3}){3,5}");


    public static InetAddress getLocalAddress()  {
        if(LOCAL_ADDRESS==null){
            try {
                LOCAL_ADDRESS = getLocalAddress0();
            } catch (Throwable throwable) {
                throw new TracingException(throwable);
            }
        }
        return LOCAL_ADDRESS;
    }



    public static byte[] getLocalHostMac(){
        try {
            return NetworkInterface.getByInetAddress(getLocalAddress()).getHardwareAddress();
        } catch (SocketException e) {
            throw new TracingException(e);
        }
    }

    private static InetAddress getLocalAddress0() throws Throwable{
        InetAddress localAddress;
        try {
            localAddress = InetAddress.getLocalHost();
            if (isValidAddress(localAddress)) {
                return localAddress;
            }
            Enumeration<NetworkInterface> interfaceEnumeration =  NetworkInterface.getNetworkInterfaces();
            while(interfaceEnumeration.hasMoreElements()){
                NetworkInterface networkInterface = interfaceEnumeration.nextElement();
                Enumeration<InetAddress> inetAddressEnumeration = networkInterface.getInetAddresses();
                while(inetAddressEnumeration.hasMoreElements()){
                    InetAddress inetAddress = inetAddressEnumeration.nextElement();
                    if(isValidAddress(inetAddress)){
                        return inetAddress;
                    }
                }
            }
        } catch (Throwable e) {
            throw e;
        }
        return localAddress;
    }


    private static boolean isValidAddress(InetAddress inetAddress){
        if(inetAddress==null||inetAddress.isLoopbackAddress()){
            return false;
        }
        String address = inetAddress.getHostAddress();
        if(address==null||LOCALHOST.equals(address)||ANYHOST.equals(address)){
            return false;
        }
        return IP_PATTERN.matcher(address).matches();
    }
}
