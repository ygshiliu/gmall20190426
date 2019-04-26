package cn.wnn.gmall2.payment.service.impl;

import cn.wnn.gmall2.bean.PaymentInfo;
import cn.wnn.gmall2.bean.PaymentStatus;
import cn.wnn.gmall2.config.mq.ActivemqUtil;
import cn.wnn.gmall2.payment.mapper.PaymentInfoMapper;
import cn.wnn.gmall2.payment.service.PaymentInfoService;
import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.request.AlipayTradeQueryRequest;
import com.alipay.api.response.AlipayTradeQueryResponse;
import org.apache.activemq.ScheduledMessage;
import org.apache.activemq.command.ActiveMQMapMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tk.mybatis.mapper.entity.Example;

import javax.jms.*;
import java.util.List;

/**
 * Created by Administrator on 2018/6/12 0012.
 */
@Service
@com.alibaba.dubbo.config.annotation.Service
public class PaymentInfoServiceImpl implements PaymentInfoService {

    @Autowired
    PaymentInfoMapper paymentInfoMapper;
    @Autowired
    ActivemqUtil activemqUtil;
    @Autowired
    AlipayClient alipayClient;
    @Override
    public void savePaymentInfo(PaymentInfo paymentInfo) {
        PaymentInfo paymentInfoQuery = new PaymentInfo();
        paymentInfoQuery.setOrderId(paymentInfo.getOrderId());
        List<PaymentInfo> list = paymentInfoMapper.select(paymentInfoQuery);
        if(list!=null && list.size()>0){
            return ;
        }

        paymentInfoMapper.insertSelective(paymentInfo);
    }

    @Override
    public PaymentInfo getPaymentInfo(PaymentInfo paymentInfo) {
        return paymentInfoMapper.selectOne(paymentInfo);
    }

    @Override
    public void updatePaymentInfoStatus(PaymentInfo paymentInfoDB) {
        paymentInfoMapper.updateByPrimaryKeySelective(paymentInfoDB);
    }

    @Override
    public void sendQueuePayResult(PaymentInfo paymentInfoDB, String sresult) {
        Connection conection = activemqUtil.getConection();
        try {
            conection.start();

            Session session = conection.createSession(true, Session.SESSION_TRANSACTED);
            Queue payQueue = session.createQueue("PAY_RESULT_QUEUE");
            MessageProducer producer = session.createProducer(payQueue);

            MapMessage map = new ActiveMQMapMessage();
            map.setString("orderId",paymentInfoDB.getOrderId());
            map.setString("result",sresult);

            producer.send(map);

            session.close();
            conection.close();

        } catch (JMSException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void sendDelayQueuePayResult(String outTradeNo,int delaySce,int checkCount) {
        Connection conection = activemqUtil.getConection();
        try {
            conection.start();

            Session session = conection.createSession(true, Session.SESSION_TRANSACTED);
            Queue payQueue = session.createQueue("PAY_RESULT_CHECK_QUEUE");
            MessageProducer producer = session.createProducer(payQueue);

            MapMessage map = new ActiveMQMapMessage();
            map.setString("outTradeNo",outTradeNo);
            map.setInt("checkCount",checkCount);
            map.setInt("delaySce",delaySce);
            //设置延迟消费的时间
            map.setLongProperty(ScheduledMessage.AMQ_SCHEDULED_DELAY,delaySce*1000);

            producer.send(map);

            session.close();
            conection.close();

        } catch (JMSException e) {
            e.printStackTrace();
        }

    }


    @Override
    public void closePayment(String orderId){
        Example example=new Example(PaymentInfo.class);
        example.createCriteria().andEqualTo("orderId",orderId);
        PaymentInfo paymentInfo=new PaymentInfo();
        paymentInfo.setPaymentStatus(PaymentStatus.ClOSED);
        paymentInfoMapper.updateByExampleSelective(paymentInfo,example);

    }

    @Override
    public boolean checkQueuePayResult(PaymentInfo paymentInfo) {
        //1.查询此订单，检查订单状态
        PaymentInfo paymentInfoDB = paymentInfoMapper.selectOne(paymentInfo);
        //2.如果已经支付或关闭状态，则返回true,不再做通知订单处理
        PaymentStatus paymentStatus = paymentInfoDB.getPaymentStatus();
        if(PaymentStatus.PAID.equals(paymentStatus)||PaymentStatus.ClOSED.equals(paymentStatus)){
            return true;
        }
        //3.否则查询支付宝接口，自动获取支付状态
        AlipayTradeQueryRequest request = new AlipayTradeQueryRequest();
        request.setBizContent("{" +
                "\"out_trade_no\":\""+paymentInfoDB.getOutTradeNo()+"\" " +
                "  }");
        AlipayTradeQueryResponse response = null;
        try {
            response = alipayClient.execute(request);
        } catch (AlipayApiException e) {
            e.printStackTrace();
        }
        //4.如果查询成功，还要查看响应状态中支付的状态
        // (如果状态为TRADE_FINISHED或TRADE_SUCCESS，还需要修改订单状态,并通知订单系统）
        //5.如果查询失败返回，false
        if(response.isSuccess()){
            System.out.println("调用成功");
            String tradeStatus = response.getTradeStatus();
            if("TRADE_SUCCESS".equals(tradeStatus)||"TRADE_FINISHED".equals(tradeStatus)){
                //修改订单状态
                paymentInfoDB.setPaymentStatus(PaymentStatus.PAID);
                updatePaymentInfoStatus(paymentInfoDB);
                //给订单系统发送支付成功通知
                sendQueuePayResult(paymentInfoDB,"success");
                return true;
            }
        } else {
            System.out.println("调用失败");
            return false;
        }

        return false;
    }
}
