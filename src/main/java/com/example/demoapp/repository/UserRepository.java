package com.example.demoapp.repository;

import com.example.demoapp.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

/***
 * 继承 JpaRepository实现用户实体类与数据库之间的操作
 */
public interface UserRepository  extends JpaRepository<UserEntity,Long> {
}
