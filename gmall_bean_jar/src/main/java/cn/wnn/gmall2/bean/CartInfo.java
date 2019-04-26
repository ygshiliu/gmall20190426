package cn.wnn.gmall2.bean;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.io.Serializable;
import java.math.BigDecimal;

/**
 * Created by Administrator on 2018/5/21 0021.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CartInfo implements Serializable{

    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    @Column
    private String id        ;
    @Column
    private String userId   ;
    @Column
    private String skuId    ;
    @Column
    private BigDecimal cartPrice;
    @Column
    private Integer skuNum   ;
    @Column
    private String imgUrl   ;
    @Column
    private String skuName  ;

    @Transient
    private  BigDecimal skuPrice;

    @Transient
    private String isChecked="0"; //0未选中;1选中

}
