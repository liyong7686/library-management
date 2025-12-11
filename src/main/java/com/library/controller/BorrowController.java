package com.library.controller;

import com.library.entity.BorrowRecord;
import com.library.entity.User;
import com.library.service.BorrowService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.Map;

@Controller
@RequestMapping("/borrow")
public class BorrowController {
    @Autowired
    private BorrowService borrowService;

    @GetMapping
    public String myBorrowRecords(@RequestParam(defaultValue = "0") int page,
                                  @RequestParam(defaultValue = "10") int size,
                                  HttpSession session, Model model) {
        User user = (User) session.getAttribute("user");
        if (user == null || user.getRole() == User.Role.GUEST) {
            return "redirect:/login";
        }
        
        Pageable pageable = PageRequest.of(page, size);
        Page<BorrowRecord> recordPage = borrowService.getUserBorrowRecords(user.getId(), pageable);
        model.addAttribute("records", recordPage.getContent());
        model.addAttribute("totalPages", recordPage.getTotalPages());
        model.addAttribute("totalElements", recordPage.getTotalElements());
        model.addAttribute("currentPage", page);
        model.addAttribute("pageSize", size);
        return "borrow/my-records";
    }

    @PostMapping("/borrow")
    @ResponseBody
    public Map<String, Object> borrowBook(@RequestParam Long bookId, HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null || user.getRole() == User.Role.GUEST) {
            Map<String, Object> result = new HashMap<>();
            result.put("success", false);
            result.put("message", "请先登录");
            return result;
        }
        
        return borrowService.borrowBook(user.getId(), bookId);
    }

    @PostMapping("/return/{id}")
    @ResponseBody
    public Map<String, Object> returnBook(@PathVariable Long id, HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            Map<String, Object> result = new HashMap<>();
            result.put("success", false);
            result.put("message", "请先登录");
            return result;
        }
        
        // 检查权限：用户只能归还自己的书，管理员可以归还任何书
        BorrowRecord record = borrowService.findById(id)
                .orElseThrow(() -> new RuntimeException("借阅记录不存在"));
        if (user.getRole() != User.Role.ADMIN && !record.getUserId().equals(user.getId())) {
            Map<String, Object> result = new HashMap<>();
            result.put("success", false);
            result.put("message", "无权限");
            return result;
        }
        
        return borrowService.returnBook(id);
    }

    @GetMapping("/admin")
    public String adminBorrowRecords(@RequestParam(defaultValue = "0") int page,
                                     @RequestParam(defaultValue = "10") int size,
                                     HttpSession session, Model model) {
        User user = (User) session.getAttribute("user");
        if (user == null || user.getRole() != User.Role.ADMIN) {
            return "redirect:/dashboard";
        }
        Pageable pageable = PageRequest.of(page, size);
        Page<BorrowRecord> recordPage = borrowService.getAllBorrowRecords(pageable);
        model.addAttribute("records", recordPage.getContent());
        model.addAttribute("totalPages", recordPage.getTotalPages());
        model.addAttribute("totalElements", recordPage.getTotalElements());
        model.addAttribute("currentPage", page);
        model.addAttribute("pageSize", size);
        return "borrow/admin-records";
    }
}

