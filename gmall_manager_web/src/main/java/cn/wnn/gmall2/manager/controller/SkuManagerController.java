package cn.wnn.gmall2.manager.controller;

import cn.wnn.gmall2.bean.SkuAttrValue;
import cn.wnn.gmall2.bean.SkuInfo;
import cn.wnn.gmall2.bean.SpuImage;
import cn.wnn.gmall2.beanes.SkuAttrValueEs;
import cn.wnn.gmall2.beanes.SkuInfoEs;
import cn.wnn.gmall2.list.EsService;
import cn.wnn.gmall2.manager.ManageService;
import com.alibaba.dubbo.config.annotation.Reference;
import org.apache.catalina.manager.ManagerServlet;
import org.apache.commons.beanutils.BeanUtils;
import org.springframework.web.bind.annotation.*;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by Administrator on 2018/4/25 0025.
 */
@RestController
public class SkuManagerController {
    @Reference
    ManageService manageService;
    @Reference
    EsService esService;

    @RequestMapping(value = "saveSkuInfo",method = RequestMethod.POST)
    @ResponseBody
    public void saveSkuInfo(SkuInfo skuInfo){
        manageService.saveSkuInfo(skuInfo);
    }

    //上架
    @RequestMapping(value = "onSale",method = RequestMethod.GET)
    @ResponseBody
    public void onSale(String skuId){
        SkuInfo skuInfo = manageService.getSkuInfoById(skuId);
        SkuInfoEs skuInfoEs = new SkuInfoEs();

        try {
            BeanUtils.copyProperties(skuInfoEs,skuInfo);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }

        //补全平台属性值
        ArrayList<SkuAttrValueEs> skuAttrValueEslist = new ArrayList<>();
        List<SkuAttrValue> skuAttrValueList = skuInfo.getSkuAttrValueList();
        for (SkuAttrValue skuAttrValue : skuAttrValueList) {
            SkuAttrValueEs skuAttrValueEs = new SkuAttrValueEs();
            skuAttrValueEs.setValueId(skuAttrValue.getValueId());
            skuAttrValueEslist.add(skuAttrValueEs);
        }
        skuInfoEs.setSkuAttrValueEsList(skuAttrValueEslist);

        //此操作生产中使用异步
        esService.saveSkuInfoEs(skuInfoEs);
    }
}
