package cn.wnn.gmall2.cache;

import org.springframework.context.annotation.Configuration;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

/**
 * Created by Administrator on 2018/5/1 0001.
 */

public class JedisUtil {
    private JedisPool jedisPool = null;

    public void initpool(String host,int port){
        JedisPoolConfig jedisPoolConfig = new JedisPoolConfig();
        jedisPoolConfig.setMaxTotal(200);
        jedisPoolConfig.setMinIdle(10);
        jedisPoolConfig.setMaxIdle(20);
        jedisPoolConfig.setBlockWhenExhausted(true);
        jedisPoolConfig.setMaxWaitMillis(1000);
        jedisPoolConfig.setTestOnBorrow(true);

         jedisPool = new JedisPool(jedisPoolConfig,host,port,6000 );

    }

    public Jedis getJedis(){
        return jedisPool.getResource();
    }
}
