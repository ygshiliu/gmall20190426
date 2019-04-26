package cn.wnn.gmall2.cache;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Created by Administrator on 2018/5/1 0001.
 */
@Configuration
public class JedisConfig {

    @Value("${spring.redis.host:disabled}")
    private String host;

    @Value("${spring.redis.port:0}")
    private int port;

    @Value("${spring.redis.database:0}")
    private int database;


    @Bean
    public JedisUtil jedisPool(){
        if("disabled".equals(host)){
            return null;
        }
        JedisUtil jedisUtil = new JedisUtil();
        jedisUtil.initpool(host,port);
        return jedisUtil;
    }
}
