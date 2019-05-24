package com.example.demoapp.entity;

import lombok.Data;

import javax.persistence.*;

@Entity
@Table(name = "bd_user")
@Data
public class UserEntity {
    /**
     * 用户id
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    /**
     * 用户名
     */
    private String name;
    /**
     * 年龄
     */
    private String age;
    /**
     * 用户email
     */
    private String email;
}
