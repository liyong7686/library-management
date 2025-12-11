package com.library.service;

import com.library.entity.Book;
import com.library.entity.BorrowRecord;
import com.library.entity.User;
import com.library.repository.BookRepository;
import com.library.repository.BorrowRecordRepository;
import com.library.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class BorrowService {
    @Autowired
    private BorrowRecordRepository borrowRecordRepository;

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private UserRepository userRepository;

    @Value("${library.borrow.max-days:30}")
    private int maxDays;

    @Value("${library.borrow.max-books:5}")
    private int maxBooks;

    @Transactional
    public Map<String, Object> borrowBook(Long userId, Long bookId) {
        Map<String, Object> result = new HashMap<>();
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
        
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new RuntimeException("图书不存在"));
        
        // 检查用户借阅数量
        Long borrowedCount = borrowRecordRepository.countBorrowedBooksByUserId(userId);
        if (borrowedCount >= maxBooks) {
            result.put("success", false);
            result.put("message", "已达到最大借阅数量：" + maxBooks);
            return result;
        }
        
        // 检查是否已借阅该书
        Optional<BorrowRecord> existingRecord = borrowRecordRepository
                .findByUserIdAndBookIdAndStatus(userId, bookId, BorrowRecord.Status.BORROWED);
        if (existingRecord.isPresent()) {
            result.put("success", false);
            result.put("message", "您已借阅该书");
            return result;
        }
        
        // 检查图书库存
        if (book.getAvailableCopies() <= 0) {
            result.put("success", false);
            result.put("message", "图书库存不足");
            return result;
        }
        
        // 创建借阅记录
        BorrowRecord record = new BorrowRecord();
        record.setUserId(userId);
        record.setBookId(bookId);
        record.setUser(user);  // 设置临时字段用于返回
        record.setBook(book);  // 设置临时字段用于返回
        record.setBorrowDate(LocalDateTime.now());
        record.setDueDate(LocalDateTime.now().plusDays(maxDays));
        record.setStatus(BorrowRecord.Status.BORROWED);
        
        borrowRecordRepository.save(record);
        
        // 更新图书库存
        book.setAvailableCopies(book.getAvailableCopies() - 1);
        bookRepository.save(book);
        
        result.put("success", true);
        result.put("message", "借阅成功");
        result.put("record", record);
        return result;
    }

    @Transactional
    public Map<String, Object> returnBook(Long recordId) {
        Map<String, Object> result = new HashMap<>();
        
        BorrowRecord record = borrowRecordRepository.findById(recordId)
                .orElseThrow(() -> new RuntimeException("借阅记录不存在"));
        
        if (record.getStatus() == BorrowRecord.Status.RETURNED) {
            result.put("success", false);
            result.put("message", "该图书已归还");
            return result;
        }
        
        record.setReturnDate(LocalDateTime.now());
        record.setStatus(BorrowRecord.Status.RETURNED);
        borrowRecordRepository.save(record);
        
        // 更新图书库存
        Book book = bookRepository.findById(record.getBookId())
                .orElseThrow(() -> new RuntimeException("图书不存在"));
        book.setAvailableCopies(book.getAvailableCopies() + 1);
        bookRepository.save(book);
        
        result.put("success", true);
        result.put("message", "归还成功");
        return result;
    }

    public List<BorrowRecord> getUserBorrowRecords(Long userId) {
        List<BorrowRecord> records = borrowRecordRepository.findByUserId(userId);
        // 填充关联对象信息
        fillAssociatedData(records);
        return records;
    }

    public Page<BorrowRecord> getUserBorrowRecords(Long userId, Pageable pageable) {
        Page<BorrowRecord> recordPage = borrowRecordRepository.findByUserId(userId, pageable);
        // 填充关联对象信息
        fillAssociatedData(recordPage.getContent());
        return recordPage;
    }

    public List<BorrowRecord> getUserBorrowingRecords(Long userId) {
        List<BorrowRecord> records = borrowRecordRepository.findByUserIdAndStatus(userId, BorrowRecord.Status.BORROWED);
        // 填充关联对象信息
        fillAssociatedData(records);
        return records;
    }

    public List<BorrowRecord> getAllBorrowRecords() {
        List<BorrowRecord> records = borrowRecordRepository.findAll();
        // 填充关联对象信息
        fillAssociatedData(records);
        return records;
    }

    public Page<BorrowRecord> getAllBorrowRecords(Pageable pageable) {
        Page<BorrowRecord> recordPage = borrowRecordRepository.findAll(pageable);
        // 填充关联对象信息
        fillAssociatedData(recordPage.getContent());
        return recordPage;
    }

    /**
     * 填充借阅记录的关联对象信息（User和Book）
     */
    private void fillAssociatedData(List<BorrowRecord> records) {
        for (BorrowRecord record : records) {
            if (record.getUserId() != null) {
                userRepository.findById(record.getUserId()).ifPresent(record::setUser);
            }
            if (record.getBookId() != null) {
                bookRepository.findById(record.getBookId()).ifPresent(record::setBook);
            }
        }
    }

    public Optional<BorrowRecord> findById(Long id) {
        Optional<BorrowRecord> recordOpt = borrowRecordRepository.findById(id);
        if (recordOpt.isPresent()) {
            BorrowRecord record = recordOpt.get();
            // 填充关联对象信息
            if (record.getUserId() != null) {
                userRepository.findById(record.getUserId()).ifPresent(record::setUser);
            }
            if (record.getBookId() != null) {
                bookRepository.findById(record.getBookId()).ifPresent(record::setBook);
            }
        }
        return recordOpt;
    }

    @Transactional
    public void checkOverdueRecords() {
        List<BorrowRecord> borrowedRecords = borrowRecordRepository
                .findAll()
                .stream()
                .filter(r -> r.getStatus() == BorrowRecord.Status.BORROWED)
                .filter(r -> r.getDueDate().isBefore(LocalDateTime.now()))
                .collect(Collectors.toList());
        
        for (BorrowRecord record : borrowedRecords) {
            record.setStatus(BorrowRecord.Status.OVERDUE);
            borrowRecordRepository.save(record);
        }
    }
}

