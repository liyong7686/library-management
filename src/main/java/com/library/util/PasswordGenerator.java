package com.library.util;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * 密码生成工具类
 * 用于生成BCrypt加密密码，更新SQL脚本中的密码值
 */
public class PasswordGenerator {
    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        String password = "123456";
        String encoded = encoder.encode(password);
        System.out.println("原始密码: " + password);
        System.out.println("BCrypt加密值: " + encoded);
        System.out.println("\n验证密码是否正确: " + encoder.matches(password, encoded));
    }
}

