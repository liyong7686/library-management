package com.library.controller;

import com.library.entity.Book;
import com.library.entity.User;
import com.library.service.BookService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/books")
public class BookController {
    @Autowired
    private BookService bookService;

    @GetMapping
    public String listBooks(@RequestParam(required = false) String keyword,
                           @RequestParam(required = false) String category,
                           @RequestParam(defaultValue = "0") int page,
                           @RequestParam(defaultValue = "10") int size,
                           Model model) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Book> bookPage;
        
        if (keyword != null && !keyword.isEmpty()) {
            List<Book> books = bookService.searchBooks(keyword);
            model.addAttribute("books", books);
        } else if (category != null && !category.isEmpty()) {
            List<Book> books = bookService.findByCategory(category);
            model.addAttribute("books", books);
        } else {
            bookPage = bookService.findAll(pageable);
            model.addAttribute("books", bookPage.getContent());
            model.addAttribute("totalPages", bookPage.getTotalPages());
            model.addAttribute("currentPage", page);
        }
        
        return "books/list";
    }

    @GetMapping("/{id}")
    public String viewBook(@PathVariable Long id, Model model) {
        // 使用带缓存的方法
        Book book = bookService.getBookById(id);
        if (book == null) {
            model.addAttribute("error", "图书不存在");
            model.addAttribute("message", "您访问的图书ID为 " + id + " 的图书不存在，可能已被删除或ID错误。");
            return "error";
        }
        model.addAttribute("book", book);
        return "books/detail";
    }

    @GetMapping("/admin")
    public String adminListBooks(@RequestParam(defaultValue = "0") int page,
                                  @RequestParam(defaultValue = "10") int size,
                                  HttpSession session, Model model) {
        User user = (User) session.getAttribute("user");
        if (user == null || user.getRole() != User.Role.ADMIN) {
            return "redirect:/dashboard";
        }
        Pageable pageable = PageRequest.of(page, size);
        Page<Book> bookPage = bookService.findAll(pageable);
        model.addAttribute("books", bookPage.getContent());
        model.addAttribute("totalPages", bookPage.getTotalPages());
        model.addAttribute("totalElements", bookPage.getTotalElements());
        model.addAttribute("currentPage", page);
        model.addAttribute("pageSize", size);
        return "books/admin-list";
    }

    @GetMapping("/admin/add")
    public String addBookPage(HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null || user.getRole() != User.Role.ADMIN) {
            return "redirect:/dashboard";
        }
        return "books/add";
    }

    @PostMapping("/admin/add")
    @ResponseBody
    public Map<String, Object> addBook(@ModelAttribute Book book, HttpSession session) {
        Map<String, Object> result = new HashMap<>();
        User user = (User) session.getAttribute("user");
        if (user == null || user.getRole() != User.Role.ADMIN) {
            result.put("success", false);
            result.put("message", "无权限");
            return result;
        }
        try {
            Book savedBook = bookService.save(book);
            result.put("success", true);
            result.put("message", "添加成功");
            result.put("book", savedBook);
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "添加失败：" + e.getMessage());
        }
        return result;
    }

    @GetMapping("/admin/edit/{id}")
    public String editBookPage(@PathVariable Long id, HttpSession session, Model model) {
        User user = (User) session.getAttribute("user");
        if (user == null || user.getRole() != User.Role.ADMIN) {
            return "redirect:/dashboard";
        }
        Book book = bookService.findById(id)
                .orElseThrow(() -> new RuntimeException("图书不存在"));
        model.addAttribute("book", book);
        return "books/edit";
    }

    @PostMapping("/admin/edit")
    @ResponseBody
    public Map<String, Object> updateBook(@ModelAttribute Book book, HttpSession session) {
        Map<String, Object> result = new HashMap<>();
        User user = (User) session.getAttribute("user");
        if (user == null || user.getRole() != User.Role.ADMIN) {
            result.put("success", false);
            result.put("message", "无权限");
            return result;
        }
        try {
            Book updatedBook = bookService.update(book);
            result.put("success", true);
            result.put("message", "更新成功");
            result.put("book", updatedBook);
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "更新失败：" + e.getMessage());
        }
        return result;
    }

    @PostMapping("/admin/delete/{id}")
    @ResponseBody
    public Map<String, Object> deleteBook(@PathVariable Long id, HttpSession session) {
        Map<String, Object> result = new HashMap<>();
        User user = (User) session.getAttribute("user");
        if (user == null || user.getRole() != User.Role.ADMIN) {
            result.put("success", false);
            result.put("message", "无权限");
            return result;
        }
        try {
            bookService.deleteById(id);
            result.put("success", true);
            result.put("message", "删除成功");
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "删除失败：" + e.getMessage());
        }
        return result;
    }
}

