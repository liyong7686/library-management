package com.library.repository;

import com.library.entity.BorrowRecord;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface BorrowRecordRepository extends JpaRepository<BorrowRecord, Long> {
    List<BorrowRecord> findByUserId(Long userId);
    Page<BorrowRecord> findByUserId(Long userId, Pageable pageable);
    List<BorrowRecord> findByBookId(Long bookId);
    List<BorrowRecord> findByUserIdAndStatus(Long userId, BorrowRecord.Status status);
    
    @Query("SELECT COUNT(br) FROM BorrowRecord br WHERE br.userId = :userId AND br.status = 'BORROWED'")
    Long countBorrowedBooksByUserId(@Param("userId") Long userId);
    
    Optional<BorrowRecord> findByUserIdAndBookIdAndStatus(Long userId, Long bookId, BorrowRecord.Status status);
    
    // 统计查询
    @Query("SELECT COUNT(br) FROM BorrowRecord br WHERE br.status = :status")
    Long countByStatus(@Param("status") BorrowRecord.Status status);
    
    @Query(value = "SELECT book_id, COUNT(*) as count FROM borrow_records GROUP BY book_id ORDER BY count DESC LIMIT :limit", nativeQuery = true)
    List<Object[]> findTopBorrowedBooks(@Param("limit") int limit);
    
    @Query(value = "SELECT user_id, COUNT(*) as count FROM borrow_records GROUP BY user_id ORDER BY count DESC LIMIT :limit", nativeQuery = true)
    List<Object[]> findTopActiveUsers(@Param("limit") int limit);
    
    @Query(value = "SELECT DATE(borrow_date) as date, COUNT(*) as count FROM borrow_records WHERE borrow_date >= :startDate GROUP BY DATE(borrow_date) ORDER BY date DESC", nativeQuery = true)
    List<Object[]> findBorrowCountByDate(@Param("startDate") java.time.LocalDateTime startDate);
    
    @Query(value = "SELECT YEAR(borrow_date) as year, MONTH(borrow_date) as month, COUNT(*) as count FROM borrow_records WHERE borrow_date >= :startDate GROUP BY YEAR(borrow_date), MONTH(borrow_date) ORDER BY year DESC, month DESC", nativeQuery = true)
    List<Object[]> findBorrowCountByMonth(@Param("startDate") java.time.LocalDateTime startDate);
}

