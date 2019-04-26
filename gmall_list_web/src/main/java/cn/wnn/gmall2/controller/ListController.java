package cn.wnn.gmall2.controller;

import cn.wnn.gmall2.bean.BaseAttrInfo;
import cn.wnn.gmall2.bean.BaseAttrValue;
import cn.wnn.gmall2.beanes.SkuInfoEs;
import cn.wnn.gmall2.beanes.SkuParamsEs;
import cn.wnn.gmall2.beanes.SkuResultEs;
import cn.wnn.gmall2.list.EsService;
import cn.wnn.gmall2.manager.ManageService;
import com.alibaba.dubbo.config.annotation.Reference;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import java.net.URLEncoder;
import java.util.*;

/**
 * Created by Administrator on 2018/5/5 0005.
 */
@Controller
public class ListController {

    @Reference
    EsService esService;
    @Reference
    ManageService manageService;

    @RequestMapping("list.html")
    public String list(SkuParamsEs skuParamsEs,Map map){
        //根据页面查询方式 从ES中skuinfoES集
        skuParamsEs.setPageSize(1);
        SkuResultEs skuResultEs = esService.searchSkuInfoList(skuParamsEs);

        //获取配置到的skuinfoEs
        List<SkuInfoEs> skuLsInfoList = skuResultEs.getSkuLsInfoList();

        //面包屑
        ArrayList<BaseAttrValue> attrValueSelectedList = new ArrayList<>();

        //平台属性
        String urlParam = makeUrlParam(skuParamsEs);
        //已经选择的平台属性值
        String[] parmaValueids = skuParamsEs.getValueId();
        List<String> parmaValueidList = null;
        if(parmaValueids!=null && parmaValueids.length>0) {
            parmaValueidList = Arrays.asList(parmaValueids);
        }
        //只有在有平台属性的情况下才进行平台属性处理，查询到商品包括的所有平台属性值
        List<String> attrValueIdList = skuResultEs.getAttrValueIdList();
        List<BaseAttrInfo> attrList = null;
        if(attrValueIdList != null && attrValueIdList.size()>0){
            attrList =manageService.getAttrList(attrValueIdList);
            Iterator<BaseAttrInfo> iterator = attrList.iterator();
            while (iterator.hasNext()){
                BaseAttrInfo baseAttrInfo = iterator.next();
                List<BaseAttrValue> attrValueList = baseAttrInfo.getAttrValueList();
    //            List<String> paramValueList = Arrays.asList(skuParamsEs.getValueId());
                for (BaseAttrValue baseAttrValue : attrValueList) {
                    if(parmaValueidList!=null && parmaValueidList.contains(baseAttrValue.getId())){
                        iterator.remove();

                        //添加面包屑对象
                        String urlParamSelect=makeUrlParam(skuParamsEs,baseAttrValue.getId());
                        BaseAttrValue attrValue = new BaseAttrValue();
                        attrValue.setUrlParam(urlParamSelect);
                        attrValue.setValueName(baseAttrInfo.getAttrName()+":"+baseAttrValue.getValueName());
                        attrValueSelectedList.add(attrValue);

                        continue;
                    }
                    baseAttrValue.setUrlParam(urlParam);
                }
            }
        }
        //要展示的平台属性
        map.put("attrList",attrList);
        //面包屑面部平台属性
        map.put("attrValueSelectedList",attrValueSelectedList);

        //商品列表展示的skuInfo
        map.put("skuLsInfoList",skuLsInfoList);

        //域中放置需要的信息：关键字，查询字符串，总页码
        map.put("totalPages",skuResultEs.getTotalPages());
        map.put("pageNo",skuParamsEs.getPageNo());
        map.put("urlParam",urlParam);
        //keyword在urlParam中已经包含了
        map.put("keyword",skuParamsEs.getKeyword());

        return "list";
    }

    private String makeUrlParam(SkuParamsEs skuParamsEs,String ... excludeValueIds){
        StringBuffer urlParam = new StringBuffer();

        //是否使用关键字
        if(skuParamsEs.getKeyword()!=null && skuParamsEs.getKeyword().length()>0){
            urlParam.append("keyword="+skuParamsEs.getKeyword());
        }
        //是否使用的3级分类
        if(skuParamsEs.getCatalog3Id() != null && skuParamsEs.getCatalog3Id().length() > 0 ){
            urlParam.append("&catalog3Id="+skuParamsEs.getCatalog3Id());
        }
        //是否使用属性值id
        if(skuParamsEs.getValueId() !=null && skuParamsEs.getValueId().length>0){
            for (String valueid : skuParamsEs.getValueId()) {

                //排除面包屑
                if(excludeValueIds != null && excludeValueIds.length>0){
                    if(valueid.equals(excludeValueIds[0])){
                        continue;
                    }
                }

                urlParam.append("&valueId="+valueid);
            }
        }
        System.out.print(urlParam.toString());
        return urlParam.toString();
    }

}
