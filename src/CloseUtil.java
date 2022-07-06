import java.io.Closeable;
import java.io.IOException;

public class CloseUtil {
    public static void close(Closeable ...closeable){
        for(Closeable close : closeable){
            try{
                if(close != null) close.close();
            }catch (IOException e){
                e.printStackTrace();
            }
        }
    }
}
