package com.example.common.constant;

/**
 * Redis key 常量
 * 各模块共用同一套前缀，避免散落在各业务类中
 */
public interface RedisKeyConstants {

    /** 图形验证码 key 前缀，完整 key = captcha:{mail} */
    String CAPTCHA_PREFIX = "captcha:";

    /** 邮件验证码 key 前缀，完整 key = mailcode:{mail} */
    String MAIL_CODE_PREFIX = "mailcode:";

    /** 图形验证码有效期（秒） */
    long CAPTCHA_EXPIRE = 60L;

    /** 邮件验证码有效期（秒） */
    long MAIL_CODE_EXPIRE = 300L;
}
