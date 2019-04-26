package cn.wnn.gmall2.cart;

import cn.wnn.gmall2.bean.CartInfo;
import cn.wnn.gmall2.bean.SkuInfo;

import java.util.List;

/**
 * Created by Administrator on 2018/5/21 0021.
 */
public interface CartService {
    public void addToCart(SkuInfo skuInfo, String skuId, Integer skuNum);

    public void mergeCart(List<CartInfo> list, String userId);

    public List<CartInfo> loadCartList(String userId);

    public List<CartInfo> getCartList(String userId);

    void checkCart(String userId,String skuId, String isChecked);

    void mergeCartandupdateCacheStatus(List<CartInfo> cartInfos, String userId);
    /*
    * 提供给订单系统使用，获取redis中选中的商品
    * */
    public List<CartInfo>  getCheckCartList(String userId);
}
