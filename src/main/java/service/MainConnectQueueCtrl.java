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

    public MainConnectQueueCtrl(BlockingQueue<String> queue) {
        this.queue = queue;
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

    private void select(String needTo) {
        switch (needTo) {
            case "connectServer":
                connectServer();
                break;
            case "switch":
                switchLED();
                break;
            default:
                System.out.println("未找到命令" + needTo);
                break;

        }
    }

    /**
     * 连接服务器
     */
    private void connectServer() {
        if (socket != null) {
            try {
                Thread.sleep(10000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        System.out.println("连接服务器");
        String serverIp = GetConfigParm.get("switch.serverIp");
        int serverPort = Integer.valueOf(GetConfigParm.get("switch.serverPort"));
        try {
            this.socket = new Socket(serverIp, serverPort);
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
