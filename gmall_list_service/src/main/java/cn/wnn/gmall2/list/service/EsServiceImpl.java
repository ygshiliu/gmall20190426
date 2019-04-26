package cn.wnn.gmall2.list.service;

import cn.wnn.gmall2.beanes.SkuAttrValueEs;
import cn.wnn.gmall2.beanes.SkuInfoEs;
import cn.wnn.gmall2.beanes.SkuParamsEs;
import cn.wnn.gmall2.beanes.SkuResultEs;
import cn.wnn.gmall2.list.EsService;
import com.alibaba.dubbo.config.annotation.Service;
import io.searchbox.client.JestClient;
import io.searchbox.core.Index;
import io.searchbox.core.Search;
import io.searchbox.core.SearchResult;
import io.searchbox.core.search.aggregation.MetricAggregation;
import io.searchbox.core.search.aggregation.TermsAggregation;
import org.elasticsearch.index.query.*;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.terms.TermsBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.highlight.HighlightBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.TreeSet;

/**
 * Created by Administrator on 2018/5/4 0004.
 */
@Service
public class EsServiceImpl implements EsService {

    public static final String index_name_gmall = "gmalles";

    public static final String type_name_gmall = "skuInfoEs";

    //springboot自动创建
    @Autowired
    JestClient jestClient;

    //managerWeb上架调用
    public void saveSkuInfoEs(SkuInfoEs skuInfoEs) {
        Index index = new Index.Builder(skuInfoEs).index(index_name_gmall).type(type_name_gmall).id(skuInfoEs.getId()).build();
        try {
            jestClient.execute(index);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public SkuResultEs searchSkuInfoList(SkuParamsEs skuLsParam) {
        String query = makeQueryStringForSearch(skuLsParam);

        Search search = new Search.Builder(query).addIndex(index_name_gmall).addType(type_name_gmall).build();
        SearchResult searchResult = null;
        try {
            searchResult = jestClient.execute(search);
        } catch (IOException e) {
            e.printStackTrace();
        }
        SkuResultEs skuLsResult = makeResultForSearch(skuLsParam, searchResult);
        return skuLsResult;

    }


    private SkuResultEs makeResultForSearch(SkuParamsEs skuLsParams, SearchResult searchResult) {
        SkuResultEs skuLsResult = new SkuResultEs();

        //获取sku列表
        List<SearchResult.Hit<SkuInfoEs, Void>> hits = searchResult.getHits(SkuInfoEs.class);
        List<SkuInfoEs> skuLsInfoList = new ArrayList<>(hits.size());
        TreeSet<String> ts = new TreeSet<>();
        for (SearchResult.Hit<SkuInfoEs, Void> hit : hits) {
            SkuInfoEs skuLsInfo = hit.source;
            if (hit.highlight != null && hit.highlight.size() > 0) {
                List<String> list = hit.highlight.get("skuName");
                //把带有高亮标签的字符串替换skuName
                String skuNameHl = list.get(0);
                skuLsInfo.setSkuName(skuNameHl);
            }
            skuLsInfoList.add(skuLsInfo);

            //更新部分：将skuInfoEs中的平台属性值摘取出来放入SkuResultEs中
            List<SkuAttrValueEs> skuAttrValueEsList = skuLsInfo.getSkuAttrValueEsList();
            if(skuAttrValueEsList !=null && skuAttrValueEsList.size()>0){
                Iterator<SkuAttrValueEs> iterator = skuAttrValueEsList.iterator();
                while (iterator.hasNext()){
                    SkuAttrValueEs next = iterator.next();
                    String valueId = next.getValueId();
                    ts.add(valueId);
                }
            }
        }

        skuLsResult.setSkuLsInfoList(skuLsInfoList);
        skuLsResult.setTotal(searchResult.getTotal());
        skuLsResult.setAttrValueIdList(new ArrayList<>(ts));

        //取记录个数并计算出总页数
//        long totalPage = (searchResult.getTotal() + skuLsParams.getPageSize() - 1) / skuLsParams.getPageSize();

        long totalPage = (searchResult.getTotal() % skuLsParams.getPageSize()) == 0 ? (searchResult.getTotal() % skuLsParams.getPageSize()) : (searchResult.getTotal() % skuLsParams.getPageSize()) + 1;
        skuLsResult.setTotalPages(totalPage);



        //取出涉及的属性值id
//        MetricAggregation aggregations = searchResult.getAggregations();
//        TermsAggregation groupby_attr = aggregations.getTermsAggregation("groupby_attr");
//        if (groupby_attr != null) {
//            List<TermsAggregation.Entry> buckets = groupby_attr.getBuckets();
//            List<String> attrValueIdList = new ArrayList<>(buckets.size());
//            for (TermsAggregation.Entry bucket : buckets) {
//                attrValueIdList.add(bucket.getKey());
//            }
//            skuLsResult.setAttrValueIdList(attrValueIdList);
//        }

        return skuLsResult;
    }

    @Test
    public void test() {
        SkuParamsEs skuParamsEs = new SkuParamsEs();
//        skuParamsEs.setCatalog3Id("61");
        skuParamsEs.setKeyword("荣耀10");
        skuParamsEs.setPageNo(1);
        skuParamsEs.setPageSize(1);

//          String[] valueId = {"11","13","14"};
//          skuParamsEs.setValueId(valueId);

        makeQueryStringForSearch(skuParamsEs);

    }
/*
{
  "from" : 0,
  "size" : 2,
  "query" : {
    "bool" : {
      "must" : {
        "match" : {
          "skuName" : {
            "query" : "10"
          }
        }
      },
      "filter" : [ {
        "terms" : {
          "skuAttrValueList.valueId" : [ "10" ]
        }
      }, {
        "terms" : {
          "skuAttrValueList.valueId" : [ "15" ]
        }
      } ]
    }
  },
  "sort" : [ {
    "hotScore" : {
      "order" : "desc"
    }
  } ],
  "aggregations" : {
    "groupby_attr" : {
      "terms" : {
        "field" : "skuAttrValueList.valueId"
      }
    }
  },
  "highlight" : {
    "pre_tags" : [ "<span style='color:red'>" ],
    "post_tags" : [ "</span>" ],
    "fields" : {
      "skuName" : { }
    }
  }
}

* */
    private String makeQueryStringForSearch(SkuParamsEs skuLsParams) {
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        //
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
        if (skuLsParams.getKeyword() != null) {
            MatchQueryBuilder matchQueryBuilder = new MatchQueryBuilder("skuName", skuLsParams.getKeyword());
            boolQueryBuilder.must(matchQueryBuilder);

            HighlightBuilder highlightBuilder = new HighlightBuilder();
            highlightBuilder.field("skuName");
            highlightBuilder.preTags("<span style='color:red'>");
            highlightBuilder.postTags("</span>");
            searchSourceBuilder.highlight(highlightBuilder);

            TermsBuilder groupby_attr = AggregationBuilders.terms("groupby_attr").field("skuAttrValueList.valueId");
            searchSourceBuilder.aggregation(groupby_attr);
        }
        if (skuLsParams.getCatalog3Id() != null) {
            QueryBuilder termQueryBuilder = new TermQueryBuilder("catalog3Id", skuLsParams.getCatalog3Id());
            boolQueryBuilder.filter(termQueryBuilder);
        }
        if (skuLsParams.getValueId() != null && skuLsParams.getValueId().length >= 0) {

            for (int i = 0; i < skuLsParams.getValueId().length; i++) {
                String valueId = skuLsParams.getValueId()[i];
                QueryBuilder termQueryBuilder = new TermsQueryBuilder("skuAttrValueEsList.valueId", valueId);
                boolQueryBuilder.filter(termQueryBuilder);
            }

        }
        searchSourceBuilder.query(boolQueryBuilder);
        //
        int from = (skuLsParams.getPageNo() - 1) * skuLsParams.getPageSize();
        searchSourceBuilder.from(from);
        searchSourceBuilder.size(skuLsParams.getPageSize());
        //
        searchSourceBuilder.sort("hotScore", SortOrder.DESC);

        String query = searchSourceBuilder.toString();

        System.err.println("query = " + query);
        return query;
    }

}
