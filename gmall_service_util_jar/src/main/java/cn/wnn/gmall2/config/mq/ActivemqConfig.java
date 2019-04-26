package cn.wnn.gmall2.config.mq;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jms.config.DefaultJmsListenerContainerFactory;
import org.springframework.jms.config.JmsListenerContainerFactory;
import org.springframework.jms.listener.MessageListenerContainer;

import javax.jms.Session;

/**
 * Created by Administrator on 2018/6/13 0013.
 */
@Configuration
public class ActivemqConfig {

    @Value("${spring.activemq.brokerURL:disable}")
    String brokerURL;

    @Value("${spring.activemq.listener.enable:disable")
    String listenerEnable;

    @Bean
    public ActivemqUtil activemqUtil(){
        if("disable".equals(brokerURL)){
            return null;
        }
        ActivemqUtil activemqUtil = new ActivemqUtil();
        activemqUtil.init(brokerURL);
        return activemqUtil;
    }
    @Bean(name="jmsQueueListener")
    public DefaultJmsListenerContainerFactory activemqListener(ActiveMQConnectionFactory activeMQConnectionFactory){
        if("disable".equals(listenerEnable)){
            return null;
        }
        //创建ActiveMQConnectionFactory的监听器
        DefaultJmsListenerContainerFactory factory = new DefaultJmsListenerContainerFactory();

        factory.setConnectionFactory(activeMQConnectionFactory);
        //设置并发数
        factory.setConcurrency("5");

        //重连间隔时间
        factory.setRecoveryInterval(5000L);
        //是否启动事务
        factory.setSessionTransacted(false);
        //签收方式
        factory.setSessionAcknowledgeMode(Session.CLIENT_ACKNOWLEDGE);
        return factory;
    }
    @Bean
    public ActiveMQConnectionFactory activeMQConnectionFactory(){
        return new ActiveMQConnectionFactory(brokerURL);
    }

}
