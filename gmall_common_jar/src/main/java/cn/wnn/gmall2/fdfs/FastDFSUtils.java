package cn.wnn.gmall2.fdfs;

import org.apache.commons.io.FilenameUtils;
import org.csource.common.NameValuePair;
import org.csource.fastdfs.ClientGlobal;
import org.csource.fastdfs.StorageClient1;
import org.csource.fastdfs.TrackerClient;
import org.csource.fastdfs.TrackerServer;
import org.springframework.core.io.ClassPathResource;

/**
 * Created by Administrator on 2018/4/10 0010.
 */
public class FastDFSUtils {

    public static String uploadPic(byte[] pic,String name,long size){
        String path = null;
        ClassPathResource resource = new ClassPathResource("tracker.conf");
        try {
            ClientGlobal.init(resource.getClassLoader().getResource("tracker.conf").getPath());
            TrackerClient trackerClient = new TrackerClient();
            TrackerServer trackerServer = trackerClient.getConnection();
            StorageClient1 storageClient1 = new StorageClient1(trackerServer, null);

            String ext = FilenameUtils.getExtension(name);

            NameValuePair[] nameValuePairs = new NameValuePair[3];
            nameValuePairs[0]=new NameValuePair("fileName",name);
            nameValuePairs[1]=new NameValuePair("fileExt",ext);
            nameValuePairs[2]=new NameValuePair("fileSize",String.valueOf(size));

            path = storageClient1.upload_file1(pic, ext, nameValuePairs);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return path;
    }
}
