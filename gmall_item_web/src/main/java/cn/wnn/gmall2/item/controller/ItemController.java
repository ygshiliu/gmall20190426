package cn.wnn.gmall2.item.controller;

import cn.wnn.gmall2.annotation.LogginRequire;
import cn.wnn.gmall2.bean.SkuInfo;
import cn.wnn.gmall2.bean.SkuSaleAttrValue;
import cn.wnn.gmall2.bean.SpuSaleAttr;
import cn.wnn.gmall2.manager.ManageService;
import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import org.json.JSONObject;
import org.json.JSONString;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Administrator on 2018/4/28 0028.
 */
@Controller
public class ItemController {

    @Reference
    ManageService manageService;


    @RequestMapping("{skuid}.html")
    public String getSkuInfo(@PathVariable("skuid") String skuid, Model model){
        //获取skuInfo信息（4，7，8）
        SkuInfo skuInfo = manageService.getSkuInfoById(skuid);
        //获取当前sku对应的spu下的所有销售属性及属性值
        List<SpuSaleAttr> SpuSaleAttrListCheckBySku = manageService.selectSpuSaleAttrListCheckBySku(skuInfo.getId(),skuInfo.getSpuId());
        //准备切换兄弟商品的json串：skusaleattrvalueid|skusaleattrvalueid|skusaleattrvalueid:skuinfoId
        //24|44|55:7
        List<SkuSaleAttrValue> skuSaleAttrValueList = manageService.getSkuSaleAttrValueListBySpu(skuInfo.getSpuId());
        String valueIdSkuJson = getSiblingSkuJson(skuSaleAttrValueList);

        //将上面准备的数据设置到域中，交给页面
        model.addAttribute("skuInfo",skuInfo);
        model.addAttribute("saleAttrList",SpuSaleAttrListCheckBySku);
        model.addAttribute("valueIdSkuJson",valueIdSkuJson);

        return "item";
    }

    private String getSiblingSkuJson(List<SkuSaleAttrValue> list){
        Map<String, String> map = new HashMap<String, String>();

        String key="";
        for (int i=0;i< list.size();i++){
            if(!"".equals(key) && key.length()!= 0){
                key+="|";
            }

            SkuSaleAttrValue v = list.get(i);
            key+=v.getSaleAttrValueId();

            if((i+1)==list.size() || !list.get(i+1).getSkuId().equals(v.getSkuId())){
                map.put(key,v.getSkuId());
                key="";
            }
        }

        String valueIdSkuJson = JSON.toJSONString(map);
        return valueIdSkuJson;
    }
}
