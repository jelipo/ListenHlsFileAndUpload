package service;

import org.apache.commons.io.monitor.FileAlterationListenerAdaptor;

import java.io.File;
import java.util.concurrent.BlockingQueue;

/**
 * Created by cao on 2017/2/26.
 */
public class ListenFile extends FileAlterationListenerAdaptor {

    private BlockingQueue<File> queue;
    public ListenFile(BlockingQueue<File> queue){
        this.queue=queue;
    }

    /**
     * 当文件被创建时，本方法开始执行，文件创建时，方法内部开始会循环，每次循环sleep 2秒，每次循环时会获取创建的文件的大小，并把现在文件的大小记录下来，
     * 当下次循环时，获取当前文件大小，并和上次循环所获得的文件大小相比较，如果和上次大小一样，则跳出循环，即判定文件不再变化大小，可以进行上传。
     * 循环结束后，将可以进行上传的文件的File类放入消息队列中。
     * 如果放入消息队列失败，将尝试第二次放入队列，如果再次失败，则放弃此文件
     * @param file 新创建的文件的File类
     */
    @Override
    public void onFileCreate(File file) {
        System.out.println(file.getName()+":创建文件。");
        long fileSize=0;
        while(true){
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            if (file.length()==fileSize){
                break;
            }
            fileSize=file.length();
        }
        System.out.println(file.getName()+"：文件创建完成，存入队列中，等待上传。");
        try {
            queue.put(file);
        } catch (InterruptedException e) {
            System.out.println("存入队列失败，准备重试："+file.getName());
            try {
                queue.put(file);
            } catch (InterruptedException e1) {
                System.out.println("第二次存入队列失败："+file.getName());
            }
        }
    }
}
