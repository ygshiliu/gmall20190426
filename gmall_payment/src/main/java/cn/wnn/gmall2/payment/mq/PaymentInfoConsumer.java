package cn.wnn.gmall2.payment.mq;

import cn.wnn.gmall2.bean.PaymentInfo;
import cn.wnn.gmall2.payment.service.PaymentInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

import javax.jms.JMSException;
import javax.jms.MapMessage;

/**
 * Created by Administrator on 2018/6/15 0015.
 */
@Component
public class PaymentInfoConsumer {
    @Autowired
    PaymentInfoService paymentInfoService;
    //destination:监听哪个消息队列
    //containerFactory:对应哪个监听器
    //注意：方法参数要与发布数据的类型匹配
    @JmsListener(destination = "PAY_RESULT_CHECK_QUEUE",containerFactory = "jmsQueueListener")
    public void consumerPayResult(MapMessage map){
        try {
            //获取队列中的信息
            String outTradeNo = map.getString("outTradeNo");
            int checkCount = map.getInt("checkCount");
            int delaySce = map.getInt("delaySce");

            //检查订单状态，如果没已支付，或通知次数不等于0，继续发送消息队列
            PaymentInfo paymentInfo = new PaymentInfo();
            paymentInfo.setOutTradeNo(outTradeNo);
            boolean flag = paymentInfoService.checkQueuePayResult(paymentInfo);
            if(!flag && checkCount!=0){
                paymentInfoService.sendDelayQueuePayResult(outTradeNo,delaySce,checkCount-1);
            }

        } catch (JMSException e) {
            e.printStackTrace();
        }


        
    }


}
