package cn.wnn.gmall2.list;

import cn.wnn.gmall2.beanes.SkuInfoEs;
import cn.wnn.gmall2.beanes.SkuParamsEs;
import cn.wnn.gmall2.beanes.SkuResultEs;

/**
 * Created by Administrator on 2018/5/4 0004.
 */
public interface EsService {
    public void saveSkuInfoEs(SkuInfoEs skuInfoEs);
    public SkuResultEs searchSkuInfoList(SkuParamsEs skuLsParam);
}
