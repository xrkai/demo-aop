package com.example.demoapp.service;

import com.example.demoapp.entity.UserEntity;

import java.util.List;

/**
 * 用户管理业务层接口
 */
public interface IUserService {
    /**
     * 查询所有用户信息
     * @return
     */
    List<UserEntity> selectUserList();

    /**
     * 添加用户
     * @return
     */
    String addUsers();
}
