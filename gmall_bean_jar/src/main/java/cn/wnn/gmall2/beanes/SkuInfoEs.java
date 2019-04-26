package cn.wnn.gmall2.beanes;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

/**
 * Created by Administrator on 2018/5/3 0003.
 */
//与elasticsearch相关的bean
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SkuInfoEs implements Serializable {
    String id;
    BigDecimal price;
    String skuName;
    String skuDesc;
    String catalog3Id;
    String skuDefaultImg;
    Long hotScore = 0L;
    List<SkuAttrValueEs> skuAttrValueEsList;


}
