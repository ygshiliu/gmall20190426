package cn.wnn.gmall2.order.task;

import cn.wnn.gmall2.bean.OrderInfo;
import cn.wnn.gmall2.order.service.OrderInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Created by Administrator on 2018/6/27 0027.
 */
@EnableScheduling
public class OrderTask {

    @Autowired
    OrderInfoService orderInfoService;

    @Scheduled(cron = "0/15 * * * * ?")
    public void updateOrderStatus(){
        //获取所有的过期订单
        List<OrderInfo> list = orderInfoService.queryExpireOrder();
        //遍历处理订单状态及支付信息状态，为close
        for (OrderInfo orderInfo : list) {
            //修改订单状态与支付信息状态
            orderInfoService.execExpiredOrder(orderInfo);
        }
    }

}
