package com.library.config;

import com.library.entity.User;
import com.library.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * 数据初始化器
 * 确保admin账户密码正确
 */
@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        // 确保admin账户密码正确
        Optional<User> adminOpt = userRepository.findByUsername("admin");
        if (adminOpt.isPresent()) {
            User admin = adminOpt.get();
            // 验证当前密码是否正确，如果不正确则更新
            if (!passwordEncoder.matches("123456", admin.getPassword())) {
                admin.setPassword(passwordEncoder.encode("123456"));
                userRepository.save(admin);
                System.out.println("已更新admin账户密码为：123456");
            }
        } else {
            // 如果admin账户不存在，创建一个
            User admin = new User();
            admin.setUsername("admin");
            admin.setPassword(passwordEncoder.encode("123456"));
            admin.setEmail("admin@library.com");
            admin.setRealName("系统管理员");
            admin.setPhone("13800000001");
            admin.setRole(User.Role.ADMIN);
            userRepository.save(admin);
            System.out.println("已创建admin账户，密码为：123456");
        }
    }
}

