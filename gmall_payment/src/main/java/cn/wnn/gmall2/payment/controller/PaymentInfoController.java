package cn.wnn.gmall2.payment.controller;

import cn.wnn.gmall2.annotation.LogginRequire;
import cn.wnn.gmall2.bean.OrderInfo;
import cn.wnn.gmall2.bean.PaymentInfo;
import cn.wnn.gmall2.bean.PaymentStatus;
import cn.wnn.gmall2.order.service.OrderInfoService;
import cn.wnn.gmall2.payment.config.AliPayConfig;
import cn.wnn.gmall2.payment.service.PaymentInfoService;
import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.internal.util.AlipaySignature;
import com.alipay.api.request.AlipayTradePagePayRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Administrator on 2018/6/8 0008.
 */
@Controller
public class PaymentInfoController {

    @Reference
    OrderInfoService orderInfoService;
    @Autowired
    PaymentInfoService paymentInfoService;
    @Autowired
    AlipayClient alipayClient;


    @RequestMapping("index")
    @LogginRequire
    public String index(HttpServletRequest request){
       // String userId = (String)request.getAttribute("userId");
        String orderId = request.getParameter("orderId");

        OrderInfo orderInfo = orderInfoService.getOrderInfoById(orderId);
        orderInfo.sumTotalAmount();

        request.setAttribute("totalAmount",orderInfo.getTotalAmount());
        request.setAttribute("orderId",orderId);

        return "index";
    }


    @RequestMapping("/alipay/submit")
    @LogginRequire
    @ResponseBody
    public String submitPay(HttpServletRequest request, HttpServletResponse response){
        //1.根据订单号获取订单相关信息
        String orderId = request.getParameter("orderId");
        OrderInfo orderInfo = orderInfoService.getOrderInfoById(orderId);

        //2.在支付模块保存支付信息，前题是数据库中没有提交过此份订单,去重表操作
        PaymentInfo paymentInfo = new PaymentInfo();
        paymentInfo.setCreateTime(new Date());
        paymentInfo.setOrderId(orderId);
        paymentInfo.setOutTradeNo(orderInfo.getOutTradeNo());
        paymentInfo.setPaymentStatus(PaymentStatus.UNPAID);
        paymentInfo.setSubject(orderInfo.getTradeBody());
        paymentInfo.setTotalAmount(new BigDecimal("0.01"));

        paymentInfoService.savePaymentInfo(paymentInfo);

        //3.制作支付宝参数
        //4.制作签名
        AlipayTradePagePayRequest alipayRequest = new AlipayTradePagePayRequest();//创建API对应的request
        alipayRequest.setReturnUrl(AliPayConfig.return_payment_url);
        alipayRequest.setNotifyUrl(AliPayConfig.notify_payment_url);//在公共参数中设置回跳和通知地址
        HashMap<String, Object> map = new HashMap<>();
        map.put("out_trade_no",paymentInfo.getOutTradeNo());
        map.put("product_code","FAST_INSTANT_TRADE_PAY");
        map.put("total_amount","0.01");
        map.put("subject",paymentInfo.getSubject());

        String bizContent = JSON.toJSONString(map);
        alipayRequest.setBizContent(bizContent);//填充业务参数
        String form="";
        try {
            form = alipayClient.pageExecute(alipayRequest).getBody(); //调用SDK生成表单
        } catch (AlipayApiException e) {
            e.printStackTrace();
        }
        response.setContentType("text/html;charset=utf-8");

        //主动查询支付结果，并使用消息队列通知订单系统
        paymentInfoService.sendDelayQueuePayResult(paymentInfo.getOutTradeNo(),30,3);

        return form;
    }

    @RequestMapping("alipay/callback/return")
    public String paymentReturn(){
        return "redirect://"+AliPayConfig.return_payment_url;
    }

    @RequestMapping(value = "alipay/callback/notify",method = RequestMethod.POST)
    @ResponseBody
    public String paymentNotify(Map<String,String> paramsMap, HttpServletRequest request){
        //1.首先验签的参数
        try {
            boolean signVerified = AlipaySignature.rsaCheckV1(paramsMap, AliPayConfig.alipay_public_key,"utf-8", AliPayConfig.sign_type); //调用SDK验证签名
            if(signVerified){
                // TODO 验签成功后，按照支付结果异步通知中的描述，对支付结果中的业务内容进行二次校验，校验成功后在response中返回success并继续商户自身业务处理，校验失败返回failure
                //2.在参数正确情况下，交易状态为状态TRADE_SUCCESS 和 TRADE_FINISHED代表完成支付
                String trade_status = paramsMap.get("trade_status");
                if("TRADE_SUCCESS".equals(trade_status) || "TRADE_FINISHED".equals(trade_status)){
                    //3.支付完成情况下，查询订单状态
                    String out_trade_no = paramsMap.get("out_trade_no");
                    PaymentInfo paymentInfo = new PaymentInfo();
                    paymentInfo.setOutTradeNo(out_trade_no);
                    PaymentInfo paymentInfoDB = paymentInfoService.getPaymentInfo(paymentInfo);
                    if(paymentInfoDB.getPaymentStatus() != PaymentStatus.ClOSED || paymentInfoDB.getPaymentStatus() != PaymentStatus.PAID){
                        //4.在订单未支付过情况下，修改状态
                        paymentInfoDB.setCallbackTime(new Date());
                        paymentInfoDB.setCallbackContent(paramsMap.toString());
                        paymentInfoDB.setPaymentStatus(PaymentStatus.PAID);
                        paymentInfoService.updatePaymentInfoStatus(paymentInfoDB);

                        //5.同时使用消息队列，异步通知订单模块
                        paymentInfoService.sendQueuePayResult(paymentInfoDB,"success");
                        //6.返回支付宝信息
                        return "success";

                    }

                }

            }else{
                // TODO 验签失败则记录异常日志，并在response中返回failure.
            }


        } catch (AlipayApiException e) {
            e.printStackTrace();
        }

        return "fail";
    }

    //模拟支付宝回调流程
    @RequestMapping("sendPaymentResult")
    @ResponseBody
    public String sendPaymentResult(PaymentInfo paymentInfo){
        paymentInfoService.sendQueuePayResult(paymentInfo,"success");
        return "sent payment result";
    }
    //模拟手动查询支付宝支付结果
    @RequestMapping("queryPaymentResult")
    @ResponseBody
    public String checkPaymentResult(String  orderId){
        PaymentInfo paymentInfo = new PaymentInfo();
        paymentInfo.setOrderId(orderId);

        boolean flag = paymentInfoService.checkQueuePayResult(paymentInfo);
        return ""+flag;

    }
}
