package cn.wnn.gmall2.service.impl;

import cn.wnn.gmall2.bean.CartInfo;
import cn.wnn.gmall2.bean.SkuInfo;
import cn.wnn.gmall2.cache.JedisUtil;
import cn.wnn.gmall2.cart.CartService;
import cn.wnn.gmall2.mapper.CartInfoMapper;
import cn.wnn.gmall2.web.Constants;
import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import org.springframework.beans.factory.annotation.Autowired;
import redis.clients.jedis.Jedis;

import java.util.*;

/**
 * Created by Administrator on 2018/5/21 0021.
 */
@Service
public class CartServiceImpl implements CartService {

    @Autowired
    CartInfoMapper cartInfoMapper;

    @Autowired
    JedisUtil jedisUtil;

    @Override
    public void addToCart(SkuInfo skuInfo, String userId, Integer skuNum) {

        CartInfo cartInfo = new CartInfo();
        cartInfo.setUserId(userId);

        List<CartInfo> list = cartInfoMapper.select(cartInfo);
        boolean flag = false;

        String key = Constants.CART_KEY_PREFIX +userId+Constants.CART_KEY_SUFFIX;

        if(list!=null && list.size()>0){
            for (CartInfo info : list) {
                if(info.getSkuId().equals(skuInfo.getId())){
                    info.setSkuNum(info.getSkuNum()+skuNum);
                    cartInfoMapper.updateByPrimaryKeySelective(info);

                    //设置缓存
                    Jedis jedis = jedisUtil.getJedis();
                    //key  user:id:cartinfo
                    jedis.hset(key,skuInfo.getId(), JSON.toJSONString(info));
                    jedis.close();
                    //
                    flag = true;
                    break;
                }
            }
        }

        if(!flag){
            CartInfo cartInfo1 = new CartInfo();
            cartInfo1.setUserId(userId);
            cartInfo1.setSkuPrice(skuInfo.getPrice());
            cartInfo1.setCartPrice(skuInfo.getPrice());
            cartInfo1.setSkuId(skuInfo.getId());
            cartInfo1.setImgUrl(skuInfo.getSkuDefaultImg());
            cartInfo1.setSkuName(skuInfo.getSkuName());
            cartInfo1.setSkuNum(skuNum);

            cartInfoMapper.insert(cartInfo1);

            Jedis jedis = jedisUtil.getJedis();
            jedis.hset(key,skuInfo.getId(),JSON.toJSONString(cartInfo1));
            Long ttl = jedis.ttl(Constants.USERKEY_PREFIX + userId + Constants.USERKEY_SUFFIX);
            jedis.expire(key,ttl.intValue());
            jedis.close();
        }



    }



    @Override
    public void mergeCart(List<CartInfo> list, String userId) {
        CartInfo cartInfo = new CartInfo();
        cartInfo.setUserId(userId);
        List<CartInfo> cartListDB = cartInfoMapper.select(cartInfo);

        for (CartInfo info : list) {
            //遍历cookie中的购物车列表，如果在数据库中有，则更新数量，
            // 这里仅更新数据库并没有同步到redis中，后面统一加载到redis中
            boolean flag = false;
            for (CartInfo cartInfo1 : cartListDB) {
                if(cartInfo1.getSkuId().equals(info.getSkuId())){
                    cartInfo1.setSkuNum(cartInfo1.getSkuNum()+info.getSkuNum());
                    cartInfoMapper.updateByPrimaryKeySelective(cartInfo1);
                    flag=true;
                    break;
                }

            }

            if(!flag){
                info.setUserId(userId);
                cartInfoMapper.insert(info);
            }
        }
        //重新更新一下redis中的购物列表
        loadCartList(userId);
    }

    //提供订单模块使用，获取redis中选中的购物商品
    public List<CartInfo>  getCheckCartList(String userId){
        String checkKey = Constants.CART_KEY_PREFIX +userId+Constants.CART_CHECK_KEY_SUFFIX;
        Jedis jedis = jedisUtil.getJedis();
        List<String> strList = jedis.hvals(checkKey);
        ArrayList<CartInfo> cartInfos = new ArrayList<>(strList.size());
        for (String str : strList) {
            CartInfo cartInfo = JSON.parseObject(str, CartInfo.class);
            cartInfos.add(cartInfo);
        }

        jedis.close();
        return cartInfos;
    }


    @Override
    public List<CartInfo> getCartList(String userId) {
        List<CartInfo> cartInfos = null;
        //从redis中查出当前用户的购物列表
        Jedis jedis = jedisUtil.getJedis();
        String key = Constants.CART_KEY_PREFIX +userId+Constants.CART_KEY_SUFFIX;
        List<String> cartstr = jedis.hvals(key);
        if(cartstr!=null && cartstr.size()>0){
            //redis中有
            //redis hash --> java list
            //用 id 排序  倒排
            cartInfos = new ArrayList<>(cartstr.size());
            for (String s : cartstr) {
                System.out.println("=====redis hash ====="+s);
                CartInfo cartInfo = JSON.parseObject(s, CartInfo.class);
                cartInfos.add(cartInfo);
            }
            //排序
            cartInfos.sort(new Comparator<CartInfo>() {
                @Override
                public int compare(CartInfo o1, CartInfo o2) {
                    return Integer.compare(Integer.valueOf(o2.getId()),Integer.valueOf(o1.getId()));
                }
            });

        }else{
            cartInfos = loadCartList(userId);
        }

        return cartInfos;
    }

    @Override
    public void checkCart(String userId,String skuId, String isChecked) {
        //设置redis中购物商品的状态，1为选中，0未选中
        Jedis jedis = jedisUtil.getJedis();
        String key = Constants.CART_KEY_PREFIX +userId+Constants.CART_KEY_SUFFIX;
        String str = jedis.hget(key, skuId);
        if(str!=null && str.length()>0){
            CartInfo cartInfo = JSON.parseObject(str, CartInfo.class);
            cartInfo.setIsChecked(isChecked);
            String s = JSON.toJSONString(cartInfo);
            jedis.hset(key,skuId,s);

            //如果选中则加入选中的redis中，否则从选中的redis中移除
            String checkKey = Constants.CART_KEY_PREFIX +userId+Constants.CART_CHECK_KEY_SUFFIX;
            if("1".equals(isChecked)){
                jedis.hset(checkKey,skuId,s);
            }else{
                jedis.hdel(checkKey,skuId);
            }
            jedis.close();
        }

    }

    @Override
    public void  mergeCartandupdateCacheStatus(List<CartInfo> cartInfos, String userId) {
        CartInfo cartInfo = new CartInfo();
        cartInfo.setUserId(userId);
        List<CartInfo> cartListDB = cartInfoMapper.select(cartInfo);

        Jedis jedis = jedisUtil.getJedis();
        String key = Constants.CART_KEY_PREFIX +userId+Constants.CART_KEY_SUFFIX;
        String checkKey = Constants.CART_KEY_PREFIX +userId+Constants.CART_CHECK_KEY_SUFFIX;

        for (CartInfo info : cartInfos) {
            //遍历cookie中的购物车列表，如果在数据库中有，则更新数量，
            // 这里仅更新数据库并没有同步到redis中，后面统一加载到redis中
            boolean flag = false;
            for (CartInfo cartInfo1 : cartListDB) {
                if(cartInfo1.getSkuId().equals(info.getSkuId())){
                    cartInfo1.setSkuNum(cartInfo1.getSkuNum()+info.getSkuNum());
                    cartInfoMapper.updateByPrimaryKeySelective(cartInfo1);

                    //更新redis
                    String s = JSON.toJSONString(cartInfo1);
                    jedis.hset(key, cartInfo1.getSkuId(),s );
                    if(info.getIsChecked().equals("1")){
                        jedis.hset(checkKey,info.getSkuId(),s);
                    }
                    flag=true;
                    break;
                }

            }

            if(!flag){
                info.setUserId(userId);
                cartInfoMapper.insert(info);

                //更新redis
                String s = JSON.toJSONString(info);
                jedis.hset(key, info.getSkuId(),s );
                if(info.getIsChecked().equals("1")){
                    jedis.hset(checkKey,info.getSkuId(),s);
                }
            }
        }
    }

    @Override
    public List<CartInfo> loadCartList(String userId) {
        List<CartInfo> cartInfos = cartInfoMapper.getCartWithPrice(userId);
        if(cartInfos!=null && cartInfos.size()>0) {
            Jedis jedis = jedisUtil.getJedis();
            String key = Constants.CART_KEY_PREFIX + userId + Constants.CART_KEY_SUFFIX;

            Map<String, String> map = new HashMap<>();
            for (CartInfo cartInfo : cartInfos) {
                String str = JSON.toJSONString(cartInfo);
                map.put(cartInfo.getSkuId(), str);

            }
            jedis.hmset(key, map);
            jedis.close();
        }
        return cartInfos;
    }
}
