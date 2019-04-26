package cn.wnn.gmall2.manager.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Created by Administrator on 2018/4/16 0016.
 */
@Controller
public class ManagerController {

    @RequestMapping("index")
    public String index(){
        return "index";
    }


}
