package cn.wnn.gmall2.order.service;

import cn.wnn.gmall2.bean.OrderInfo;
import cn.wnn.gmall2.bean.ProcessStatus;

import java.util.List;

/**
 * Created by Administrator on 2018/5/29 0029.
 */
public interface OrderInfoService {
    /*
    * 生成令牌
    * */
    String getTradeCode(String userId);
    /*
    *检查令牌是否有效
    * */
    boolean checkTradeCode(String userId, String reqToken);

    void delTradeCode(String userId);

    void saveOrderInfo(OrderInfo orderInfo);

    OrderInfo getOrderInfoById(String id);

    void updateOrderStatus(String orderId, ProcessStatus paid);
    public String initWareOrder(String orderId);

    void sendWareOrder(String orderId);

    List<OrderInfo> queryExpireOrder();
    public void execExpiredOrder(OrderInfo orderInfo);
}
