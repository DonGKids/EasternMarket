package com.example.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.common.constant.RedisKeyConstants;
import com.example.common.result.Result;
import com.example.entity.User;
import com.example.feign.CouponFeignClient;
import com.example.mapper.UserMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
public class UserService {

    private static final Logger log = LoggerFactory.getLogger(UserService.class);

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private JavaMailSender mailSender;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private CouponFeignClient couponFeignClient;

    @Value("${spring.mail.username}")
    private String from;

    // 新人券模板ID（注册成功后自动发放）
    @Value("${coupon.new-user-template-id:0}")
    private Long newUserTemplateId;

    /**
     * 校验图形验证码并发送邮件验证码
     */
    public Result<Void> sendMailCode(String mail, String code) {
        if (mail == null || mail.isBlank()) {
            return Result.fail("邮箱不能为空");
        }
        if (code == null || code.isBlank()) {
            return Result.fail("图形验证码不能为空");
        }

        // 1. 校验图形验证码
        String captchaKey = RedisKeyConstants.CAPTCHA_PREFIX + mail;
        String storedCaptcha = redisTemplate.opsForValue().get(captchaKey);

        if (storedCaptcha == null) {
            return Result.fail("图形验证码已过期，请重新获取");
        }
        if (!storedCaptcha.equalsIgnoreCase(code)) {
            return Result.fail("图形验证码错误");
        }
        // 校验通过后立即删除，避免重复使用
        redisTemplate.delete(captchaKey);

        // 2. 生成 6 位数字邮件验证码
        String mailCode = String.format("%06d", (int) (Math.random() * 1000000));

        // 3. 存入 Redis，5 分钟过期
        redisTemplate.opsForValue().set(
                RedisKeyConstants.MAIL_CODE_PREFIX + mail,
                mailCode,
                RedisKeyConstants.MAIL_CODE_EXPIRE,
                TimeUnit.SECONDS);

        // 4. 发送邮件
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(from);
            message.setTo(mail);
            message.setSubject("【buka-shop】注册验证码");
            message.setText("您的验证码是：" + mailCode + "，5 分钟内有效，请勿泄露。");
            mailSender.send(message);
        } catch (Exception e) {
            return Result.fail("邮件发送失败：" + e.getMessage());
        }

        return Result.ok("邮件验证码已发送，请查收");
    }

    /**
     * 注册：校验邮件验证码 -> 校验邮箱是否已注册 -> 入库
     */
    public Result<Void> register(String mail, String code, String password) {
        if (mail == null || mail.isBlank()) {
            return Result.fail("邮箱不能为空");
        }
        if (code == null || code.isBlank()) {
            return Result.fail("邮件验证码不能为空");
        }
        if (password == null || password.length() < 6) {
            return Result.fail("密码至少 6 位");
        }

        // 1. 校验邮件验证码
        String mailCodeKey = RedisKeyConstants.MAIL_CODE_PREFIX + mail;
        String storedCode = redisTemplate.opsForValue().get(mailCodeKey);
        if (storedCode == null) {
            return Result.fail("邮件验证码已过期，请重新发送");
        }
        if (!storedCode.equalsIgnoreCase(code)) {
            return Result.fail("邮件验证码错误");
        }
        redisTemplate.delete(mailCodeKey);

        // 2. 校验邮箱是否已注册
        User exist = userMapper.selectOne(new QueryWrapper<User>().eq("mail", mail));
        if (exist != null) {
            return Result.fail("该邮箱已注册");
        }

        // 3. 入库
        User user = new User();
        user.setMail(mail);
        user.setPassword(password); // 演示用明文，生产环境请加密
        userMapper.insert(user);

        // 4. 自动发放新人券（失败不影响注册主流程）
        if (newUserTemplateId != null && newUserTemplateId > 0) {
            try {
                couponFeignClient.receive(Long.valueOf(user.getId()), newUserTemplateId);
                log.info("新人券发放成功，userId={}, templateId={}", user.getId(), newUserTemplateId);
            } catch (Exception e) {
                log.warn("新人券发放失败（不影响注册），userId={}, templateId={}, err={}",
                        user.getId(), newUserTemplateId, e.getMessage());
            }
        }

        return Result.ok("注册成功，已赠送新人优惠券");
    }

    /**
     * 登录：邮箱 + 密码
     */
    public Result<Void> login(String mail, String password) {
        if (mail == null || mail.isBlank() || password == null || password.isBlank()) {
            return Result.fail("邮箱和密码不能为空");
        }

        User user = userMapper.selectOne(new QueryWrapper<User>().eq("mail", mail));
        if (user == null) {
            return Result.fail("邮箱未注册");
        }
        if (!user.getPassword().equals(password)) {
            return Result.fail("密码错误");
        }

        // 演示用 token，生产环境请用 JWT
        String token = "mock-token-" + mail + "-" + System.currentTimeMillis();

        // 补发新人券（为历史注册用户、或注册时发券失败的用户兜底）
        // receive() 内部已按 perLimit 校验，重复调用不会重复发券，失败不影响登录
        if (newUserTemplateId != null && newUserTemplateId > 0) {
            try {
                Result<Void> r = couponFeignClient.receive(Long.valueOf(user.getId()), newUserTemplateId);
                if (r.isSuccess()) {
                    log.info("补发新人券成功，userId={}, templateId={}", user.getId(), newUserTemplateId);
                } else {
                    // 返回非成功：大概率是"已领取"/"领完"等业务原因，静默处理
                    log.debug("补发新人券跳过：userId={}, msg={}", user.getId(), r.getMsg());
                }
            } catch (Exception e) {
                log.warn("补发新人券失败（不影响登录），userId={}, err={}", user.getId(), e.getMessage());
            }
        }

        return Result.<Void>ok("登录成功")
                .with("token", token)
                .with("mail", mail)
                .with("userId", user.getId());
    }

    /**
     * 保存/更新用户资料（昵称、年龄、性别、头像）
     * 通过 mail 定位用户
     */
    public Result<Void> saveProfile(String mail, String nickname, Integer age, Integer gender, String avatar) {
        if (mail == null || mail.isBlank()) {
            return Result.fail("邮箱不能为空");
        }

        User user = userMapper.selectOne(new QueryWrapper<User>().eq("mail", mail));
        if (user == null) {
            return Result.fail("用户不存在");
        }

        if (nickname != null) user.setNickname(nickname);
        if (age != null) user.setAge(age);
        if (gender != null) user.setGender(gender);
        if (avatar != null) user.setAvatar(avatar);

        userMapper.updateById(user);

        return Result.ok("资料保存成功");
    }

    /**
     * 读取用户资料
     */
    public Result<Map<String, Object>> getProfile(String mail) {
        if (mail == null || mail.isBlank()) {
            return Result.fail("邮箱不能为空");
        }

        User user = userMapper.selectOne(new QueryWrapper<User>().eq("mail", mail));
        if (user == null) {
            return Result.fail("用户不存在");
        }

        Map<String, Object> data = new HashMap<>();
        data.put("userId", user.getId());
        data.put("mail", user.getMail());
        data.put("nickname", user.getNickname());
        data.put("age", user.getAge());
        data.put("gender", user.getGender());
        data.put("avatar", user.getAvatar());

        return Result.ok("ok", data);
    }
}
