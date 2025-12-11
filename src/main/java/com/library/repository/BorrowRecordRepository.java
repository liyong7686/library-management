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
}

