package service;

import okhttp3.*;
import other.GetConfigParm;
import qiniu.UploadFile;

import java.io.*;
import java.util.Properties;
import java.util.concurrent.BlockingQueue;

/**
 * Created by cao on 2017/2/26.
 */
public class UploadThread implements Runnable {
    private BlockingQueue<File> queue;
    private UploadFile uploadFile;
    private OkHttpClient httpClient;
    private String some;
    private String floderPath;

    public UploadThread(BlockingQueue<File> queue, String floderPath) throws IOException {
        this.queue = queue;
        this.uploadFile = new UploadFile();
        this.floderPath=floderPath;
        this.httpClient = new OkHttpClient();
        this.some = GetConfigParm.get("hls.some");
    }


    /**
     * 从队列中读取一个新建的ts文件（File实例），上传到cdn后，并把参数发送到服务器上
     */
    @Override
    public void run() {
        while (true) {
            File file = null;
            try {
                file = this.queue.take();
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                System.out.println("从队列中取得数据失败");
                continue;
            }
            String videoTime = null;
            try {
                videoTime = getVideoTime(file);
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (videoTime == null) {
                continue;
            }
            String fileName=file.getName();
            String num=fileName.replace(".ts","");
            String foramtTime=getFormatTime(videoTime);
            String fileUrl = this.uploadFile.coverSimpleUpload(file, "hls/" + file.getName());
            send(fileName, fileUrl,videoTime,num,foramtTime);

        }
    }



    /**
     * 从m38u文件中读取某个ts文件的时长
     *
     * @param file ts文件的File实例
     * @return 时长
     */
    private String getVideoTime(File file) throws IOException {
        FileReader m3u8 = new FileReader(floderPath + ".m3u8");
        BufferedReader reader = new BufferedReader(m3u8);
        StringBuilder result = new StringBuilder();
        String theLine = null;
        try {
            while ((theLine = reader.readLine()) != null) {
                result.append(theLine + "\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        reader.close();
        m3u8.close();
        String fileName = file.getName();
        int start = result.indexOf(fileName);
        for(;start>0;start--){
            if (result.charAt(start)==','){
                int timeStop=start;
                for (int i=0;i<20;i++){
                    if (result.charAt(start)==':'){
                        return result.substring(++start,timeStop);
                    }
                    start--;
                }
                break;
            }
        }
        return null;
    }

    /**
     * 根据ts视频的时间向上取整
     * @param videoTime
     * @return
     */
    private String getFormatTime(String videoTime){
        float time=Float.valueOf(videoTime);
        return String.valueOf(Math.ceil(time));
    }

    /**
     * 向服务器发送消息
     *
     * @param fileName ts文件的名称，
     * @param fileUrl  ts文件在cdn的url
     */
    private void send(String fileName, String fileUrl,String videoTime,String num,String formatTime) {
        RequestBody formBody = new FormBody.Builder()
                .add("fileUrl", fileUrl)
                .add("fileName", fileName)
                .add("videoTime", videoTime)
                .add("num",num)
                .add("formatTime",formatTime)
                .add("some", this.some)
                .build();
        Request request = new Request.Builder()
                .url(GetConfigParm.get("hls.ServerPath"))
                .post(formBody)
                .build();
        try {
            Response response = this.httpClient.newCall(request).execute();
            System.out.println(response.isSuccessful()?"发送成功！结果"+response.body().string() : "wrong：发送失败！结果"+response.body().string());
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("发送失败" + fileName);
        }
    }
}
