package com.example.demoapp.service.Impl;

import com.example.demoapp.entity.UserEntity;
import com.example.demoapp.repository.UserRepository;
import com.example.demoapp.service.IUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

@Service
public class UserServiceImpl implements IUserService {

    /**
     * 用户Responsitory
     */
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    /**
     * 查询用户列表方法
     *
     * @return
     */
    @Override
    public List<UserEntity> selectUserList() {
        return userRepository.findAll();
    }

    /**
     * 添加用户方法
     *
     * @return
     */
    @Override
    @Async("asyncServiceExecutor")
    public String addUsers() {
        // @Async 主要方便添加用户 , 想快速增加测一下Redis缓存的效率
        List<UserEntity> users = new ArrayList<>();
        // 循环设置用户
        for (int i = 0; i < 100; i++) {
            UserEntity user = new UserEntity();
            user.setName("用户" + i);
            user.setAge("26");
            user.setEmail("526282843@qq.com");
            users.add(user);
        }
        // 批量保存用户  效率贼低 试试jdbcTemplate
        // userRepository.saveAll(users);
        return batchUsers(users);
    }

    /**
     * jdbcTemlapte批量保存用户
     * @param users
     * @return
     */
    public String batchUsers(List<UserEntity> users){
        jdbcTemplate.batchUpdate("insert into bd_user (age,email,name) values(?,?,?)", new BatchPreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement preparedStatement, int i) throws SQLException {
                String name = users.get(i).getName();
                String age = users.get(i).getAge();
                String email = users.get(i).getEmail();
                preparedStatement.setString(1, age);
                preparedStatement.setString(2, email);
                preparedStatement.setString(3, name);
            }
            @Override
            public int getBatchSize() {
                return users.size();
            }
        });
        return "新增成功";
    }

}
