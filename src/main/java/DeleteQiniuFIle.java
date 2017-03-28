import com.qiniu.common.QiniuException;
import com.qiniu.storage.model.FileInfo;
import com.qiniu.util.Auth;
import qiniu.DeleteFile;
import qiniu.SimpleTools;

/**
 * Created by cao on 2017/3/28.
 */
public class DeleteQiniuFIle {

    public static void main(String[] args) {
        SimpleTools simpleTools = new SimpleTools();
        FileInfo[] fileInfos = new FileInfo[0];
        Auth auth = Auth.create("gj58boOVyhKtrJqmgjnMk5UPrbJ-3ISG33CzgX4A", "3SoI5bpmrYEQbo4dLPi1LIay-dFisUS8up0Ow1f6");
        try {
            fileInfos = simpleTools.getCdnFileListByAuth(auth, "res-blog-springmarker-com", "hls/", null);
        } catch (QiniuException e) {
            e.printStackTrace();
        }
        DeleteFile deleteFile = new DeleteFile();
        System.out.println("准备删除");
        deleteFile.batchDeleteFile(fileInfos, "res-blog-springmarker-com", auth);


    }
}
