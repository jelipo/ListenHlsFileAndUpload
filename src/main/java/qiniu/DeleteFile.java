package qiniu;

import com.qiniu.common.QiniuException;
import com.qiniu.common.Zone;
import com.qiniu.http.Response;
import com.qiniu.storage.BucketManager;
import com.qiniu.storage.Configuration;
import com.qiniu.storage.model.BatchStatus;
import com.qiniu.storage.model.FileInfo;
import com.qiniu.util.Auth;
import org.apache.commons.lang3.ArrayUtils;

import java.util.Arrays;
import java.util.Spliterators;

/**
 * Created by cao on 2017/2/27.
 */
public class DeleteFile {

    private BucketManager bucketManager;

    public void deleteFileByAuth(String bucketName, String fileKey, Auth auth) {
        delete(bucketName, fileKey, auth);
    }

    public void deleteFile(String bucketName, String fileKey, String accessKey, String secretKey) {
        Auth auth = Auth.create(accessKey, secretKey);
        delete(bucketName, fileKey, auth);
    }

    public void batchDeleteFile(String keyList[], String bucketName, Auth auth) {
        int maxBlock = 999;
        int keyListLength = keyList.length;
        if (keyListLength >= maxBlock) {
            int lastlength = 0;
            int fullBlockNum = keyListLength / maxBlock;
            for (int i = 0; i < fullBlockNum; i++) {
                String[] keysBlock = ArrayUtils.subarray(keyList, lastlength, (i + 1) * maxBlock);
                batchDelete(keysBlock, bucketName, auth);
                lastlength=(i + 1) * maxBlock;
            }
            if (keyListLength%maxBlock!=0){
                String[] keysBlock = ArrayUtils.subarray(keyList, lastlength,keyListLength);
                batchDelete(keysBlock, bucketName, auth);
            }
            return;
        }
        batchDelete(keyList, bucketName, auth);
    }

    public void batchDeleteFile(FileInfo fileInfos[], String bucketName, Auth auth) {
        int keyListLength = fileInfos.length;
        String[] keyList = new String[keyListLength];
        for (int i = 0; i < keyListLength; i++) {
            keyList[i] = fileInfos[i].key;
        }
        batchDeleteFile(keyList, bucketName, auth);
    }

    private void delete(String bucketName, String fileKey, Auth auth) {
        //...其他参数参考类注释
        if (bucketManager == null) {
            Configuration cfg = new Configuration(Zone.zone0());
            this.bucketManager = new BucketManager(auth, cfg);
        }
        try {
            bucketManager.delete(bucketName, fileKey);
        } catch (QiniuException ex) {
            //如果遇到异常，说明删除失败
            System.err.println(ex.code());
            System.err.println(ex.response.toString());
        }
    }

    private void batchDelete(String keyList[], String bucketName, Auth auth) {
        BucketManager.BatchOperations batchOperations = new BucketManager.BatchOperations();
        batchOperations.addDeleteOp(bucketName, keyList);
        if (bucketManager == null) {
            Configuration cfg = new Configuration(Zone.zone0());
            this.bucketManager = new BucketManager(auth, cfg);
        }
        Response response = null;
        BatchStatus[] batchStatusList = new BatchStatus[0];
        try {
            response = bucketManager.batch(batchOperations);
            batchStatusList = response.jsonToObject(BatchStatus[].class);
        } catch (QiniuException e) {
            e.printStackTrace();
        }

        for (int i = 0; i < keyList.length; i++) {
            BatchStatus status = batchStatusList[i];
            String key = keyList[i];
            System.out.print(key + "\t");
            if (status.code == 200) {
                System.out.println("delete success");
            } else {
                System.out.println(status.data.error);
            }
        }
    }
}
