package cn.wnn.gmall2.interceptor;

import cn.wnn.gmall2.annotation.LogginRequire;
import cn.wnn.gmall2.util.HttpClientUtil;
import cn.wnn.gmall2.web.Constants;
import cn.wnn.gmall2.webutil.CookieUtil;
import com.alibaba.fastjson.JSON;
import io.jsonwebtoken.impl.Base64UrlCodec;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.HttpClient;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.net.URLEncoder;
import java.util.Map;

/**
 * Created by Administrator on 2018/5/16 0016.
 */
@Component
public class AuthInterceptor extends HandlerInterceptorAdapter {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        System.out.println(request.getRequestURL()+"****************");
        String newToken = request.getParameter("newToken");
        if(newToken!=null && newToken.length()>0){
            CookieUtil.setCookie(request,response,"token",newToken, Constants.COOKIE_MAXAGE,false);
        }
        if(newToken==null || newToken.length()==0){
            newToken = CookieUtil.getCookieValue(request, "token", false);
        }
        if(newToken!=null && newToken.length()>0){
            Map map = getNickNameByToken(newToken);
            String nickName = (String) map.get("nickName");
            request.setAttribute("nickName",nickName);
        }

        HandlerMethod handlerMethod = (HandlerMethod) handler;
        LogginRequire logginRequire = handlerMethod.getMethodAnnotation(LogginRequire.class);
        if(logginRequire != null){
            String ip = request.getHeader("X-forwarded-for");

            String url = null;
            if(newToken==null){
                url =Constants.USER_VERIFY_URL+"?newToken=";
            }else{
                url =Constants.USER_VERIFY_URL+"?newToken="+newToken;
            }

            System.out.println("******登录验证:"+url);
            String result = HttpClientUtil.doGet(url);
            if("success".equals(result)){
                Map map = getNickNameByToken(newToken);
                String userId = (String) map.get("userId");
                request.setAttribute("userId",userId);
                return true;
            }else{
                if(logginRequire.autoRedirect()) {
                    String requestURL = request.getRequestURL().toString();
                    String queryString = request.getQueryString();
                    String temp =requestURL+"?"+queryString;
                    System.out.println("=========拦截器中的原请求地址===========" + temp);

                    String requestURL1 = URLEncoder.encode(temp, "utf-8");
                    url = Constants.USER_LOGGIN_URL + "?originUrl=" + requestURL1;

                    response.sendRedirect(url);
                    return false;
                }
            }

        }

        return true;
    }

    private Map getNickNameByToken(String newToken) {
        String str = StringUtils.substringBetween(newToken, ".");
        Base64UrlCodec base64UrlCodec = new Base64UrlCodec();
        byte[] bytes = base64UrlCodec.decode(str);
        try{
            String s = new String(bytes, "utf-8");
            Map map = JSON.parseObject(s, Map.class);
            return map;
        }catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }

}
