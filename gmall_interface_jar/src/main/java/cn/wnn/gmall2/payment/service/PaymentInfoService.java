package cn.wnn.gmall2.payment.service;

import cn.wnn.gmall2.bean.PaymentInfo;

/**
 * Created by Administrator on 2018/6/12 0012.
 */
public interface PaymentInfoService {

    void savePaymentInfo(PaymentInfo paymentInfo);

    PaymentInfo getPaymentInfo(PaymentInfo paymentInfo);

    void updatePaymentInfoStatus(PaymentInfo paymentInfoDB);

    public void sendQueuePayResult(PaymentInfo paymentInfoDB, String sresult);

    boolean checkQueuePayResult(PaymentInfo paymentInfo);
    public void sendDelayQueuePayResult(String outTradeNo, int delaySce, int checkCount);

    void closePayment(String orderId);
}
