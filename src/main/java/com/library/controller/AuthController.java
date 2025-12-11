package com.library.controller;

import com.library.entity.User;
import com.library.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.servlet.http.HttpSession;
import java.util.Map;

@Controller
public class AuthController {
    @Autowired
    private UserService userService;

    @GetMapping("/")
    public String index() {
        return "redirect:/login";
    }

    @GetMapping("/login")
    public String loginPage() {
        return "login";
    }

    @PostMapping("/login")
    @ResponseBody
    public Map<String, Object> login(@RequestParam String username, 
                                     @RequestParam String password,
                                     HttpSession session) {
        Map<String, Object> result = userService.login(username, password);
        if ((Boolean) result.get("success")) {
            User user = (User) result.get("user");
            session.setAttribute("user", user);
            session.setAttribute("token", result.get("token"));
        }
        return result;
    }

    @GetMapping("/register")
    public String registerPage() {
        return "register";
    }

    @PostMapping("/register")
    @ResponseBody
    public Map<String, Object> register(@ModelAttribute User user) {
        return userService.register(user);
    }

    @GetMapping("/forgot-password")
    public String forgotPasswordPage() {
        return "forgot-password";
    }

    @PostMapping("/forgot-password/send-code")
    @ResponseBody
    public Map<String, Object> sendResetCode(@RequestParam String email) {
        return userService.sendResetCode(email);
    }

    @PostMapping("/forgot-password/reset")
    @ResponseBody
    public Map<String, Object> resetPassword(@RequestParam String email,
                                            @RequestParam String code,
                                            @RequestParam String newPassword) {
        return userService.resetPassword(email, code, newPassword);
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/login";
    }
}

