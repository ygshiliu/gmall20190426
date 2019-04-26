package cn.wnn.gmall2.web;

/**
 * Created by Administrator on 2018/4/11 0011.
 */
public class Constants {

    public static final String IMG_URL="http://192.168.254.130/";
//    redis中sku的key
    public static final String SKUKEY_PREFIX="sku:";
    public static final String SKUKEY_SUFFIX=":info";
    public static final int SKUKEY_TIMEOUT=24*60*60;
    public static final String SKULOCK_SUFFIX=":lock";
    public static final long SKULOCK_EXPIRE_PX=1000;

    // redis中user的key
    public static final String USERKEY_PREFIX="user:";
    public static final String USERKEY_SUFFIX=":info";
    public static final int USERKEY_TIMEOUT= 60*60;

    //cookie
    public static final int COOKIE_MAXAGE= 7*24*3600;

    //认证url
    public static final String USER_VERIFY_URL="http://passport.wnn.com/verify";
    public static final String USER_LOGGIN_URL="http://passport.wnn.com/index";

    //购物车
    public static final String CART_KEY="cartInfo";
    public static final String CART_KEY_PREFIX ="cart:";
    public static final String CART_KEY_SUFFIX =":cartInfo";
    public static final String CART_CHECK_KEY_SUFFIX=":checkcartInfo";


    //用户下单令牌
    public static final String ORDER_TOKEN_SUFFIX=":tradeCode";

    //仓储url
    public static final String GWARE_URL= "http://www.gware.com/hasStock";

}
