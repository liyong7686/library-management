package com.library.service;

import com.library.entity.Book;
import com.library.entity.BorrowRecord;
import com.library.entity.User;
import com.library.repository.BookRepository;
import com.library.repository.BorrowRecordRepository;
import com.library.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class StatisticsService {
    
    @Autowired
    private BorrowRecordRepository borrowRecordRepository;
    
    @Autowired
    private BookRepository bookRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    /**
     * 获取总体统计信息
     */
    public Map<String, Object> getOverallStatistics() {
        Map<String, Object> stats = new HashMap<>();
        
        // 总借阅数量
        long totalBorrows = borrowRecordRepository.count();
        stats.put("totalBorrows", totalBorrows);
        
        // 当前借阅中数量
        long borrowedCount = borrowRecordRepository.countByStatus(BorrowRecord.Status.BORROWED);
        stats.put("borrowedCount", borrowedCount);
        
        // 已归还数量
        long returnedCount = borrowRecordRepository.countByStatus(BorrowRecord.Status.RETURNED);
        stats.put("returnedCount", returnedCount);
        
        // 逾期数量
        long overdueCount = borrowRecordRepository.countByStatus(BorrowRecord.Status.OVERDUE);
        stats.put("overdueCount", overdueCount);
        
        // 归还率
        double returnRate = totalBorrows > 0 ? (double) returnedCount / totalBorrows * 100 : 0;
        stats.put("returnRate", Math.round(returnRate * 100.0) / 100.0);
        
        return stats;
    }
    
    /**
     * 获取最受欢迎的图书（借阅次数最多）
     */
    public List<Map<String, Object>> getTopBorrowedBooks(int limit) {
        List<Object[]> results = borrowRecordRepository.findTopBorrowedBooks(limit);
        List<Map<String, Object>> topBooks = new ArrayList<>();
        
        for (Object[] result : results) {
            Object bookIdObj = result[0];
            Object countObj = result[1];
            
            Long bookId = null;
            Long count = null;
            
            if (bookIdObj instanceof Number) {
                bookId = ((Number) bookIdObj).longValue();
            } else if (bookIdObj != null) {
                bookId = Long.parseLong(bookIdObj.toString());
            }
            
            if (countObj instanceof Number) {
                count = ((Number) countObj).longValue();
            } else if (countObj != null) {
                count = Long.parseLong(countObj.toString());
            }
            
            if (bookId != null && count != null) {
                Optional<Book> bookOpt = bookRepository.findById(bookId);
                if (bookOpt.isPresent()) {
                    Book book = bookOpt.get();
                    Map<String, Object> bookStat = new HashMap<>();
                    bookStat.put("bookId", bookId);
                    bookStat.put("title", book.getTitle());
                    bookStat.put("author", book.getAuthor());
                    bookStat.put("category", book.getCategory());
                    bookStat.put("borrowCount", count);
                    topBooks.add(bookStat);
                }
            }
        }
        
        return topBooks;
    }
    
    /**
     * 获取最活跃的用户（借阅次数最多）
     */
    public List<Map<String, Object>> getTopActiveUsers(int limit) {
        List<Object[]> results = borrowRecordRepository.findTopActiveUsers(limit);
        List<Map<String, Object>> topUsers = new ArrayList<>();
        
        for (Object[] result : results) {
            Object userIdObj = result[0];
            Object countObj = result[1];
            
            Long userId = null;
            Long count = null;
            
            if (userIdObj instanceof Number) {
                userId = ((Number) userIdObj).longValue();
            } else if (userIdObj != null) {
                userId = Long.parseLong(userIdObj.toString());
            }
            
            if (countObj instanceof Number) {
                count = ((Number) countObj).longValue();
            } else if (countObj != null) {
                count = Long.parseLong(countObj.toString());
            }
            
            if (userId != null && count != null) {
                Optional<User> userOpt = userRepository.findById(userId);
                if (userOpt.isPresent()) {
                    User user = userOpt.get();
                    Map<String, Object> userStat = new HashMap<>();
                    userStat.put("userId", userId);
                    userStat.put("username", user.getUsername());
                    userStat.put("realName", user.getRealName());
                    userStat.put("email", user.getEmail());
                    userStat.put("borrowCount", count);
                    topUsers.add(userStat);
                }
            }
        }
        
        return topUsers;
    }
    
    /**
     * 按日期统计借阅数量（最近30天）
     */
    public List<Map<String, Object>> getBorrowStatisticsByDate(int days) {
        LocalDateTime startDate = LocalDateTime.now().minusDays(days);
        List<Object[]> results = borrowRecordRepository.findBorrowCountByDate(startDate);
        
        // 创建日期到数量的映射
        Map<String, Long> dateCountMap = new HashMap<>();
        for (Object[] result : results) {
            String dateStr = null;
            Long count = 0L;
            
            if (result[0] != null) {
                // 处理日期格式，确保是 YYYY-MM-DD 格式
                Object dateObj = result[0];
                if (dateObj instanceof java.sql.Date) {
                    dateStr = dateObj.toString();
                } else if (dateObj instanceof java.util.Date) {
                    java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd");
                    dateStr = sdf.format((java.util.Date) dateObj);
                } else {
                    dateStr = dateObj.toString();
                    // 如果包含时间部分，只取日期部分
                    if (dateStr.contains(" ")) {
                        dateStr = dateStr.split(" ")[0];
                    }
                    if (dateStr.contains("T")) {
                        dateStr = dateStr.split("T")[0];
                    }
                }
            }
            
            if (result[1] instanceof Number) {
                count = ((Number) result[1]).longValue();
            } else if (result[1] != null) {
                try {
                    count = Long.parseLong(result[1].toString());
                } catch (NumberFormatException e) {
                    count = 0L;
                }
            }
            
            if (dateStr != null && !dateStr.isEmpty()) {
                dateCountMap.put(dateStr, count);
            }
        }
        
        // 填充所有日期（包括没有借阅记录的日期）
        List<Map<String, Object>> dateStats = new ArrayList<>();
        LocalDateTime current = LocalDateTime.now().minusDays(days - 1);
        LocalDateTime end = LocalDateTime.now();
        
        while (!current.isAfter(end)) {
            String dateKey = current.toLocalDate().toString();
            Map<String, Object> stat = new HashMap<>();
            stat.put("date", dateKey);
            stat.put("count", dateCountMap.getOrDefault(dateKey, 0L));
            dateStats.add(stat);
            current = current.plusDays(1);
        }
        
        return dateStats;
    }
    
    /**
     * 按月份统计借阅数量（最近12个月）
     */
    public List<Map<String, Object>> getBorrowStatisticsByMonth(int months) {
        LocalDateTime startDate = LocalDateTime.now().minusMonths(months);
        List<Object[]> results = borrowRecordRepository.findBorrowCountByMonth(startDate);
        List<Map<String, Object>> monthStats = new ArrayList<>();
        
        for (Object[] result : results) {
            Map<String, Object> stat = new HashMap<>();
            int year = 0;
            int month = 0;
            long count = 0;
            
            if (result[0] instanceof Number) {
                year = ((Number) result[0]).intValue();
            } else if (result[0] != null) {
                year = Integer.parseInt(result[0].toString());
            }
            
            if (result[1] instanceof Number) {
                month = ((Number) result[1]).intValue();
            } else if (result[1] != null) {
                month = Integer.parseInt(result[1].toString());
            }
            
            if (result[2] instanceof Number) {
                count = ((Number) result[2]).longValue();
            } else if (result[2] != null) {
                count = Long.parseLong(result[2].toString());
            }
            
            stat.put("year", year);
            stat.put("month", month);
            stat.put("monthLabel", year + "-" + String.format("%02d", month));
            stat.put("count", count);
            monthStats.add(stat);
        }
        
        return monthStats;
    }
    
    /**
     * 按状态统计借阅分布
     */
    public Map<String, Long> getBorrowStatisticsByStatus() {
        Map<String, Long> statusStats = new HashMap<>();
        statusStats.put("BORROWED", borrowRecordRepository.countByStatus(BorrowRecord.Status.BORROWED));
        statusStats.put("RETURNED", borrowRecordRepository.countByStatus(BorrowRecord.Status.RETURNED));
        statusStats.put("OVERDUE", borrowRecordRepository.countByStatus(BorrowRecord.Status.OVERDUE));
        return statusStats;
    }
    
    /**
     * 按图书分类统计借阅数量
     */
    public List<Map<String, Object>> getBorrowStatisticsByCategory() {
        List<BorrowRecord> allRecords = borrowRecordRepository.findAll();
        Map<String, Long> categoryCount = new HashMap<>();
        
        for (BorrowRecord record : allRecords) {
            Optional<Book> bookOpt = bookRepository.findById(record.getBookId());
            if (bookOpt.isPresent()) {
                String category = bookOpt.get().getCategory();
                if (category != null && !category.isEmpty()) {
                    categoryCount.put(category, categoryCount.getOrDefault(category, 0L) + 1);
                }
            }
        }
        
        List<Map<String, Object>> categoryStats = new ArrayList<>();
        categoryCount.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .forEach(entry -> {
                    Map<String, Object> stat = new HashMap<>();
                    stat.put("category", entry.getKey());
                    stat.put("count", entry.getValue());
                    categoryStats.add(stat);
                });
        
        return categoryStats;
    }
    
    /**
     * 获取完整的统计报表数据
     */
    public Map<String, Object> getFullStatisticsReport() {
        Map<String, Object> report = new HashMap<>();
        
        // 总体统计
        report.put("overall", getOverallStatistics());
        
        // 最受欢迎的图书（前10）
        report.put("topBooks", getTopBorrowedBooks(10));
        
        // 最活跃的用户（前10）
        report.put("topUsers", getTopActiveUsers(10));
        
        // 按状态统计
        report.put("byStatus", getBorrowStatisticsByStatus());
        
        // 按分类统计
        report.put("byCategory", getBorrowStatisticsByCategory());
        
        // 按日期统计（最近30天）
        report.put("byDate", getBorrowStatisticsByDate(30));
        
        // 按月份统计（最近12个月）
        report.put("byMonth", getBorrowStatisticsByMonth(12));
        
        return report;
    }
}
