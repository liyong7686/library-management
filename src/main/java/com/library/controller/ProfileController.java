package com.library.controller;

import com.library.entity.User;
import com.library.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpSession;
import java.util.Map;

@Controller
public class ProfileController {
    
    @Autowired
    private UserService userService;

    /**
     * 显示个人资料页面
     */
    @GetMapping("/profile")
    public String profilePage(HttpSession session, Model model) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/login";
        }
        
        // 重新从数据库获取最新用户信息
        userService.findById(user.getId()).ifPresent(u -> {
            session.setAttribute("user", u);
            model.addAttribute("user", u);
        });
        
        return "profile";
    }

    /**
     * 更新个人资料
     */
    @PostMapping("/profile/update")
    @ResponseBody
    public Map<String, Object> updateProfile(@RequestParam(required = false) String email,
                                            @RequestParam(required = false) String realName,
                                            @RequestParam(required = false) String phone,
                                            HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            Map<String, Object> result = new java.util.HashMap<>();
            result.put("success", false);
            result.put("message", "请先登录");
            return result;
        }
        
        Map<String, Object> result = userService.updateProfile(user.getId(), email, realName, phone);
        
        // 如果更新成功，更新session中的用户信息
        if ((Boolean) result.get("success")) {
            User updatedUser = (User) result.get("user");
            session.setAttribute("user", updatedUser);
        }
        
        return result;
    }

    /**
     * 修改密码
     */
    @PostMapping("/profile/change-password")
    @ResponseBody
    public Map<String, Object> changePassword(@RequestParam String oldPassword,
                                             @RequestParam String newPassword,
                                             HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            Map<String, Object> result = new java.util.HashMap<>();
            result.put("success", false);
            result.put("message", "请先登录");
            return result;
        }
        
        return userService.changePassword(user.getId(), oldPassword, newPassword);
    }
}
