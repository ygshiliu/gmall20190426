package cn.wnn.gmall2.mapper;

import cn.wnn.gmall2.bean.CartInfo;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;

/**
 * Created by Administrator on 2018/5/21 0021.
 */
public interface CartInfoMapper extends Mapper<CartInfo> {
    List<CartInfo> getCartWithPrice(String userId);
}
