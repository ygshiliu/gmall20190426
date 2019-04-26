package cn.wnn.gmall2.manager.service;

import cn.wnn.gmall2.bean.*;
import cn.wnn.gmall2.cache.JedisUtil;
import cn.wnn.gmall2.json.JsonUtil;
import cn.wnn.gmall2.manager.ManageService;
import cn.wnn.gmall2.manager.mapper.*;
import cn.wnn.gmall2.web.Constants;
import com.alibaba.dubbo.config.annotation.Service;
import net.minidev.json.JSONUtil;
import org.springframework.beans.factory.annotation.Autowired;
import redis.clients.jedis.Jedis;
import sun.java2d.pipe.ValidatePipe;
import tk.mybatis.mapper.entity.Example;

import java.util.HashMap;
import java.util.List;

/**
 * Created by Administrator on 2018/4/16 0016.
 */
@Service
public class ManageServiceImpl implements ManageService {
    @Autowired
    BaseCatalog1Mapper baseCatalog1Mapper;
    @Autowired
    BaseCatalog2Mapper baseCatalog2Mapper;
    @Autowired
    BaseCatalog3Mapper baseCatalog3Mapper;
    @Autowired
    BaseAttrInfoMapper baseAttrInfoMapper;
    @Autowired
    BaseAttrValueMapper baseAttrValueMapper;
    @Autowired
    SpuInfoMapper spuInfoMapper;
    @Autowired
    SpuImageMapper spuImageMapper;
    @Autowired
    SpuSaleAttrMapper spuSaleAttrMapper;
    @Autowired
    SpuSaleAttrValueMapper spuSaleAttrValueMapper;
    @Autowired
    BaseSaleAttrMapper baseSaleAttrMapper;
    @Autowired
    SkuInfoMapper skuInfoMapper;
    @Autowired
    SkuAttrValueMapper skuAttrValueMapper;
    @Autowired
    SkuSaleAttrValueMapper skuSaleAttrValueMapper;
    @Autowired
    SkuImageMapper skuImageMapper;

    @Override
    public List<BaseCatalog1> getCatalog1() {
        return baseCatalog1Mapper.selectAll();
    }

    @Override
    public List<BaseCatalog2> getCatalog2(String catalog1Id) {
        BaseCatalog2 catalog2 = new BaseCatalog2();
        catalog2.setCatalog1Id(catalog1Id);

        return baseCatalog2Mapper.select(catalog2);
    }

    @Override
    public List<BaseCatalog3> getCatalog3(String catalog2Id) {
        BaseCatalog3 catalog3 = new BaseCatalog3();
        catalog3.setCatalog2Id(catalog2Id);

        return baseCatalog3Mapper.select(catalog3);
    }

    @Override
    public List<BaseAttrInfo> getAttrList(String catalog3Id) {
        BaseAttrInfo baseAttrInfo = new BaseAttrInfo();
        baseAttrInfo.setCatalog3Id(catalog3Id);

        return baseAttrInfoMapper.select(baseAttrInfo);
    }

    @Override
    public List<BaseAttrInfo> getAttrList(List valueIds) {

        return baseAttrInfoMapper.selectAttrListByValueIds(valueIds);
    }

    @Override
    public void saveAttr(BaseAttrInfo baseAttrInfo) {
        //如果有主键就进行更新，如果没有就插入
        if (baseAttrInfo.getId() != null && baseAttrInfo.getId().length() > 0) {
            baseAttrInfoMapper.updateByPrimaryKey(baseAttrInfo);
        } else {
            //防止主键被赋上一个空字符串
            if (baseAttrInfo.getId().length() == 0) {
                baseAttrInfo.setId(null);
            }
            baseAttrInfoMapper.insertSelective(baseAttrInfo);
        }
        //把原属性值全部清空
        BaseAttrValue baseAttrValue4Del = new BaseAttrValue();
        baseAttrValue4Del.setAttrId(baseAttrInfo.getId());
        baseAttrValueMapper.delete(baseAttrValue4Del);

        //重新插入属性
        if (baseAttrInfo.getAttrValueList() != null && baseAttrInfo.getAttrValueList().size() > 0) {
            for (BaseAttrValue attrValue : baseAttrInfo.getAttrValueList()) {
                //防止主键被赋上一个空字符串
                if (attrValue.getId() != null && attrValue.getId().length() == 0) {
                    attrValue.setId(null);
                }
                attrValue.setAttrId(baseAttrInfo.getId());
                baseAttrValueMapper.insertSelective(attrValue);
            }
        }
    }

    @Override
    public List<BaseAttrValue> getAttrInfo(String attrId) {
        BaseAttrValue baseAttrValue = new BaseAttrValue();
        baseAttrValue.setAttrId(attrId);

        return baseAttrValueMapper.select(baseAttrValue);
    }

    @Override
    public List<SpuInfo> getSpuInfoList(String catalog3Id) {
        SpuInfo spuInfo = new SpuInfo();
        spuInfo.setCatalog3Id(catalog3Id);

        List<SpuInfo> spuInfoList = spuInfoMapper.select(spuInfo);
        return spuInfoList;
    }

    @Override
    public List<BaseSaleAttr> getBaseSaleAttrList() {
        return baseSaleAttrMapper.selectAll();
    }

    @Override
    public void deleteAttr(String id) {
        //先删除 平台属性 对应的 属性值
        BaseAttrValue baseAttrValue = new BaseAttrValue();
        baseAttrValue.setAttrId(id);
        baseAttrValueMapper.delete(baseAttrValue);
        //再删除 平台属性
        baseAttrInfoMapper.deleteByPrimaryKey(id);
    }

    @Override
    public void saveSpuInfo(SpuInfo spuInfo) {
        //保存主表 通过主键存在判断是修改 还是新增
        if (spuInfo.getId() == null || spuInfo.getId().length() == 0) {
            spuInfo.setId(null);
            spuInfoMapper.insertSelective(spuInfo);
        } else {
            spuInfoMapper.updateByPrimaryKey(spuInfo);
        }

        //保存图片信息 先删除 再插入
        Example spuImageExample = new Example(SpuImage.class);
        spuImageExample.createCriteria().andEqualTo("spuId", spuInfo.getId());
        spuImageMapper.deleteByExample(spuImageExample);

        List<SpuImage> spuImageList = spuInfo.getSpuImageList();
        if (spuImageList != null) {
            for (SpuImage spuImage : spuImageList) {
                if (spuImage.getId() != null && spuImage.getId().length() == 0) {
                    spuImage.setId(null);
                }
                spuImage.setSpuId(spuInfo.getId());
                spuImageMapper.insertSelective(spuImage);
            }
        }

        //保存销售属性信息  先删除 再插入
        Example spuSaleAttrExample = new Example(SpuSaleAttr.class);
        spuSaleAttrExample.createCriteria().andEqualTo("spuId", spuInfo.getId());
        spuSaleAttrMapper.deleteByExample(spuSaleAttrExample);

        //保存销售属性值信息  先删除 再插入
        Example spuSaleAttrValueExample = new Example(SpuSaleAttrValue.class);
        spuSaleAttrValueExample.createCriteria().andEqualTo("spuId", spuInfo.getId());
        spuSaleAttrValueMapper.deleteByExample(spuSaleAttrValueExample);

        //保存图片信息 先删除 再插入
        List<SpuSaleAttr> spuSaleAttrList = spuInfo.getSpuSaleAttrList();
        if (spuSaleAttrList != null) {
            for (SpuSaleAttr spuSaleAttr : spuSaleAttrList) {
                if (spuSaleAttr.getId() != null && spuSaleAttr.getId().length() == 0) {
                    spuSaleAttr.setId(null);
                }
                spuSaleAttr.setSpuId(spuInfo.getId());
                spuSaleAttrMapper.insertSelective(spuSaleAttr);
                List<SpuSaleAttrValue> spuSaleAttrValueList = spuSaleAttr.getSpuSaleAttrValueList();
                for (SpuSaleAttrValue spuSaleAttrValue : spuSaleAttrValueList) {
                    if (spuSaleAttrValue.getId() != null && spuSaleAttrValue.getId().length() == 0) {
                        spuSaleAttrValue.setId(null);
                    }
                    spuSaleAttrValue.setSpuId(spuInfo.getId());
                    spuSaleAttrValue.setSaleAttrId(spuSaleAttr.getId());
                    spuSaleAttrValueMapper.insertSelective(spuSaleAttrValue);
                }
            }

        }
    }

    @Override
    public List<SpuImage> getSpuImageList(String spuId) {
        SpuImage spuImage = new SpuImage();
        spuImage.setSpuId(spuId);

        return spuImageMapper.select(spuImage);
    }

    @Override
    public List<SpuSaleAttr> getSpuSaleAttrList(String spuId) {
        return spuSaleAttrMapper.selectSpuSaleAttrList(Long.parseLong(spuId));
    }

    @Override
    public List<BaseAttrInfo> getAttrInfoList(String catalog3Id) {
        return baseAttrInfoMapper.selectAttrList(Long.parseLong(catalog3Id));
    }

    @Override
    public void saveSkuInfo(SkuInfo skuInfo) {

        //如果有主键就进行更新，如果没有就插入
        if (skuInfo.getId() != null && skuInfo.getId().length() > 0) {
            skuInfoMapper.updateByPrimaryKey(skuInfo);
        } else {
            //防止主键被赋上一个空字符串
            if (skuInfo.getId().length() == 0) {
                skuInfo.setId(null);
            }
            skuInfoMapper.insertSelective(skuInfo);
        }

        List<SkuAttrValue> skuAttrValueList = skuInfo.getSkuAttrValueList();
        for (SkuAttrValue skuAttrValue : skuAttrValueList) {
            skuAttrValue.setSkuId(skuInfo.getId());
            skuAttrValueMapper.insertSelective(skuAttrValue);
        }

        List<SkuSaleAttrValue> skuSaleAttrValueList = skuInfo.getSkuSaleAttrValueList();
        for (SkuSaleAttrValue skuSaleAttrValue : skuSaleAttrValueList) {
            skuSaleAttrValue.setSkuId(skuInfo.getId());
            skuSaleAttrValueMapper.insertSelective(skuSaleAttrValue);
        }

        List<SkuImage> skuImageList = skuInfo.getSkuImageList();
        for (SkuImage skuImage : skuImageList) {
            skuImage.setSkuId(skuInfo.getId());
            skuImageMapper.insertSelective(skuImage);
        }


    }

    @Autowired
    JedisUtil jedisUtil;
    @Override
    public SkuInfo getSkuInfoById(String skuid) {
        Jedis jedis = jedisUtil.getJedis();


        //获取分布式锁
        String lock = Constants.SKUKEY_PREFIX+skuid+Constants.SKULOCK_SUFFIX;
        String ok = jedis.set(lock,"ok","nx","px",Constants.SKULOCK_EXPIRE_PX);
        if(!"OK".equals(ok)){
            //未获得分布锁，递归等待，并隔一断时间检查一下
            //避免雪崩，避免高并发时，所有访问都集中到redis上
            try {
                System.out.println("等待redis锁:"+Thread.currentThread().getName());
                Thread.sleep(9000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            getSkuInfoById(skuid);
        }
        System.out.println("redis获得锁===:"+Thread.currentThread().getName());
        String key = Constants.SKUKEY_PREFIX+skuid+Constants.SKUKEY_SUFFIX;
        String value = jedis.get(key);

        SkuInfo skuInfo = null;
        if(value==null || value.length()==0){
            System.out.println("redis未命中***:"+Thread.currentThread().getName());
            skuInfo = getSkuInfoByIdDB(skuid);
            if(skuInfo==null){
                jedis.setex(key,Constants.SKUKEY_TIMEOUT,"empty");
                return null;
            }
            String str = JsonUtil.objectToString(skuInfo);
            jedis.setex(key,Constants.SKUKEY_TIMEOUT,str);
            return skuInfo;
        }

        if("empty".equals(value)){
            return  null;
        }
        System.out.println("redis命中===:"+Thread.currentThread().getName());
        skuInfo = JsonUtil.stringToObject(value, SkuInfo.class);


        return skuInfo;
    }

    @Override
    public List<SpuSaleAttr> selectSpuSaleAttrListCheckBySku(String skuId, String spuId) {
        HashMap<String, Object> map = new HashMap<>();
        map.put("skuId",skuId);
        map.put("spuId",spuId);
        List<SpuSaleAttr> list = spuSaleAttrMapper.selectSpuSaleAttrListCheckBySku(map);
        return list;
    }

    @Override
    public List<SkuSaleAttrValue> getSkuSaleAttrValueListBySpu(String spuId) {
        List<SkuSaleAttrValue> list = spuSaleAttrMapper.getSkuSaleAttrValueListBySpu(spuId);
        return list;
    }

    private SkuInfo getSkuInfoByIdDB(String skuid) {

        System.out.println("查询mysql数据库:"+Thread.currentThread().getName());
//        try {
//            Thread.sleep(3000);
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
        SkuInfo skuInfo = skuInfoMapper.selectByPrimaryKey(skuid);

        SkuImage skuImage = new SkuImage();
        skuImage.setSkuId(skuid);
        List<SkuImage> skuImageList = skuImageMapper.select(skuImage);
        skuInfo.setSkuImageList(skuImageList);

        SkuAttrValue skuAttrValue = new SkuAttrValue();
        skuAttrValue.setSkuId(skuid);
        List<SkuAttrValue> skuAttrValueList = skuAttrValueMapper.select(skuAttrValue);
        skuInfo.setSkuAttrValueList(skuAttrValueList);

        SkuSaleAttrValue skuSaleAttrValue = new SkuSaleAttrValue();
        skuSaleAttrValue.setSkuId(skuid);
        List<SkuSaleAttrValue> skuSaleAttrValueList = skuSaleAttrValueMapper.select(skuSaleAttrValue);
        skuInfo.setSkuSaleAttrValueList(skuSaleAttrValueList);

        return skuInfo;
    }
}
