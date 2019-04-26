package cn.wnn.gmall2.controller;

import cn.wnn.gmall2.annotation.LogginRequire;
import cn.wnn.gmall2.bean.CartInfo;
import cn.wnn.gmall2.bean.SkuInfo;
import cn.wnn.gmall2.cart.CartService;
import cn.wnn.gmall2.manager.ManageService;
import cn.wnn.gmall2.web.Constants;
import cn.wnn.gmall2.webutil.CookieUtil;
import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * Created by Administrator on 2018/5/21 0021.
 */
@Controller
public class CartController {
    @Reference
    CartService cartService;
    @Reference
    ManageService manageService;
    @Autowired
    CartCookieHandler cartCookieHandler;


    @RequestMapping("/addToCart")
    @LogginRequire(autoRedirect = false)
    public String addToCart(String skuId, Integer skuNum,
                            HttpServletRequest request, HttpServletResponse response){
        //用户是否登录，根据拦截器中在请求域中放置的usrid进行判断
        String userId = (String) request.getAttribute("userId");

        //统一获取sku信息
        SkuInfo skuInfo = manageService.getSkuInfoById(skuId);

        if(userId==null || userId.length()==0){
            //未登录，放cookie
            cartCookieHandler.addToCart(skuInfo,skuNum,request,response);
        }else{
            //已登录 ，放redis 数据库
            cartService.addToCart(skuInfo,userId,skuNum);
        }

        request.setAttribute("skuInfo",skuInfo);
        request.setAttribute("skuNum",skuNum);

        return "success";
    }

    //展示购物车列表
    @RequestMapping("cartList")
    @LogginRequire(autoRedirect = false)
    public String cartList(HttpServletRequest request,HttpServletResponse response){

        String userId = (String) request.getAttribute("userId");
        List<CartInfo> list = null;
        //根据用户是否登录，查询购物车列表不同
        if(userId!=null && userId.length() > 0){
            //①从数据库和redis中查询
            String cartliststr = CookieUtil.getCookieValue(request, Constants.CART_KEY, true);
            //另如果cookie不为空，需要进行合并
            if(cartliststr!=null && cartliststr.length()>0){
                //1合并购物车列表
                List<CartInfo>  listcookie = JSON.parseArray(cartliststr, CartInfo.class);
                cartService.mergeCart(listcookie,userId);
                //2清空cookie
                CookieUtil.deleteCookie(request,response,Constants.CART_KEY);
            }

            list=cartService.getCartList(userId);

        }else{
            //②从cookie中查询
            list = cartCookieHandler.cartList(request);
        }

        request.setAttribute("cartList",list);
        return "cartList";
    }

    @ResponseBody
    @RequestMapping(value = "checkCart",method = RequestMethod.POST)
    @LogginRequire(autoRedirect = false)
    public void checkCart(HttpServletRequest request,HttpServletResponse response,
                          String skuId,String isChecked){
        //是否登录
        String userId = (String) request.getAttribute("userId");
        if(userId!=null && userId.length()>0){
            //登录，更新redis中的商品状态，并放入选中购物车列表中
            cartService.checkCart(userId,skuId,isChecked);
        }else{
            //未登录，更新cookie中商品状态
            cartCookieHandler.checkCart(request,response,skuId,isChecked);
        }

    }
    @RequestMapping("toTrade")
    @LogginRequire
    public String toTrade(HttpServletRequest request){
        //必须登录
        String userId = (String) request.getAttribute("userId");
        //如果之前未登录需要将cookie中的购物车进行合并,判断cookie是否有购物信息
        List<CartInfo> cartInfos = cartCookieHandler.cartList(request);
        if (cartInfos!=null && cartInfos.size()>0) {
            //商品的选中状态只保存在redis中，数据库中并没有保存
            cartService.mergeCartandupdateCacheStatus(cartInfos,userId);
        }
        return "redirect://order.gmall.com/trade";
    }



}
