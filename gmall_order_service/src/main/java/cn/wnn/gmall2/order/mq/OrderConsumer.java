package cn.wnn.gmall2.order.mq;

import cn.wnn.gmall2.bean.ProcessStatus;
import cn.wnn.gmall2.order.service.OrderInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

import javax.jms.JMSException;
import javax.jms.MapMessage;

/**
 * Created by Administrator on 2018/6/15 0015.
 */
@Component
public class OrderConsumer {
    @Autowired
    OrderInfoService orderInfoService;
    //destination:监听哪个消息队列
    //containerFactory:对应哪个监听器
    //注意：方法参数要与发布数据的类型匹配
    @JmsListener(destination = "PAY_RESULT_QUEUE",containerFactory = "jmsQueueListener")
    public void consumerPayResult(MapMessage map){
        try {
            String orderId = map.getString("orderId");
            String result = map.getString("result");
            //如果订单支付成功，修改订单状态，同时通知库存模块发货
            if("success".equals(result)){
                //修改订单状态ProcessStatus.PAID
                orderInfoService.updateOrderStatus(orderId, ProcessStatus.PAID);
                //通知库存系统减库存
                orderInfoService.sendWareOrder(orderId);
                //通知库存模块后，再修改订单状态 ProcessStatus.WAITING_DELEVER
                orderInfoService.updateOrderStatus(orderId, ProcessStatus.NOTIFIED_WARE);
            }
        } catch (JMSException e) {
            e.printStackTrace();
        }

    }

    @JmsListener(destination = "SKU_DEDUCT_QUEUE",containerFactory = "jmsQueueListener")
    public void consumerWareResult(MapMessage map){
        try {
            String orderId = map.getString("orderId");
            String status = map.getString("status");
            if("DEDUCTED".equals(status)){
                //修改订单 状态 WAITING_DELEVER待发货
                orderInfoService.updateOrderStatus(orderId, ProcessStatus.WAITING_DELEVER);
            }else{
                //修改订单状态STOCK_EXCEPTION
                orderInfoService.updateOrderStatus(orderId, ProcessStatus.STOCK_EXCEPTION);
                //同时通知客服模块
            }
        } catch (JMSException e) {
            e.printStackTrace();
        }

    }
}
