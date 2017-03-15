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

    /**
     * 此线程会先向服务器发送一次心跳，然后循环获取从服务器获得的所有消息。
     * 如果出现IO异常，会关闭Socket连接
     */
    @Override
    public void run() {
        try {
            System.out.println("向服务器发送第一次消息（心跳）");
            SocketTool.send(socket, "I'm client");
            BufferedInputStream bis = new BufferedInputStream(socket.getInputStream());
            while (true) {
                String message = SocketTool.getMessage(bis);
                queue.put(message);
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
