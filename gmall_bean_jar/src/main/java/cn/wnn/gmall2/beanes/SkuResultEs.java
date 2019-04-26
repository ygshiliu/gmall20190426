package cn.wnn.gmall2.beanes;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * Created by Administrator on 2018/5/5 0005.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class SkuResultEs implements Serializable {

   private List<SkuInfoEs> skuLsInfoList;

   private long total;

   private long totalPages;

   private List<String> attrValueIdList;

}
