package cn.wnn.gmall2.manager.mapper;

import cn.wnn.gmall2.bean.SkuSaleAttrValue;
import cn.wnn.gmall2.bean.SpuSaleAttr;
import tk.mybatis.mapper.common.Mapper;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Administrator on 2018/4/18 0018.
 */
public interface SpuSaleAttrMapper extends Mapper<SpuSaleAttr> {

    public List<SpuSaleAttr> selectSpuSaleAttrList(long spuId);

    List<SpuSaleAttr> selectSpuSaleAttrListCheckBySku(Map map);

    List<SkuSaleAttrValue> getSkuSaleAttrValueListBySpu(String spuId);
}
