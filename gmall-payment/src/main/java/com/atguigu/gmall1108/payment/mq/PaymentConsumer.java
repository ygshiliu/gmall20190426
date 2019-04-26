package com.atguigu.gmall1108.payment.mq;

import com.alibaba.dubbo.config.annotation.Reference;
import com.atguigu.gmall.service.PaymentService;
import com.atguigu.gmall1108.bean.PaymentInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

import javax.jms.JMSException;
import javax.jms.MapMessage;

/**
 * @param
 * @return
 */

@Component
public class PaymentConsumer{

    @Autowired
    PaymentService paymentService;

    @JmsListener(destination = "PAYMENT_RESULT_CHECK_QUEUE",containerFactory = "jmsQueueListener")
    public  void consumePaymentResultCheck(MapMessage mapMessage) throws JMSException {
        String outTradeNo = mapMessage.getString("outTradeNo");
        int delaySec = mapMessage.getInt("delaySec");
        int checkCount = mapMessage.getInt("checkCount");

        PaymentInfo paymentInfo=new PaymentInfo();
        paymentInfo.setOutTradeNo(outTradeNo);
        System.out.println(" 开始检查支付结果");
        boolean checkPaymentResult = paymentService.checkPayment(paymentInfo);
        System.out.println("  支付结果:"+checkPaymentResult);
        if(!checkPaymentResult&&checkCount!=0){
            System.out.println( "checkCount:"+checkCount  +"  再次发送延迟队列");
            paymentService.sendDelayPaymentResultCheck(outTradeNo,delaySec,checkCount-1);
        }

    }

}