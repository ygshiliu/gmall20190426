package cn.wnn.gmall2.manager.controller;

import cn.wnn.gmall2.fdfs.FastDFSUtils;
import cn.wnn.gmall2.web.Constants;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

/**
 * Created by Administrator on 2018/4/19 0019.
 */
@Controller
public class FileUploadController {

    @RequestMapping(value = "fileUpload",method = RequestMethod.POST)
    @ResponseBody
    public String fileUpload(@RequestParam("file") MultipartFile file){
        String url=null;
        try {
            String name = file.getOriginalFilename();
            byte[]   bytes = file.getBytes();
            long size = file.getSize();

            String path = FastDFSUtils.uploadPic(bytes, name, size);

            url= Constants.IMG_URL+path;
            System.out.println(url);

           /* JSONObject js = new JSONObject();
            js.put("url",url);

            response.setContentType("application/json;charset=utf-8");
            response.getWriter().write(js.toString());*/

        } catch (IOException e) {
            e.printStackTrace();
        }
        return url;
    }

}
