import com.qiniu.common.Zone;
import com.qiniu.http.Response;
import com.qiniu.storage.BucketManager;
import com.qiniu.storage.Configuration;
import com.qiniu.storage.model.BatchStatus;
import com.qiniu.storage.model.FileInfo;
import com.qiniu.util.Auth;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.commons.io.monitor.FileAlterationMonitor;
import org.apache.commons.io.monitor.FileAlterationObserver;
import other.GetConfigParm;
import qiniu.SimpleTools;
import service.ListenFile;
import service.UploadThread;

import java.io.File;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

public class Main{

    public static void main(String[] args) throws Exception {

        BlockingQueue<File> queue = new LinkedBlockingQueue(10);
        String floderPath=getHlsPath();
        //开始文件上传线程
        Thread uploadThread=new Thread(new UploadThread(queue,floderPath));
        System.out.println("1");
        uploadThread.start();
        //开始监听文件变更线程
        startListen(queue,floderPath);
    }


    /**
     * 开启监听本地文件线程
     * @param queue 和上传文件线程传递File类的消息通道
     * @param floderPath 文件夹的路径
     * @throws Exception
     */
    private static void startListen(BlockingQueue<File> queue,String floderPath) throws Exception {
        long interval = TimeUnit.SECONDS.toMillis(5);
        FileAlterationObserver observer = new FileAlterationObserver(floderPath, FileFilterUtils.and(
                FileFilterUtils.fileFileFilter(),FileFilterUtils.suffixFileFilter(".ts")));
        observer.addListener(new ListenFile(queue)); //设置文件变化监听器
        FileAlterationMonitor monitor = new FileAlterationMonitor(interval, observer);
        monitor.start();
    }

    /**
     * 根据当前运行的操作系统环境来获取配置文件中的路径
     * @return 返回要监听的文件夹路径
     */
    private static String getHlsPath(){
        String osName=System.getProperty("os.name");
        if (osName.contains("win")||osName.contains("Win")){
            return GetConfigParm.get("hls.testPath");
        }
        return GetConfigParm.get("hls.piHlsPath");
    }




}
