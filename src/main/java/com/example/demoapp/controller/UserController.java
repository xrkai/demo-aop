package com.example.demoapp.controller;

import com.example.demoapp.config.rediscatch.RedisCache;
import com.example.demoapp.entity.UserEntity;
import com.example.demoapp.service.IUserService;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import java.util.List;

@Controller
@RequestMapping("/")
@Log4j2
@RedisCache // 对象级别
public class UserController {

    /**
     * 用户业务层接口
     */
    @Autowired
    private IUserService iUserService;

    /**
     * 跳转用户列表界面
     *
     * @return
     */
    @RequestMapping(value = "/index")
//    @RedisCache  方法级别
    public String selectUsers(ModelMap model) {
        // 查询开始时间
        long beginTime = System.currentTimeMillis();
        List<UserEntity> users = iUserService.selectUserList();
        model.addAttribute("users", users);
        log.warn("查询数据库 >>>> end 耗时：" + (System.currentTimeMillis() - beginTime));
        model.addAttribute("time", (System.currentTimeMillis() - beginTime));
        return "index.html";
    }

    /**
     * 批量增加用户
     * Get方便快速添加用户测试
     * @return
     */
    @GetMapping(value = "/addUser")
    public String addUsers(ModelMap model) {
        iUserService.addUsers();
        model.addAttribute("msg","异步新增");
        return "add.html";
    }
}
