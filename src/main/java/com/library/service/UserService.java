package com.library.service;

import com.library.entity.User;
import com.library.repository.UserRepository;
import com.library.util.JwtUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.concurrent.TimeUnit;

@Service
public class UserService {
    
    private static final Logger logger = LoggerFactory.getLogger(UserService.class);
    
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    public Map<String, Object> register(User user) {
        Map<String, Object> result = new HashMap<>();
        
        if (userRepository.existsByUsername(user.getUsername())) {
            result.put("success", false);
            result.put("message", "用户名已存在");
            return result;
        }
        
        if (userRepository.existsByEmail(user.getEmail())) {
            result.put("success", false);
            result.put("message", "邮箱已被注册");
            return result;
        }
        
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        if (user.getRole() == null) {
            user.setRole(User.Role.USER);
        }
        
        User savedUser = userRepository.save(user);
        result.put("success", true);
        result.put("message", "注册成功");
        result.put("user", savedUser);
        return result;
    }

    public Map<String, Object> login(String username, String password) {
        Map<String, Object> result = new HashMap<>();
        
        Optional<User> userOpt = userRepository.findByUsername(username);
        if (!userOpt.isPresent()) {
            result.put("success", false);
            result.put("message", "用户名或密码错误");
            return result;
        }
        
        User user = userOpt.get();
        if (!passwordEncoder.matches(password, user.getPassword())) {
            result.put("success", false);
            result.put("message", "用户名或密码错误");
            return result;
        }
        
        String token = jwtUtil.generateToken(user.getUsername(), user.getRole().name());
        result.put("success", true);
        result.put("message", "登录成功");
        result.put("token", token);
        result.put("user", user);
        return result;
    }

    public Map<String, Object> sendResetCode(String email) {
        Map<String, Object> result = new HashMap<>();
        
        Optional<User> userOpt = userRepository.findByEmail(email);
        if (!userOpt.isPresent()) {
            result.put("success", false);
            result.put("message", "该邮箱未注册");
            return result;
        }
        
        String code = String.format("%06d", new Random().nextInt(1000000));
        try {
            redisTemplate.opsForValue().set("reset_code:" + email, code, 10, TimeUnit.MINUTES);
            result.put("success", true);
            result.put("message", "验证码已发送（实际项目中应发送邮件）");
            result.put("code", code); // 开发环境返回，生产环境应移除
        } catch (Exception e) {
            logger.error("Redis 操作失败，无法保存验证码: {}", e.getMessage(), e);
            result.put("success", false);
            result.put("message", "系统错误，请稍后重试");
        }
        return result;
    }

    public Map<String, Object> resetPassword(String email, String code, String newPassword) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            String storedCode = redisTemplate.opsForValue().get("reset_code:" + email);
            if (storedCode == null || !storedCode.equals(code)) {
                result.put("success", false);
                result.put("message", "验证码错误或已过期");
                return result;
            }
            
            Optional<User> userOpt = userRepository.findByEmail(email);
            if (!userOpt.isPresent()) {
                result.put("success", false);
                result.put("message", "用户不存在");
                return result;
            }
            
            User user = userOpt.get();
            user.setPassword(passwordEncoder.encode(newPassword));
            userRepository.save(user);
            
            redisTemplate.delete("reset_code:" + email);
            
            result.put("success", true);
            result.put("message", "密码重置成功");
        } catch (Exception e) {
            logger.error("Redis 操作失败，无法验证或删除验证码: {}", e.getMessage(), e);
            result.put("success", false);
            result.put("message", "系统错误，请稍后重试");
        }
        return result;
    }

    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    public Optional<User> findById(Long id) {
        return userRepository.findById(id);
    }
}

