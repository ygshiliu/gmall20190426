package cn.wnn.gmall2.usermanager.service;

import cn.wnn.gmall2.bean.UserAddress;
import cn.wnn.gmall2.bean.UserInfo;

import java.util.List;
import java.util.Map;

/**
 * Created by Administrator on 2018/4/13 0013.
 */
public interface UserInfoService {

  //  public List<UserInfo> getUserInfoList(UserInfo userInfoQuery);

 //   public UserInfo getUserInfo(UserInfo userInfoQuery);

  //  public void delete(UserInfo userInfoQuery);

    public void addUserInfo(UserInfo userInfo);
    public UserInfo login(UserInfo userInfo);

    UserInfo verify(Map<String, Object> map);

    //  public void updateUserInfo(UserInfo userInfo);

    public List<UserAddress> getUserAddressList(String userId);
}
