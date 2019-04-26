package cn.wnn.gmall2.beanes;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * Created by Administrator on 2018/5/5 0005.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class SkuParamsEs implements Serializable{

   private String  keyword;

   private String catalog3Id;

   private String[] valueId;

   private String hotScore;

   private int pageNo=1;

   private int pageSize=1;

   private String urlParam;

}
