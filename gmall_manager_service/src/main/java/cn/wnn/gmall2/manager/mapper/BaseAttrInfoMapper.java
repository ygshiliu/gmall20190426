package cn.wnn.gmall2.manager.mapper;

import cn.wnn.gmall2.bean.BaseAttrInfo;
import org.apache.ibatis.annotations.Param;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;

/**
 * Created by Administrator on 2018/4/16 0016.
 */
public interface BaseAttrInfoMapper extends Mapper<BaseAttrInfo> {

    public List<BaseAttrInfo> selectAttrList(long catalog3Id);

    List<BaseAttrInfo> selectAttrListByValueIds(@Param("valueIds")List valueIds);
}
