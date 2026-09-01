package com.example.controller;

import cn.hutool.captcha.CaptchaUtil;
import cn.hutool.captcha.CircleCaptcha;
import com.example.common.constant.RedisKeyConstants;
import com.example.common.result.Result;
import com.example.common.storage.StorageService;
import com.example.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/shop/user")
public class UserController {

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private UserService userService;

    @Autowired
    private StorageService storageService;

    /** 允许上传的头像图片扩展名（小写，不含点） */
    private static final Set<String> ALLOW_AVATAR_EXT = Set.of("jpg", "jpeg", "png", "gif", "webp");

    @RequestMapping("/test")
    public String hello() {
        return "shop:user:test";
    }

    /**
     * 生成图形验证码并写入响应流
     * key: captcha:{mail}   value: code   过期: 60s
     */
    @RequestMapping("/verify")
    public void getVerify(String mail, HttpServletRequest request, HttpServletResponse response) throws IOException {
        CircleCaptcha captcha = CaptchaUtil.createCircleCaptcha(200, 100, 4, 20);
        String code = captcha.getCode();

        // 以 mail 为 key 存验证码，方便后续校验比对
        redisTemplate.opsForValue().set(
                RedisKeyConstants.CAPTCHA_PREFIX + mail,
                code,
                RedisKeyConstants.CAPTCHA_EXPIRE,
                TimeUnit.SECONDS);

        captcha.write(response.getOutputStream());
    }

    /**
     * 校验图形验证码（独立接口，备用）
     */
    @RequestMapping("/checkCode")
    public Result<Void> checkCode(String mail, String code) {
        if (mail == null || mail.isBlank()) {
            return Result.fail("邮箱不能为空");
        }
        if (code == null || code.isBlank()) {
            return Result.fail("验证码不能为空");
        }

        String key = RedisKeyConstants.CAPTCHA_PREFIX + mail;
        String stored = redisTemplate.opsForValue().get(key);

        if (stored == null) {
            return Result.fail("验证码已过期，请重新获取");
        }

        // 校验通过后立即删除，避免重复使用
        if (stored.equalsIgnoreCase(code)) {
            redisTemplate.delete(key);
            return Result.ok("验证码校验通过");
        }
        return Result.fail("验证码错误");
    }

    /**
     * 发送邮件验证码
     * 内部会先校验图形验证码，通过后生成 6 位数字验证码并发送到邮箱
     */
    @RequestMapping("/sendMail")
    public Result<Void> sendMail(String mail, String code) {
        return userService.sendMailCode(mail, code);
    }

    /**
     * 注册：校验邮件验证码 -> 入库
     */
    @RequestMapping("/register")
    public Result<Void> register(String mail, String code, String password) {
        return userService.register(mail, code, password);
    }

    /**
     * 登录：邮箱 + 密码
     */
    @RequestMapping("/login")
    public Result<Void> login(String mail, String password) {
        return userService.login(mail, password);
    }

    /**
     * 上传头像
     * 前端用 multipart/form-data 上传，文件存到七牛云，返回可直接访问的 URL
     */
    @RequestMapping("/uploadAvatar")
    public Result<Void> uploadAvatar(@RequestParam("file") MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return Result.fail("文件不能为空");
        }

        String originalName = file.getOriginalFilename();
        if (originalName == null) {
            return Result.fail("文件名无效");
        }

        // 取扩展名并校验类型，防止上传非图片或可执行文件
        String ext = "";
        int dot = originalName.lastIndexOf('.');
        if (dot >= 0 && dot < originalName.length() - 1) {
            ext = originalName.substring(dot + 1).toLowerCase();
        }
        if (ext.isEmpty() || !ALLOW_AVATAR_EXT.contains(ext)) {
            return Result.fail("仅支持 jpg/jpeg/png/gif/webp 格式");
        }

        try {
            String url = storageService.upload(file.getBytes(), ext, "avatar/");
            return Result.<Void>ok("上传成功").with("url", url);
        } catch (Exception e) {
            return Result.fail("上传失败：" + e.getMessage());
        }
    }

    /**
     * 保存用户资料（昵称、年龄、性别、头像）
     */
    @RequestMapping("/saveProfile")
    public Result<Void> saveProfile(String mail, String nickname, Integer age, Integer gender, String avatar) {
        return userService.saveProfile(mail, nickname, age, gender, avatar);
    }

    /**
     * 读取用户资料
     */
    @RequestMapping("/getProfile")
    public Result<java.util.Map<String, Object>> getProfile(String mail) {
        return userService.getProfile(mail);
    }
}
