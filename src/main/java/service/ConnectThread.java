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

    public ConnectThread(BlockingQueue<String> queue) {
        this.queue = queue;
    }

    @Override
    public void run() {
        Socket socket;
        try {
            String serverIp = GetConfigParm.get("switch.serverIp");
            int serverPort = Integer.valueOf(GetConfigParm.get("switch.serverPort"));
            socket = new Socket(serverIp, serverPort);
            OutputStream os = socket.getOutputStream();//字节输出流
            PrintWriter pw = new PrintWriter(os);//将输出流包装成打印流
            pw.write("I'm Client");
            pw.flush();
            socket.shutdownOutput();
            //3、获取输入流，并读取服务器端的响应信息
            InputStream is = socket.getInputStream();
            BufferedReader br = new BufferedReader(new InputStreamReader(is));
            String info = null;
            while ((info = br.readLine()) != null) {
                queue.put(info);
                pw.write("success");
                pw.flush();
            }
            //4、关闭资源
            br.close();
            is.close();
            pw.close();
            os.close();
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
            System.out.println("放入消息队列失败");
        }

    }
}
