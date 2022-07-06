import jdk.internal.util.xml.impl.Input;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class IOThread extends Thread {
    InputStream in;
    OutputStream out;

    public IOThread(InputStream in, OutputStream out) {
        this.in = in;
        this.out = out;
    }

    @Override
    public void run() {
        if(in == null || out == null) return;
        try{
            byte[] bs = new byte[4028000];
            int cnt = 0;
            while((cnt = in.read(bs)) != -1){
                out.write(bs,0,cnt);
                out.flush();
            }
        } catch (IOException e){}
    }
}
