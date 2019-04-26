package cn.wnn.gmall2.bean;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.Transient;
import java.io.Serializable;

/**
 * Created by Administrator on 2018/4/18 0018.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SpuSaleAttrValue implements Serializable {

    @Id
    @Column
    private String id ;

    @Column
    private String spuId;

    @Column
    private String saleAttrId;

    @Column
    private String saleAttrValueName;

    @Transient
    private String isChecked;


}
