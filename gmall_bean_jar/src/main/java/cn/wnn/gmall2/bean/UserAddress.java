package cn.wnn.gmall2.bean;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Id;
import java.io.Serializable;

/**
 * Created by Administrator on 2018/5/29 0029.
 */

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserAddress implements Serializable {
    @Id
    @Column
    private String id;
    @Column
    private String userAddress ;
    @Column
    private String userId;
    @Column
    private String consignee;
    @Column
    private String phoneNum;
    @Column
    private String isDefault;


}
