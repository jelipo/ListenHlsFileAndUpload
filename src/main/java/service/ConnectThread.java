package service;

import other.GetConfigParm;

import java.io.*;
import java.net.Socket;
import java.util.concurrent.BlockingQueue;

/**
 * Created by cao on 2017/3/6.
 */
public class ConnectThread implements Runnable {

    private BlockingQueue<String> queue;
    private Socket socket;

    public ConnectThread(Socket socket, BlockingQueue<String> queue) {
        this.queue = queue;
        this.socket = socket;
    }

    @Override
    public void run() {
        try {
            DataOutputStream out = new DataOutputStream(socket.getOutputStream());
            System.out.println("向服务器发送第一次消息");
            SocketTool.send(socket, "I'm client");
            BufferedInputStream bis = new BufferedInputStream(socket.getInputStream());
            while (true) {
                String message = SocketTool.getMessage(bis);
                queue.put(message);
                System.out.println(message);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            System.out.println("连接断开");
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


}
