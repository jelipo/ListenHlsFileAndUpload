package service;

import other.GetConfigParm;

import java.net.Socket;
import java.util.concurrent.BlockingQueue;


public class HeartbeatThread implements Runnable {

    private Socket socket;
    private BlockingQueue<String> queue;
    private int heartBeatTime;

    HeartbeatThread(Socket socket, BlockingQueue<String> queue) {
        this.socket = socket;
        this.queue = queue;
        this.heartBeatTime= Integer.valueOf(GetConfigParm.get("hls.heartBeatTime"));
    }

    /**
     * 此线程为循环线程，循环体内会先判断连接是否为空或者是否关闭，是的话，会向消息队列发送重新连接的请求，然后此线程执行完毕。
     * 否的话，会向服务器发送心跳内容
     */
    @Override
    public void run() {

        while (true) {
            if (socket==null||socket.isClosed()){
                restart();
                return;
            }
            try {
                Thread.sleep(heartBeatTime);
                System.out.println("heart");
                SocketTool.send(socket,"I'm client");
                queue.put("checkLastGetWordTime");
            } catch (InterruptedException e) {
                e.printStackTrace();
                System.out.println("连接断开");
                restart();
                return;
            }
        }

    }

    /**
     * 此方法给消息队列发送消息，要求重新连接
     */
    private void restart() {
        try {
            queue.put("connectServer");
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
