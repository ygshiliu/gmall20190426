package cn.wnn.gmall2.bean;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * Created by Administrator on 2018/4/18 0018.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class SpuSaleAttr implements Serializable {
    @Id
    @Column
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private String id ;

    @Column
    private String spuId;

    @Column
    private String saleAttrId;

    @Column
    private String saleAttrName;


    @Transient
    private List<SpuSaleAttrValue> spuSaleAttrValueList;

    @Transient
    private Map spuSaleAttrValueJson;


}
