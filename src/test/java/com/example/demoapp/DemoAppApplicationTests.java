package com.example.demoapp;

import com.example.demoapp.entity.UserEntity;
import com.example.demoapp.repository.UserRepository;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.junit4.SpringRunner;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

@RunWith(SpringRunner.class)
@SpringBootTest
public class DemoAppApplicationTests {

    @Autowired
    private StringRedisTemplate stringRedisTemplate;
    /**
     * 用户Repository
     */
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;
    @Test
    public void contextLoads() {
        stringRedisTemplate.opsForValue().set("demoAop","demoAop");
        System.out.println(stringRedisTemplate.opsForValue().get("demoAop"));
    }

    @Test
    public void addUsers(){
        List<UserEntity> users = new ArrayList<>();
        // 循环设置10w 用户
        for(int i =0;i<10000000;i++){
            UserEntity user = new UserEntity();
            user.setName("用户"+i);
            user.setAge("26");
            user.setEmail("526282843@qq.com");
            users.add(user);
        }
        // 批量保存用户
//        userRepository.saveAll(users);
        jdbcTemplate.batchUpdate("insert into bd_user (age,email,name) values(?,?,?)", new BatchPreparedStatementSetter(){

            @Override
            public void setValues(PreparedStatement preparedStatement, int i) throws SQLException {
                String name = users.get(i).getName();
                String age = users.get(i).getAge();
                String email = users.get(i).getEmail();
                preparedStatement.setString(1,age);
                preparedStatement.setString(2,email);
                preparedStatement.setString(3,name);
            }

            @Override
            public int getBatchSize() {
                return users.size();
            }
        });

    }
}
