import com.pi4j.io.gpio.*;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.commons.io.monitor.FileAlterationMonitor;
import org.apache.commons.io.monitor.FileAlterationObserver;
import other.GetConfigParm;
import service.ListenFile;
import service.MainConnectQueueCtrl;
import service.UploadThread;

import java.io.File;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

public class Main {

    public static void main(String[] args) throws Exception {

        BlockingQueue<String> mainQueue = new LinkedBlockingQueue(10);
        MainConnectQueueCtrl mainConnectQueueCtrl=new MainConnectQueueCtrl(mainQueue);
        Thread thread=new Thread(mainConnectQueueCtrl);
        thread.start();
        //向队列发送消息，是其连接服务器
        mainQueue.put("connectServer");

        BlockingQueue<File> queue = new LinkedBlockingQueue(10);
        String floderPath=getHlsPath();
        //开始文件上传线程
        Thread uploadThread=new Thread(new UploadThread(queue,floderPath));
        System.out.println("1");
        uploadThread.start();
        //开始监听文件变更线程
        startListen(queue,floderPath);
    }


    public static void start() throws InterruptedException {
        // 创建一个GPIO控制器
        final GpioController gpio = GpioFactory.getInstance();

        // 获取1号GPIO针脚并设置高电平状态，对应的是树莓派上的12号针脚，可以参考pi4j提供的图片。
        final GpioPinDigitalOutput pin = gpio.provisionDigitalOutputPin(RaspiPin.GPIO_01, "LED", PinState.HIGH);

        while (true) {
            //设置高电平
            pin.high();
            System.out.println("打开继电器");
            //睡眠1秒
            Thread.sleep(5000);
            //设置低电平
            pin.low();
            System.out.println("关闭继电器");
            Thread.sleep(5000);
            //切换状态
            //pin.toggle();
        }
    }

        /**
         * 开启监听本地文件线程
         *
         * @param queue      和上传文件线程传递File类的消息通道
         * @param floderPath 文件夹的路径
         * @throws Exception
         */
    private static void startListen(BlockingQueue<File> queue, String floderPath) throws Exception {
        long interval = TimeUnit.SECONDS.toMillis(5);
        FileAlterationObserver observer = new FileAlterationObserver(floderPath, FileFilterUtils.and(
                FileFilterUtils.fileFileFilter(), FileFilterUtils.suffixFileFilter(".ts")));
        observer.addListener(new ListenFile(queue)); //设置文件变化监听器
        FileAlterationMonitor monitor = new FileAlterationMonitor(interval, observer);
        monitor.start();
    }

    /**
     * 根据当前运行的操作系统环境来获取配置文件中的路径
     *
     * @return 返回要监听的文件夹路径
     */
    private static String getHlsPath() {
        String osName = System.getProperty("os.name");
        if (osName.contains("win") || osName.contains("Win")) {
            return GetConfigParm.get("hls.testPath");
        }
        return GetConfigParm.get("hls.piHlsPath");
    }


}
