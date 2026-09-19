package com.example.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

/**
 * 用户实体
 */
@TableName("user")
public class User {

    @TableId(type = IdType.AUTO)
    private Integer id;

    /** 邮箱（登录账号） */
    private String mail;

    /** 密码（明文，演示用；生产环境请加密） */
    private String password;

    /** 昵称 */
    private String nickname;

    /** 年龄 */
    private Integer age;

    /** 性别：0 女，1 男 */
    private Integer gender;

    /** 头像访问 URL */
    private String avatar;

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getMail() { return mail; }
    public void setMail(String mail) { this.mail = mail; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getNickname() { return nickname; }
    public void setNickname(String nickname) { this.nickname = nickname; }

    public Integer getAge() { return age; }
    public void setAge(Integer age) { this.age = age; }

    public Integer getGender() { return gender; }
    public void setGender(Integer gender) { this.gender = gender; }

    public String getAvatar() { return avatar; }
    public void setAvatar(String avatar) { this.avatar = avatar; }
}
