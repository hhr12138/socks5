import com.sun.corba.se.impl.orbutil.ObjectUtility;
import com.sun.xml.internal.ws.policy.privateutil.PolicyUtils;

import java.io.*;
import java.net.*;
import java.nio.Buffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ProxyThread extends Thread {
    private Socket socket;
    public static byte SOCKS_VAR = 5;

    public ProxyThread(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        Socket conn = null;
        InputStream in = null;
        OutputStream out = null;
        try{
            in = socket.getInputStream();
            out = socket.getOutputStream();
            auth(in, out);
            conn = connect(in, out);
            commun(conn, in, out);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            CloseUtil.close(in,out,socket,conn);
        }
    }

    private void commun(Socket serverSocket, InputStream clientIn, OutputStream clientOut){
        if(serverSocket == null) return;
        InputStream serverIn = null;
        OutputStream serverOut = null;
        try{
            serverIn = serverSocket.getInputStream();
            serverOut = serverSocket.getOutputStream();
            Thread t1 = new IOThread(serverIn, clientOut);
            Thread t2 = new IOThread(clientIn, serverOut);
            t2.start();
            t1.start();
            t1.join();
            t2.join();
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        } finally {
            CloseUtil.close(serverIn, serverOut);
        }

    }

    private Socket connect(InputStream in, OutputStream out) throws IOException {
        Socket serverSocket = null;
        try{
            in = socket.getInputStream();
            out = socket.getOutputStream();
            byte[] bs = new byte[1024];
            int size = in.read(bs);
            byte ver = bs[0];
            byte cmd = bs[1];
            byte rsv = bs[2];
            byte atyp = bs[3];
            if(ver != SOCKS_VAR) {
                return null;
            }
            //ipv4
            String addr = "";
            if(atyp == 1){
                addr = joinIp(bs);
            } else if(atyp == 3){ //域名
                int len = bs[4];
                addr = new String(bs,5,len);
            }
            if(addr.length() == 0) return null;
            int port = getPort(bs[size - 2],bs[size - 1]);
            serverSocket = new Socket(addr,port);
            //返回响应
            byte[] ans = null;
            if(atyp == 1){
                ans = new byte[]{5,0,0,1,bs[0],bs[1],bs[2],bs[3],bs[size - 2],bs[size - 1]};
            } else{
                InetAddress address = InetAddress.getByName(addr);
                byte[] result = address.getAddress();
                ans = new byte[]{5,0,0,1,result[0],result[1],result[2],result[3],bs[size - 2],bs[size - 1]};
            }
            out.write(ans);
            out.flush();
        } catch (IOException e){
            e.printStackTrace();
        } finally {
            if (out != null) out.flush();
        }
        return serverSocket;
    }

    private int getPort(int num1, int num2){
        if(num1 < 0) num1 += 256;
        if(num2 < 0) num2 += 256;
        return num1*256+num2;
    }

    private String joinIp(byte[] bs){
        if(bs.length < 8) return "";
        StringBuilder sb = new StringBuilder();
        for(int i = 4; i < 8; i++){
            int num = bs[i];
            if(num < 0) num += 256;
            sb.append(num);
            if(i != 7) sb.append('.');
        }
        return sb.toString();
    }

    private void auth(InputStream in, OutputStream out) throws IOException {
        try {
            in = socket.getInputStream();
            out = socket.getOutputStream();
            byte[] bs = new byte[1024];
            in.read(bs,0,1);
            byte ver = bs[0];
            if(ver != SOCKS_VAR){
                System.out.println("version err");
                return;
            }
            in.read(bs,0,1);
            byte nMethods = bs[0];
            if(nMethods < 0){
                System.out.println("nMethods err");
                return;
            }
            in.read(bs,0,nMethods);
            //这里最简单的吧, 不加密
            for(int i = 0; i < nMethods; i++){
                if(bs[i] == 0){
                    break;
                }
                System.out.println("consult err");
            }
            out.write(new byte[]{5,0});
            out.flush();
        } catch (IOException e){
            e.printStackTrace();
        }
    }

    private void test(){
        InputStream in = null;
        OutputStream out = null;
        try {
            in = socket.getInputStream();
            out = socket.getOutputStream();
            byte[] bs = new byte[1024];
            for(int cnt = in.read(bs); cnt != 0; cnt = in.read(bs)){
                out.write(bs,0,cnt);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            CloseUtil.close(in,out);
        }
    }
}
