package com.example.controller;

import cn.hutool.captcha.CaptchaUtil;
import cn.hutool.captcha.CircleCaptcha;
import com.example.common.constant.RedisKeyConstants;
import com.example.common.result.Result;
import com.example.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/shop/user")
public class UserController {

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private UserService userService;

    // 头像上传目录（从配置读取，默认 D:/buka-shop-files/avatars）
    @Value("${app.upload.avatar-dir:D:/buka-shop-files/avatars}")
    private String avatarDir;

    // 头像访问路径前缀
    @Value("${app.upload.avatar-url:/avatars}")
    private String avatarUrlPrefix;

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
     * 前端用 multipart/form-data 上传，返回可访问的 URL
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

        // 取扩展名
        String ext = "";
        int dot = originalName.lastIndexOf('.');
        if (dot >= 0) {
            ext = originalName.substring(dot);
        }

        // 新文件名：uuid + 扩展名
        String newFileName = UUID.randomUUID().toString().replace("-", "") + ext;

        File dir = new File(avatarDir);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        File dest = new File(dir, newFileName);
        try {
            file.transferTo(dest);
        } catch (IOException e) {
            return Result.fail("上传失败：" + e.getMessage());
        }

        // 返回可访问的 URL（由 WebConfig 把 /avatars/** 映射到本地目录）
        String url = avatarUrlPrefix + "/" + newFileName;
        return Result.<Void>ok("上传成功").with("url", url);
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
