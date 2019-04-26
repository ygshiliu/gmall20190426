package cn.wnn.gmall2.config.mq;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.pool.PooledConnectionFactory;

import javax.jms.Connection;
import javax.jms.JMSException;

/**
 * Created by Administrator on 2018/6/13 0013.
 */
public class ActivemqUtil {

    private PooledConnectionFactory pooledConnectionFactory;

    public void init(String url){
        ActiveMQConnectionFactory activeMQConnectionFactory = new ActiveMQConnectionFactory(url);
        pooledConnectionFactory = new PooledConnectionFactory(activeMQConnectionFactory);
        //超时时间
        pooledConnectionFactory.setExpiryTimeout(2000);
        // 最大连接数
        pooledConnectionFactory.setMaxConnections(30);
        //服务宕机，重连接
        pooledConnectionFactory.setReconnectOnException(true);
        //最大活动的连接数
        pooledConnectionFactory.setMaximumActiveSessionPerConnection(10);
    }

    public Connection getConection(){
        Connection connection = null;
        try {
            connection = pooledConnectionFactory.createConnection();
        } catch (JMSException e) {
            e.printStackTrace();
        }
        return connection;
    }
}
