package cn.wnn.gmall2.manager;

import cn.wnn.gmall2.bean.*;

import java.util.List;

/**
 * Created by Administrator on 2018/4/16 0016.
 */
public interface ManageService {
    public List<BaseCatalog1> getCatalog1();

    public List<BaseCatalog2> getCatalog2(String catalog1Id);

    public List<BaseCatalog3> getCatalog3(String catalog2Id);

    public List<BaseAttrInfo> getAttrList(String catalog3Id);

    public List<BaseAttrInfo> getAttrList(List valueIds);

    public void saveAttr(BaseAttrInfo baseAttrInfo);

    public List<BaseAttrValue> getAttrInfo(String attrId);

    public List<SpuInfo> getSpuInfoList(String catalog3Id);

    public List<BaseSaleAttr> getBaseSaleAttrList();

    void deleteAttr(String id);

    void saveSpuInfo(SpuInfo spuInfo);

    List<SpuImage> getSpuImageList(String spuId);

    List<SpuSaleAttr> getSpuSaleAttrList(String spuId);

    List<BaseAttrInfo> getAttrInfoList(String catalog3Id);

    void saveSkuInfo(SkuInfo skuInfo);

    SkuInfo getSkuInfoById(String skuid);

    List<SpuSaleAttr> selectSpuSaleAttrListCheckBySku(String skuId, String spuId);

    List<SkuSaleAttrValue> getSkuSaleAttrValueListBySpu(String spuId);
}
