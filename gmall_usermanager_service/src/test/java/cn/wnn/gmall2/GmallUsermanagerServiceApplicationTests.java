package cn.wnn.gmall2;

import cn.wnn.gmall2.bean.UserInfo;
import cn.wnn.gmall2.usermanager.service.UserInfoService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
public class GmallUsermanagerServiceApplicationTests {

	@Autowired
	UserInfoService userInfoService;

	@Test
	public void contextLoads() {
        UserInfo userInfo = new UserInfo();
        userInfo.setName("haha");
        userInfo.setPasswd("123123");
        userInfo.setEmail("haha@163.com");

        userInfoService.addUserInfo(userInfo);

    }

}
