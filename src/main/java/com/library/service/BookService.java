package com.library.service;

import com.library.entity.Book;
import com.library.repository.BookRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class BookService {
    @Autowired
    private BookRepository bookRepository;

    public Optional<Book> findById(Long id) {
        return bookRepository.findById(id);
    }
    
    /**
     * 获取图书（带缓存）
     * 只缓存非空的 Book 对象，避免 Optional 序列化问题
     * 
     * 注意：如果遇到 "ERR wrong number of arguments for 'set' command" 错误，
     * 可能是 Redis 版本过低（需要 3.0+），请升级 Redis 或暂时禁用缓存
     */
    @Cacheable(value = "books", key = "#id", unless = "#result == null")
    public Book getBookById(Long id) {
        return bookRepository.findById(id).orElse(null);
    }

    public List<Book> findAll() {
        return bookRepository.findAll();
    }

    public Page<Book> findAll(Pageable pageable) {
        return bookRepository.findAll(pageable);
    }

    public List<Book> searchBooks(String keyword) {
        return bookRepository.searchBooks(keyword);
    }

    public List<Book> findByTitle(String title) {
        return bookRepository.findByTitleContaining(title);
    }

    public List<Book> findByAuthor(String author) {
        return bookRepository.findByAuthorContaining(author);
    }

    public List<Book> findByCategory(String category) {
        return bookRepository.findByCategory(category);
    }

    @CacheEvict(value = "books", key = "#book.id")
    @Transactional
    public Book save(Book book) {
        if (book.getAvailableCopies() == null) {
            book.setAvailableCopies(book.getTotalCopies());
        }
        Book savedBook = bookRepository.save(book);
        return savedBook;
    }

    @CacheEvict(value = "books", key = "#id")
    @Transactional
    public void deleteById(Long id) {
        bookRepository.deleteById(id);
    }

    @CacheEvict(value = "books", key = "#book.id")
    @Transactional
    public Book update(Book book) {
        Book existingBook = bookRepository.findById(book.getId())
                .orElseThrow(() -> new RuntimeException("图书不存在"));
        
        if (book.getTitle() != null) existingBook.setTitle(book.getTitle());
        if (book.getAuthor() != null) existingBook.setAuthor(book.getAuthor());
        if (book.getIsbn() != null) existingBook.setIsbn(book.getIsbn());
        if (book.getPublisher() != null) existingBook.setPublisher(book.getPublisher());
        if (book.getPublishDate() != null) existingBook.setPublishDate(book.getPublishDate());
        if (book.getDescription() != null) existingBook.setDescription(book.getDescription());
        if (book.getTotalCopies() != null) {
            int diff = book.getTotalCopies() - existingBook.getTotalCopies();
            existingBook.setTotalCopies(book.getTotalCopies());
            existingBook.setAvailableCopies(existingBook.getAvailableCopies() + diff);
        }
        if (book.getCategory() != null) existingBook.setCategory(book.getCategory());
        
        return bookRepository.save(existingBook);
    }
}

