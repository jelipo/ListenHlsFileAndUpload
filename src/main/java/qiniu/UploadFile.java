package qiniu;

import com.qiniu.common.QiniuException;
import com.qiniu.common.Zone;
import com.qiniu.http.Response;
import com.qiniu.storage.Configuration;
import com.qiniu.storage.UploadManager;
import com.qiniu.util.Auth;
import other.GetConfigParm;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;


/**
 * Created by cao on 2017/1/23.
 */


public class UploadFile {

    private String bucketName;
    private Auth auth;
    private Configuration c;
    private UploadManager uploadManager;
    private String urlPrefix;
    public UploadFile() throws IOException {

        this.c = new Configuration(Zone.zone0());
        this.uploadManager = new UploadManager(this.c);
        this.auth = Auth.create(GetConfigParm.get("qiniu.ACCESS_KEY"), GetConfigParm.get("qiniu.SECRET_KEY"));
        this.bucketName = GetConfigParm.get("qiniu.bucketName");
        this.urlPrefix=GetConfigParm.get("qiniu.cdnDomainName");
    }


    public void simpleUploadByCustom(String localPath, String CDNFileName, UploadManager uploadManager, Auth auth, String bucketName) {
        File localFile = new File(localPath);
        upload(localFile, CDNFileName, uploadManager, auth.uploadToken(bucketName));
    }

    public String coverSimpleUpload(String localPath, String CDNFileName) throws IOException {

        File localFile = new File(localPath);
        upload(localFile, CDNFileName, uploadManager, auth.uploadToken(bucketName, CDNFileName));
        return urlPrefix+CDNFileName;
    }

    /**
     * 上传到文件到七牛云存储中，默认是覆盖上传
     * @param file 本地文件的File类
     * @param CDNFileName 存储到CDN的文件名称
     * @return 返回存储到CDN后的http地址
     */
    public String coverSimpleUpload(File file, String CDNFileName) {
        upload(file, CDNFileName, uploadManager, auth.uploadToken(bucketName, CDNFileName));
        return urlPrefix+CDNFileName;
    }

    /**
     * 实际参与上传的方法
     * @param localFile 本地文件的File类
     * @param CDNFileName 存储到CDN的文件名称
     * @param uploadManager 七牛的uploadmanager
     * @param uploadToken 上传的token
     */
    private void upload(File localFile, String CDNFileName, UploadManager uploadManager, String uploadToken) {
        if (localFile.exists()) {
            Response res = null;
            try {
                res = uploadManager.put(localFile, CDNFileName, uploadToken);
                Boolean isSuccess = res.isJson();
                if (isSuccess) {
                    System.out.println("上传成功："+new String(res.body()));
                }
            } catch (QiniuException e) {
                System.out.println("上传失败,fileName:" + CDNFileName + ",localPath:" + localFile.getAbsolutePath());

            }
        } else {
            System.out.println("本地文件不存在！ fileName:" + CDNFileName + ",localPath:" + localFile.getAbsolutePath());
        }
    }

}
