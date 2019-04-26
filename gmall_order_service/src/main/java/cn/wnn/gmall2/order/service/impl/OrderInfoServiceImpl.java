package cn.wnn.gmall2.order.service.impl;

import cn.wnn.gmall2.bean.OrderDetail;
import cn.wnn.gmall2.bean.OrderInfo;
import cn.wnn.gmall2.bean.OrderStatus;
import cn.wnn.gmall2.bean.ProcessStatus;
import cn.wnn.gmall2.cache.JedisUtil;
import cn.wnn.gmall2.config.mq.ActivemqUtil;
import cn.wnn.gmall2.order.mapper.OrderDetailMapper;
import cn.wnn.gmall2.order.mapper.OrderInfoMapper;
import cn.wnn.gmall2.order.service.OrderInfoService;
import cn.wnn.gmall2.payment.service.PaymentInfoService;
import cn.wnn.gmall2.web.Constants;
import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import org.apache.activemq.command.ActiveMQTextMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import redis.clients.jedis.Jedis;
import tk.mybatis.mapper.entity.Example;

import javax.jms.*;
import javax.jms.Queue;
import java.util.*;

/**
 * Created by Administrator on 2018/5/29 0029.
 */
@Service
@org.springframework.stereotype.Service
public class OrderInfoServiceImpl implements OrderInfoService {
    @Autowired
    JedisUtil jedisUtil;
    @Autowired
    OrderInfoMapper orderInfoMapper;
    @Autowired
    OrderDetailMapper orderDetailMapper;

    @Autowired
    ActivemqUtil activemqUtil;

    @Reference
    PaymentInfoService paymentInfoService;
    @Override
    public String getTradeCode(String userId) {
        Jedis jedis = jedisUtil.getJedis();
        String key = Constants.USERKEY_PREFIX+userId+Constants.ORDER_TOKEN_SUFFIX;
        String tradecode = UUID.randomUUID().toString().replace("-", "");
        jedis.setex(key,60*60,tradecode);
        jedis.close();
        return tradecode;
    }

    @Override
    public boolean checkTradeCode(String userId, String reqToken) {
        Jedis jedis = jedisUtil.getJedis();
        String key = Constants.USERKEY_PREFIX+userId+Constants.ORDER_TOKEN_SUFFIX;
        String token = jedis.get(key);
        if(reqToken.equals(token)){
            return true;
        }
        return false;
    }

    @Override
    public void delTradeCode(String userId) {
        Jedis jedis = jedisUtil.getJedis();
        String key = Constants.USERKEY_PREFIX+userId+Constants.ORDER_TOKEN_SUFFIX;
        jedis.del(key);
        jedis.close();
    }

    @Override
    public void saveOrderInfo(OrderInfo orderInfo) {
        orderInfoMapper.insertSelective(orderInfo);
        List<OrderDetail> orderDetailList = orderInfo.getOrderDetailList();
        for (OrderDetail orderDetail : orderDetailList) {
            orderDetail.setOrderId(orderInfo.getId());
            orderDetailMapper.insertSelective(orderDetail);
        }

    }
    //根据订单号查询订单，避免网络之间的传输导致数据被篡改情况
    @Override
    public OrderInfo getOrderInfoById(String id) {

        OrderInfo orderInfo = orderInfoMapper.selectByPrimaryKey(id);

        OrderDetail orderDetail = new OrderDetail();
        orderDetail.setOrderId(id);
        List<OrderDetail> list = orderDetailMapper.select(orderDetail);

        orderInfo.setOrderDetailList(list);

        return orderInfo;
    }

    @Override
    public void updateOrderStatus(String orderId, ProcessStatus paid) {
        OrderInfo orderInfo = new OrderInfo();
        orderInfo.setId(orderId);
        orderInfo.setProcessStatus(paid);
        orderInfo.setOrderStatus(paid.getOrderStatus());
        orderInfoMapper.updateByPrimaryKeySelective(orderInfo);
    }



    public String initWareOrder(String orderId){
        OrderInfo orderInfo = getOrderInfoById(orderId);

        HashMap<String, Object> map = new HashMap<>();
        map.put("orderId",orderId);
        map.put("consignee",orderInfo.getConsignee());
        map.put("consigneeTel",orderInfo.getConsigneeTel());
        map.put("orderComment",orderInfo.getOrderComment());
        map.put("orderBody",orderInfo.getTradeBody());
        map.put("deliveryAddress",orderInfo.getDeliveryAddress());
        map.put("paymentWay",2);

        List list = new ArrayList();
        List<OrderDetail> orderDetailList = orderInfo.getOrderDetailList();
        for (OrderDetail orderDetail : orderDetailList) {
            Integer skuNum = orderDetail.getSkuNum();
            String skuName = orderDetail.getSkuName();
            String skuId = orderDetail.getSkuId();
            HashMap<String, String> map1 = new HashMap<>();
            map1.put("skuId",skuId);
            map1.put("skuName",skuName);
            map1.put("skuNum",skuNum+"");
            list.add(map);
        }
        map.put("details",list);

        String str = JSON.toJSONString(map);
        return str;
    }

    @Override
    public void sendWareOrder(String orderId) {
        //根据库存模块的接口，准备需要的参数
        //准备库存模块接口的json数据
        String wareOrderStr = initWareOrder(orderId);
        //使用消息队列通知库存模块
        Connection conection = activemqUtil.getConection();
        try {
            conection.start();
            Session session = conection.createSession(true, Session.SESSION_TRANSACTED);
            Queue queue = session.createQueue("ORDER_RESULT_QUEUE");
            MessageProducer producer = session.createProducer(queue);

            ActiveMQTextMessage message = new ActiveMQTextMessage();
            message.setText(wareOrderStr);

            producer.send(message );

            session.commit();

            session.close();
            producer.close();
            conection.close();

       } catch (JMSException e) {
            e.printStackTrace();
        }

    }

    @Override
    public List<OrderInfo> queryExpireOrder() {
        //查询过期(比现在时间小的） 且 没有付款 的订单
        Example example = new Example(OrderInfo.class);
        example.createCriteria().andLessThan("expireTime",new Date()).andEqualTo("processStatus", ProcessStatus.UNPAID);
        List<OrderInfo> orderInfos = orderInfoMapper.selectByExample(example);
        return orderInfos;
    }

    @Async
    public void execExpiredOrder(OrderInfo orderInfo){
        updateOrderStatus(orderInfo.getId(), ProcessStatus.CLOSED);
        //TODO:待定 此处需要释放库存商品数量
        paymentInfoService.closePayment(orderInfo.getId());
    }
}
