package cn.wnn.gmall2.user;

import cn.wnn.gmall2.bean.UserInfo;
import cn.wnn.gmall2.usermanager.service.UserInfoService;
import cn.wnn.gmall2.webutil.JwtUtil;
import com.alibaba.dubbo.config.annotation.Reference;
import io.jsonwebtoken.JwtException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Created by Administrator on 2018/5/11 0011.
 */
@Controller
public class UserController {


    @Reference
    UserInfoService userInfoService;

    @Value("${token.key}")
    String TOKEN_KEY ;

    @RequestMapping(value = "toRegist")
    public String toRegist(){
        return "regist";
    }

    @RequestMapping("index")
    public String login(HttpServletRequest request){
      //  String originUrl =  request.getHeader("X-forwarded-for");
        final String originUrl = request.getParameter("originUrl");
        request.setAttribute("originUrl", originUrl);
        return "index";
    }

    @RequestMapping(value = "regist",method = RequestMethod.POST)
    public String regist(UserInfo userInfo){
       userInfoService.addUserInfo(userInfo);
        return "redirect:/index";
    }
    @RequestMapping(value = "login",method = RequestMethod.POST)
    @ResponseBody
    public String login(HttpServletRequest request, UserInfo userInfo){

        String originUrl = request.getHeader("X-forwarded-for");
        if(!("".equals(userInfo.getLoginName()) && "".equals(userInfo.getPasswd()))){
            UserInfo user = userInfoService.login(userInfo);
            if(user != null){
                HashMap<String, Object> map = new HashMap<>();
                map.put("nickName",user.getNickName());
                map.put("userId",user.getId());

                String data = JwtUtil.encode(TOKEN_KEY, map, originUrl);
                return data;
            }
        }

        return "fail";
    }

    @RequestMapping("verify")
    @ResponseBody
    public String verify(HttpServletRequest request){

        String currentip = request.getHeader("X-forwarded-for");
        System.out.println("验证ip==================:"+currentip);
        String newToken = request.getParameter("newToken");
        if(newToken==null || newToken.length()==0){
            return "fail";
        }
        try {

            Map<String, Object> map = JwtUtil.decode(newToken, TOKEN_KEY, currentip);
            if(map != null){
                UserInfo userInfo = userInfoService.verify(map);
                if(userInfo!=null){
                    return "success";
                }
            }
        } catch (JwtException e) {
            return "fail";
        }
        return "fail";

    }

}
