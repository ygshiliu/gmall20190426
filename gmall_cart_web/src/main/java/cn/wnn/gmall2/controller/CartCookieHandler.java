package cn.wnn.gmall2.controller;

import cn.wnn.gmall2.bean.CartInfo;
import cn.wnn.gmall2.bean.SkuInfo;
import cn.wnn.gmall2.web.Constants;
import cn.wnn.gmall2.webutil.CookieUtil;
import com.alibaba.fastjson.JSON;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2018/5/21 0021.
 */
@Component
public class CartCookieHandler {

    public void addToCart(SkuInfo skuInfo,  Integer skuNum,
                          HttpServletRequest request, HttpServletResponse response){

        String cartInfostr = CookieUtil.getCookieValue(request, Constants.CART_KEY, true);
        List<CartInfo>  list = new ArrayList<>();
        boolean flag = false;

        if(cartInfostr!=null && cartInfostr.length()>0){
            list = JSON.parseArray(cartInfostr, CartInfo.class);
            for (CartInfo cartInfo : list) {
                if(cartInfo.getSkuId().equals(skuInfo.getId())){
                    cartInfo.setSkuNum(cartInfo.getSkuNum()+skuNum);
                    flag = true;
                    break;
                }
            }
        }

        if(!flag){
            CartInfo cartInfo = new CartInfo();
            cartInfo.setCartPrice(skuInfo.getPrice());
            cartInfo.setSkuPrice(skuInfo.getPrice());
            cartInfo.setImgUrl(skuInfo.getSkuDefaultImg());
            cartInfo.setSkuId(skuInfo.getId());
            cartInfo.setSkuName(skuInfo.getSkuName());
            cartInfo.setSkuNum(skuNum);

            list.add(cartInfo);
        }

        String cartstr = JSON.toJSONString(list);
        CookieUtil.setCookie(request,response,Constants.CART_KEY,cartstr,Constants.COOKIE_MAXAGE,true);

    }

    public List<CartInfo> cartList(HttpServletRequest request) {
        String cookieValue = CookieUtil.getCookieValue(request, Constants.CART_KEY, true);
        if(cookieValue!=null && cookieValue.length()>0){
            List<CartInfo> cartInfos = JSON.parseArray(cookieValue, CartInfo.class);
            return cartInfos;
        }
        return null;
    }

    public void checkCart(HttpServletRequest request, HttpServletResponse response, String skuId, String isChecked) {
        List<CartInfo> cartInfos = cartList(request);
        if(cartInfos!=null && cartInfos.size()>0){
            for (CartInfo cartInfo : cartInfos) {
                if(cartInfo.getSkuId().equals(skuId)){
                    cartInfo.setIsChecked(isChecked);
                    break;
                }
            }
            String str = JSON.toJSONString(cartInfos);
            CookieUtil.setCookie(request,response,Constants.CART_KEY,str,Constants.COOKIE_MAXAGE,true);
        }
    }
}
