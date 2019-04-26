package cn.wnn.gmall2.usermanager.serviceImpl;

import cn.wnn.gmall2.bean.UserAddress;
import cn.wnn.gmall2.bean.UserInfo;
import cn.wnn.gmall2.cache.JedisUtil;
import cn.wnn.gmall2.usermanager.mapper.UserAddressMapper;
import cn.wnn.gmall2.usermanager.mapper.UserInfoMapper;
import cn.wnn.gmall2.usermanager.service.UserInfoService;
import cn.wnn.gmall2.web.Constants;
import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import redis.clients.jedis.Jedis;

import java.util.List;
import java.util.Map;

/**
 * Created by Administrator on 2018/4/13 0013.
 */
@Service
public class UserInfoServiceImpl implements UserInfoService {
    @Autowired
    UserInfoMapper userInfoMapper;
    @Autowired
    UserAddressMapper userAddressMapper;
    @Autowired
    JedisUtil jedisUtil;

    @Override
    public void addUserInfo(UserInfo userInfo) {
        userInfo.setPasswd(DigestUtils.md5Hex(userInfo.getPasswd()));
        userInfoMapper.insertSelective(userInfo);
    }

    public UserInfo login(UserInfo userInfo){
        //用户登录成功同时，将用户信息放入redis中，并设置过期时间
        userInfo.setPasswd(DigestUtils.md5Hex(userInfo.getPasswd()));
        UserInfo user = userInfoMapper.selectOne(userInfo);
        if(user != null){
            Jedis jedis = jedisUtil.getJedis();
            String key = Constants.USERKEY_PREFIX+user.getId()+Constants.USERKEY_SUFFIX;
            String userstr = JSON.toJSONString(user);
            jedis.setex(key, Constants.USERKEY_TIMEOUT, userstr);
            jedis.close();
            return user;
        }
        return null;
    }

    @Override
    public UserInfo verify(Map<String, Object> map) {
        //根据userid从redis中获取用户信息
        //如果能获取到，则重新定义用户的生命周期
        Object userId = map.get("userId");
        Jedis jedis = jedisUtil.getJedis();
        String key = Constants.USERKEY_PREFIX+userId+Constants.USERKEY_SUFFIX;
        String userstr = jedis.get(key);
        if(userstr!=null){
            jedis.expire(key,Constants.USERKEY_TIMEOUT);
            jedis.close();
            UserInfo userInfo = JSON.parseObject(userstr, UserInfo.class);
            return userInfo;
        }
        jedis.close();
        return null;
    }

    @Override
    public List<UserAddress> getUserAddressList(String userId) {
        UserAddress userAddressQuery = new UserAddress();
        userAddressQuery.setUserId(userId);

        List<UserAddress> userAddressList = userAddressMapper.select(userAddressQuery);

        return userAddressList;
    }

}
