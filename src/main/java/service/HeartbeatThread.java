package service;

import java.net.Socket;
import java.util.concurrent.BlockingQueue;

/**
 * Created by cao on 2017/3/7.
 */
public class HeartbeatThread implements Runnable {

    private Socket socket;
    private BlockingQueue<String> queue;

    HeartbeatThread(Socket socket, BlockingQueue<String> queue) {
        this.socket = socket;
        this.queue = queue;
    }

    @Override
    public void run() {

        while (true) {
            if (socket==null||socket.isClosed()){
                restart();
                return;
            }
            try {
                Thread.sleep(30000);
                System.out.println("heart");
                SocketTool.send(socket,"I'm client");
            } catch (InterruptedException e) {
                e.printStackTrace();
                System.out.println("连接断开");
                restart();
                return;
            }
        }

    }

    private void restart() {
        try {
            System.out.println("10s后尝试重新连接服务器");
            queue.put("connectServer");
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
