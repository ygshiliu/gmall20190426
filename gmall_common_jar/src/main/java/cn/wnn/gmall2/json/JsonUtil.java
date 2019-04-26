package cn.wnn.gmall2.json;

import com.alibaba.fastjson.JSON;

/**
 * Created by Administrator on 2018/5/1 0001.
 */
public class JsonUtil {

    public static String objectToString(Object obj){
        String str = JSON.toJSONString(obj);
        return str;
    }

    public static <T>T stringToObject(String str,Class<T> cla){
        T t = JSON.parseObject(str, cla);
        return t;
    }
}
