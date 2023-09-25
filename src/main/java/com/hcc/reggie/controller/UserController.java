package com.hcc.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.hcc.reggie.common.R;
import com.hcc.reggie.entity.User;
import com.hcc.reggie.service.UserService;
import com.hcc.reggie.utils.SMSUtils;
import com.hcc.reggie.utils.ValidateCodeUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpSession;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Slf4j
@RestController
@RequestMapping("/user")
public class UserController {
    @Autowired
    private UserService userService;
    @Autowired
    private RedisTemplate redisTemplate;


    /** 发送手机验证码 */
    @PostMapping("/sendMsg")
    public R<String> sendMsg(HttpSession session, @RequestBody User user) {
        String phone = user.getPhone(); // 获取手机号
        if (phone != null) { // 手机号不为空
            String code = ValidateCodeUtils.generateValidateCode(4).toString();  // 生成随机的 4 位验证码
            log.info("验证码：code = {}", code);
             // SMSUtils.sendMessage("外卖短信验证", "", phone, code); // 调用阿里云提供的短信服务 API 完成短信发送
             // session.setAttribute(phone, code); // 将生成的验证码存储到 Session中
            redisTemplate.opsForValue().set(phone, code, 5, TimeUnit.MINUTES); // 将生成的验证码缓存到 Redis 中，并且设置有效期为 5 分钟
            return R.success("验证码已发送");
        }
        return R.success("短信发送失败"); // 手机号为空
    }

    /**
     * 用户登录
     *
     * @param session 会话
     * @param map 接收客户端传过来的 JSON 数据
     * @return 登录成功或失败
     */
    @PostMapping("/login")
    public R<User> login(HttpSession session, @RequestBody Map map) {
        // 获取客户端传送的手机号和验证码
        String phone = map.get("phone").toString();
        String code = map.get("code").toString();
        // 获取存储在 Session 中的验证码
        // String realCode = session.getAttribute(phone).toString();
        // 获取存储在 Redis 中的验证码
        String realCode = redisTemplate.opsForValue().get(phone).toString();


        if (code != null && code.equals(realCode)) {
            // 判断为新用户则自动注册
            LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(User::getPhone, phone);
            User user = userService.getOne(queryWrapper);
            if (user == null) {
                user = new User();
                user.setPhone(phone);
                user.setStatus(1);
                userService.save(user);
            }

            // 登录成功，删除 Redis 缓存中的验证码，并返回成功
            redisTemplate.delete(phone);
            session.setAttribute("user", user.getId());
            return R.success(user);
        }

        // 验证码错误
        return R.error("验证码错误");
    }

    /** 员工退出 */
    @PostMapping("/loginout")
    public R<String> logout(HttpSession session) {
        session.removeAttribute("user");
        return R.success("退出成功");
    }
}
