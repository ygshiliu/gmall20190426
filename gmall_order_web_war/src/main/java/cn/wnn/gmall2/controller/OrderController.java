package cn.wnn.gmall2.controller;

import cn.wnn.gmall2.annotation.LogginRequire;
import cn.wnn.gmall2.bean.*;
import cn.wnn.gmall2.cart.CartService;
import cn.wnn.gmall2.manager.ManageService;
import cn.wnn.gmall2.order.service.OrderInfoService;
import cn.wnn.gmall2.usermanager.service.UserInfoService;
import cn.wnn.gmall2.util.HttpClientUtil;
import cn.wnn.gmall2.web.Constants;
import com.alibaba.dubbo.config.annotation.Reference;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.*;

/**
 * Created by Administrator on 2018/4/13 0013.
 */
@Controller
public class OrderController {

    @Reference
    UserInfoService userInfoService;

    @Reference
    CartService cartService;

    @Reference
    OrderInfoService orderInfoService;
    @Reference
    ManageService manageService;

    @ResponseBody
    @RequestMapping(value = "initOrder")
    public String initOrder(){

        UserInfo userInfo = new UserInfo();
        userInfo.setName("xixi");
        userInfo.setEmail("xixi@163.com");
        userInfo.setPasswd("123123");
        userInfoService.addUserInfo(userInfo);

//        String userId = request.getParameter("userId");
//        List<UserAddress> userAddressList = userManageService.getUserAddressList(userId);
//        String jsonString = JSON.toJSONString(userAddressList);
        return  "123";

    }

    @RequestMapping("trade")
    @LogginRequire
    public String trade(HttpServletRequest request, HttpSession session){

        String userId = (String)request.getAttribute("userId");
        //通过cartService获取redis中选中的商品
        List<CartInfo> checkCartList = cartService.getCheckCartList(userId);
        ArrayList<OrderDetail> OrderDetails = new ArrayList<>(checkCartList.size());
        for (CartInfo cartInfo : checkCartList) {
            OrderDetail orderDetail = new OrderDetail();
            orderDetail.setSkuId(cartInfo.getSkuId());
            orderDetail.setSkuName(cartInfo.getSkuName());
            orderDetail.setImgUrl(cartInfo.getImgUrl());
            orderDetail.setSkuNum(cartInfo.getSkuNum());
            orderDetail.setOrderPrice(cartInfo.getCartPrice());
            OrderDetails.add(orderDetail);
        }

        OrderInfo orderInfo = new OrderInfo();
        orderInfo.setOrderDetailList(OrderDetails);
        orderInfo.setUserId(userId);
        orderInfo.sumTotalAmount();
        //如果没有购物商品，则不能到达下单页面，提示去购物
        if(orderInfo.getOrderDetailList()==null&&orderInfo.getOrderDetailList().size()==0){
            request.setAttribute("errMsg","您还没有选购的商品，请先选购商品!!");
            return "tradeFail";
        }
        request.setAttribute("orderInfo",orderInfo);

        // 通过usermanager获取用户收货地址、支付方式
        // 将其并展示到页面中
        List<UserAddress> userAddressList = userInfoService.getUserAddressList(userId);
        request.setAttribute("userAddressList",userAddressList);

        // 同时防止重复提交下单设置令牌，针对不同用户生成令牌，放在redis中
        //thymeleaf不能获取session中的数据
//        String token = System.currentTimeMillis()+""+userId+""+new Random().nextInt(1000);
//        session.setAttribute("token",token);
        String token = orderInfoService.getTradeCode(userId);
        request.setAttribute("token",token);

        return "trade";
    }

    @RequestMapping("submitOrder")
    @LogginRequire
    public String submitOrder(OrderInfo orderInfo,HttpServletRequest request){
        //防止重复提交
        String userId = (String) request.getAttribute("userId");
        String reqToken = request.getParameter("token");
        boolean existTradeCode =  orderInfoService.checkTradeCode(userId,reqToken);
        if(!existTradeCode){
            request.setAttribute("errMsg","该页面已经失效，请重新结算！");
            return "tradeFail";
        }

        //验价
        List<OrderDetail> orderDetailList = orderInfo.getOrderDetailList();
        for (OrderDetail orderDetail : orderDetailList) {
            String skuId = orderDetail.getSkuId();
            SkuInfo skuInfo = manageService.getSkuInfoById(skuId);
            if(orderDetail.getOrderPrice().compareTo(skuInfo.getPrice())!=0){
                request.setAttribute("errMsg","您选择的商品可能存在价格变动，请重新下单！");
                return "tradeFail";
            }

            //验库
            boolean flag = checkStock(orderDetail.getSkuNum(), skuId);
            if(!flag){
                request.setAttribute("errMsg","您选择的商品【"+orderDetail.getSkuName()+"】库存不足，请重新下单！");
                return "tradeFail";
            }
        }

        //封装订单剩余信息
        orderInfo.setUserId(userId);
        orderInfo.setOrderStatus(OrderStatus.UNPAID);
        orderInfo.setProcessStatus(ProcessStatus.UNPAID);
        orderInfo.setCreateTime(new Date());

        Calendar instance = Calendar.getInstance();
        instance.add(Calendar.DATE,2);
        orderInfo.setExpireTime(instance.getTime());

        String outTradeNo="ATGUIGU"+System.currentTimeMillis()+""+userId+new Random().nextInt(1000);
        orderInfo.setOutTradeNo(outTradeNo);

        //保存订单
        orderInfoService.saveOrderInfo(orderInfo);
        //TODO:清空购物车中已经提交的项
        //TODO:锁定库存
        //清除令牌
        orderInfoService.delTradeCode(userId);
        //重定向到支付系统
        return "redirect://payment.gmall.com/index?orderId="+orderInfo.getId();
    }

    private boolean checkStock(Integer num,String skuId){
        String url = Constants.GWARE_URL+"?skuId="+skuId+"&num="+num;
        String result = HttpClientUtil.doGet(url);
        if("1".equals(result)){
            return true;
        }
        return false;

    }
}
