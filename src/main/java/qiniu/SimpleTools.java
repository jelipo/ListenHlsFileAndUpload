package qiniu;

import com.qiniu.common.QiniuException;
import com.qiniu.common.Zone;
import com.qiniu.storage.BucketManager;
import com.qiniu.storage.Configuration;
import com.qiniu.storage.model.FileInfo;
import com.qiniu.storage.model.FileListing;
import com.qiniu.util.Auth;
import org.apache.commons.lang3.ArrayUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class SimpleTools {


    public FileInfo[] getCdnFileListByAuth(String bucketName, String prefix, String a, String b) throws QiniuException {
        Auth auth = Auth.create(a, b);
        return getFileListing(auth, bucketName, prefix, null, 100, null);

    }

    /**
     * 根据文件前缀，获得CDN中的文件数组
     * @param auth 由七牛公钥和私钥所组成的Auth对象
     * @param bucketName cdn存储空间的名称
     * @param prefix 文件名前缀
     * @param delimiter 指定目录分隔符，列出所有公共前缀（模拟列出目录效果）。缺省值为空字符串
     * @return 返回获FileInfo的数组
     * @throws QiniuException
     */
    public FileInfo[] getCdnFileListByAuth(Auth auth, String bucketName, String prefix,  String delimiter) throws QiniuException {
        return getFileListing(auth, bucketName, prefix, null, 100, delimiter);
    }

    /**
     * 调用getList方法，将返回的List中FileListing的所有结果放入数组中，并返回
     * @param auth 由七牛公钥和私钥所组成的Auth对象
     * @param bucketName cdn存储空间的名称
     * @param prefix 文件名前缀
     * @param marker 上一次获取文件列表时返回的 marker
     * @param limit 每次迭代的长度限制，七牛官方说明：最大1000，推荐值 100
     * @param delimiter 指定目录分隔符，列出所有公共前缀（模拟列出目录效果）。缺省值为空字符串
     * @return 返回根据文件前缀所获得的所有文件数组
     */
    private FileInfo[] getFileListing(Auth auth, String bucketName, String prefix, String marker, int limit, String delimiter) {
        List<FileListing> list=getList(new ArrayList(), auth, bucketName, prefix, marker, limit, delimiter);
        FileInfo[] fileInfos=list.get(0).items;
        for (int i=1;i<list.size();i++){
            fileInfos=ArrayUtils.addAll(fileInfos, list.get(i).items);
        }
        return fileInfos;
    }


    /**
     * 使用七牛的SDK向服务器获取文件信息列表，当单次请求大于等于limit参数时，会迭代开始请求下一次请求，每次迭代结果都放入List中，
     * 迭代完成时返回所有迭代结果所集成的List
     * @param list 迭代时共有的对象，每次结果都放入此List
     * @param auth 由七牛公钥和私钥所组成的Auth对象
     * @param bucketName cdn存储空间的名称
     * @param prefix 文件名前缀
     * @param marker 上一次获取文件列表时返回的 marker
     * @param limit 每次迭代的长度限制，七牛官方说明：最大1000，推荐值 100
     * @param delimiter 指定目录分隔符，列出所有公共前缀（模拟列出目录效果）。缺省值为空字符串
     * @return
     */
    private List<FileListing> getList(List list, Auth auth, String bucketName, String prefix, String marker, int limit, String delimiter) {
        Zone z = Zone.zone0();
        Configuration config = new Configuration(z);
        FileListing fileListing = null;
        try {
            //实例化一个BucketManager对象
            BucketManager bucketManager = new BucketManager(auth, config);
            //调用listFiles方法列举指定空间的指定文件
            //参数一：bucket    空间名
            //参数二：prefix    文件名前缀
            //参数三：marker    上一次获取文件列表时返回的 marker
            //参数四：limit     每次迭代的长度限制，最大1000，推荐值 100
            //参数五：delimiter 指定目录分隔符，列出所有公共前缀（模拟列出目录效果）。缺省值为空字符串
            fileListing = bucketManager.listFiles(bucketName, prefix, marker, limit, delimiter);
            list.add(fileListing);
            if (fileListing.items.length >= limit) {
                getList(list, auth, bucketName, prefix, fileListing.marker, limit, delimiter);
            }
        } catch (QiniuException e) {
            e.printStackTrace();
        }
        return list;
    }

    /**
     * 类似于工具方法，将FileInfo数组中的每个FileInfo的“key”变量作为Map的key，再把FileInfo作为Value，放入Map。
     * @param fileInfos FileInfo数组
     * @return 返回FileInfo的Map
     */
    public Map<String,FileInfo> FileInfoArray2MapByKey(FileInfo[] fileInfos) {
        Map<String, FileInfo> map = new HashMap<>();
        for (int i = 0; i < fileInfos.length; i++) {
            map.put(fileInfos[i].key, fileInfos[i]);
        }
        return map;
    }

}
