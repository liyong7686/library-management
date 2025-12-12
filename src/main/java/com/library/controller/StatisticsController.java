package com.library.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.library.entity.User;
import com.library.service.StatisticsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import jakarta.servlet.http.HttpSession;
import java.util.Map;

@Controller
@RequestMapping("/statistics")
public class StatisticsController {
    
    @Autowired
    private StatisticsService statisticsService;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    /**
     * 显示统计报表页面（管理员）
     */
    @GetMapping
    public String statisticsPage(HttpSession session, Model model) {
        User user = (User) session.getAttribute("user");
        if (user == null || user.getRole() != User.Role.ADMIN) {
            return "redirect:/dashboard";
        }
        
        // 获取所有统计数据
        Map<String, Object> report = statisticsService.getFullStatisticsReport();
        model.addAttribute("report", report);
        
        // 将数据序列化为 JSON 字符串，方便前端使用
        try {
            model.addAttribute("reportJson", objectMapper.writeValueAsString(report));
        } catch (Exception e) {
            model.addAttribute("reportJson", "{}");
        }
        
        return "statistics/report";
    }
    
    /**
     * 获取统计数据（JSON API）
     */
    @GetMapping("/api/data")
    @ResponseBody
    public Map<String, Object> getStatisticsData(HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null || user.getRole() != User.Role.ADMIN) {
            return Map.of("success", false, "message", "无权限");
        }
        
        return statisticsService.getFullStatisticsReport();
    }
}
