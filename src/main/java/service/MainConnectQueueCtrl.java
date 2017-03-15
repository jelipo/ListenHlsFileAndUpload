package service;

import com.pi4j.io.gpio.*;
import other.GetConfigParm;

import java.io.IOException;
import java.net.Socket;
import java.util.concurrent.BlockingQueue;

/**
 * Created by cao on 2017/3/7.
 */
public class MainConnectQueueCtrl implements Runnable {

    private BlockingQueue<String> queue;
    private Socket socket;
    private long lastConnectTime = 0;
    private int heartBeatTime;

    public MainConnectQueueCtrl(BlockingQueue<String> queue) {
        this.queue = queue;
        this.heartBeatTime = Integer.valueOf(GetConfigParm.get("hls.heartBeatTime"));
    }


    @Override
    public void run() {
        while (true) {
            try {
                String needTo = queue.take();
                select(needTo);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 对从消息队列获得的内容作判断,判断是否是命令，找到则执行方法,开始时标记当前得到命令的时间
     * @param needTo 从服务器发送过来的消息内容
     */
    private void select(String needTo) {
        markGetWordTime();
        switch (needTo) {
            case "connectServer":
                connectServer();
                break;
            case "switch":
                switchLED();
                break;
            case "markGetWordTime":
                markGetWordTime();
                break;
            case "checkLastGetWordTime":
                checkLastGetWordTime();
                break;
            case "I'm Server":
                heartbeatEcho();
                break;
            default:
                System.out.println("未找到命令:" + needTo);
                break;
        }
    }

    /**
     * 发送心跳包后，服务器会返回相应的消息，此方法为得到消息时所执行的事情
     */
    private void heartbeatEcho() {
        System.out.println("服务器返回心跳包响应");
    }

    /**
     * 使用当前时间与上一次发送过来消息的时间做计算，如果差值超过发送心跳包的3倍，则判定连接异常断开，并关闭socket连接
     */
    private void checkLastGetWordTime() {
        long nowTime=System.currentTimeMillis();
        if (lastConnectTime==0){
            markGetWordTime();
            return;
        }
        if ((nowTime-lastConnectTime)>(heartBeatTime*3)){
            markGetWordTime();
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 标记此次从服务器发来消息的时间
     */
    private void markGetWordTime() {
        lastConnectTime = System.currentTimeMillis();
    }


    boolean firstConnrctFlag=false;
    int restartTime=Integer.valueOf(GetConfigParm.get("hls.restartConnectTime"));
    /**
     * 如果是第一次连接或者连接未成功过，则不睡眠，直接执行连接内容。
     * 如果连接成功过，则执行休眠后执行来连接内容。
     * 会在此方法内直接与服务器进行连接，连接成功后，创建可以从服务器接收消息的线程。
     * 然后会创建一个专门负责发送心跳的线程，心跳线程判断连接关闭后会发送消息重新连接
     */
    private void connectServer() {
        if (firstConnrctFlag) {
            try {
                System.out.println(restartTime/1000+"秒后尝试重新连接");
                Thread.sleep(restartTime);
                firstConnrctFlag=true;
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        System.out.println("连接服务器");
        String serverIp = GetConfigParm.get("switch.serverIp");
        int serverPort = Integer.valueOf(GetConfigParm.get("switch.serverPort"));
        try {
            this.socket = new Socket(serverIp, serverPort);
            socket.setKeepAlive(true);
            firstConnrctFlag=true;
            ConnectThread connectThread = new ConnectThread(socket, queue);
            Thread thread = new Thread(connectThread, "ServerThread");
            thread.start();
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("连接服务器出现错误");
        }
        creatHeaartbeat();
    }


    /**
     * 创建向服务器发送心跳包的线程，此线程在连接出错时重新连接服务器
     */
    private void creatHeaartbeat() {
        HeartbeatThread heartbeatThread = new HeartbeatThread(socket, queue);
        Thread thread = new Thread(heartbeatThread, "HeartbeatThread");
        thread.start();
    }

    final GpioController gpio = GpioFactory.getInstance();
    // 获取1号GPIO针脚并设置高电平状态，对应的是树莓派上的12号针脚，可以参考pi4j提供的图片。
    final GpioPinDigitalOutput pin = gpio.provisionDigitalOutputPin(RaspiPin.GPIO_01, "LED", PinState.HIGH);

    private void switchLED() {
        System.out.println("开/关");
        pin.toggle();
    }


    protected class PackegSocket {
        protected Socket socket;
    }
}
