-- ============================================
-- 图书管理系统完整初始化脚本
-- 包含：数据库创建、表结构创建、数据初始化
-- 图书表：500+ 条数据
-- 借阅记录表：500+ 条数据
-- 用户表：100+ 条数据
-- ============================================
-- 
-- 重要说明：
-- 1. 执行此脚本前请确保MySQL服务已启动
-- 2. 所有用户的密码统一为：123456
--    管理员账户：admin / 123456
-- 3. 密码已使用BCrypt加密（强度10）
-- 4. 本脚本会先删除已存在的表，然后重新创建
-- 
-- ============================================

-- 创建数据库（如果不存在）
CREATE DATABASE IF NOT EXISTS library_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE library_db;

-- ============================================
-- 删除已存在的表（如果存在）
-- ============================================
DROP TABLE IF EXISTS borrow_records;
DROP TABLE IF EXISTS books;
DROP TABLE IF EXISTS users;

-- ============================================
-- 创建表结构
-- ============================================

-- 1. 用户表 (users)
CREATE TABLE users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    email VARCHAR(100) NOT NULL,
    real_name VARCHAR(50),
    phone VARCHAR(20),
    role VARCHAR(20) NOT NULL DEFAULT 'GUEST',
    created_at DATETIME,
    updated_at DATETIME,
    INDEX idx_username (username),
    INDEX idx_email (email)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 2. 图书表 (books)
CREATE TABLE books (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(200) NOT NULL,
    author VARCHAR(100),
    isbn VARCHAR(50),
    publisher VARCHAR(100),
    publish_date DATETIME,
    description TEXT,
    total_copies INT NOT NULL DEFAULT 0,
    available_copies INT NOT NULL DEFAULT 0,
    category VARCHAR(50),
    created_at DATETIME,
    updated_at DATETIME,
    INDEX idx_title (title(100)),
    INDEX idx_author (author(50)),
    INDEX idx_category (category),
    INDEX idx_isbn (isbn)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 3. 借阅记录表 (borrow_records)
CREATE TABLE borrow_records (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    book_id BIGINT NOT NULL,
    borrow_date DATETIME NOT NULL,
    return_date DATETIME,
    due_date DATETIME NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'BORROWED',
    created_at DATETIME,
    updated_at DATETIME,
    INDEX idx_user_id (user_id),
    INDEX idx_book_id (book_id),
    INDEX idx_status (status),
    INDEX idx_borrow_date (borrow_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================
-- 删除已存在的存储过程（如果存在）
-- ============================================
DROP PROCEDURE IF EXISTS generate_users;
DROP PROCEDURE IF EXISTS generate_books;
DROP PROCEDURE IF EXISTS generate_borrow_records;

-- ============================================
-- 1. 用户表 (users) - 100条数据
-- ============================================
-- 密码统一为: 123456 (BCrypt加密后的值: $2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iwK8pJNK)

-- 先插入管理员和基础用户
INSERT INTO users (username, password, email, real_name, phone, role, created_at, updated_at) VALUES
-- 管理员
('admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iwK8pJNK', 'admin@library.com', '系统管理员', '13800000001', 'ADMIN', '2024-01-01 10:00:00', '2024-01-01 10:00:00'),
('manager', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iwK8pJNK', 'manager@library.com', '图书管理员', '13800000002', 'ADMIN', '2024-01-02 10:00:00', '2024-01-02 10:00:00'),
-- 访客用户
('guest001', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iwK8pJNK', 'guest001@example.com', '访客001', '13900001001', 'GUEST', '2024-01-14 10:00:00', '2024-01-14 10:00:00'),
('guest002', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iwK8pJNK', 'guest002@example.com', '访客002', '13900001002', 'GUEST', '2024-01-15 10:00:00', '2024-01-15 10:00:00'),
('guest003', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iwK8pJNK', 'guest003@example.com', '访客003', '13900001003', 'GUEST', '2024-01-16 10:00:00', '2024-01-16 10:00:00'),
('guest004', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iwK8pJNK', 'guest004@example.com', '访客004', '13900001004', 'GUEST', '2024-01-17 10:00:00', '2024-01-17 10:00:00'),
('guest005', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iwK8pJNK', 'guest005@example.com', '访客005', '13900001005', 'GUEST', '2024-01-18 10:00:00', '2024-01-18 10:00:00'),
('guest006', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iwK8pJNK', 'guest006@example.com', '访客006', '13900001006', 'GUEST', '2024-01-19 10:00:00', '2024-01-19 10:00:00'),
('guest007', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iwK8pJNK', 'guest007@example.com', '访客007', '13900001007', 'GUEST', '2024-01-20 10:00:00', '2024-01-20 10:00:00'),
('guest008', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iwK8pJNK', 'guest008@example.com', '访客008', '13900001008', 'GUEST', '2024-01-21 10:00:00', '2024-01-21 10:00:00');

-- 创建存储过程生成90个普通用户
DELIMITER //
CREATE PROCEDURE generate_users()
BEGIN
    DECLARE i INT DEFAULT 1;
    WHILE i <= 90 DO
        INSERT INTO users (username, password, email, real_name, phone, role, created_at, updated_at) VALUES
        (CONCAT('user', LPAD(i, 3, '0')), 
         '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iwK8pJNK',
         CONCAT('user', LPAD(i, 3, '0'), '@example.com'),
         CONCAT('用户', i),
         CONCAT('1380000', LPAD(1000 + i, 4, '0')),
         'USER',
         DATE_ADD('2024-01-01 10:00:00', INTERVAL i DAY),
         DATE_ADD('2024-01-01 10:00:00', INTERVAL i DAY));
        SET i = i + 1;
    END WHILE;
END //
DELIMITER ;

-- 执行存储过程生成用户
CALL generate_users();

-- 删除存储过程
DROP PROCEDURE generate_users;

-- ============================================
-- 2. 图书表 (books) - 500条数据
-- ============================================

-- 创建存储过程生成500本图书
DELIMITER //
CREATE PROCEDURE generate_books()
BEGIN
    DECLARE i INT DEFAULT 1;
    DECLARE category_name VARCHAR(50);
    DECLARE publisher_name VARCHAR(100);
    DECLARE total_copies INT;
    DECLARE available_copies INT;
    
    WHILE i <= 500 DO
        -- 根据索引选择分类（20个分类循环）
        SET category_name = CASE (i - 1) % 20
            WHEN 0 THEN '计算机科学'
            WHEN 1 THEN '文学'
            WHEN 2 THEN '历史'
            WHEN 3 THEN '科普'
            WHEN 4 THEN '经济学'
            WHEN 5 THEN '心理学'
            WHEN 6 THEN '数据库'
            WHEN 7 THEN '前端开发'
            WHEN 8 THEN '操作系统'
            WHEN 9 THEN '数学'
            WHEN 10 THEN '物理'
            WHEN 11 THEN '化学'
            WHEN 12 THEN '生物'
            WHEN 13 THEN '医学'
            WHEN 14 THEN '艺术'
            WHEN 15 THEN '音乐'
            WHEN 16 THEN '体育'
            WHEN 17 THEN '教育'
            WHEN 18 THEN '法律'
            ELSE '哲学'
        END;
        
        -- 根据索引选择出版社（10个出版社循环）
        SET publisher_name = CASE (i - 1) % 10
            WHEN 0 THEN '机械工业出版社'
            WHEN 1 THEN '人民邮电出版社'
            WHEN 2 THEN '人民文学出版社'
            WHEN 3 THEN '中信出版社'
            WHEN 4 THEN '商务印书馆'
            WHEN 5 THEN '中国人民大学出版社'
            WHEN 6 THEN '清华大学出版社'
            WHEN 7 THEN '北京大学出版社'
            WHEN 8 THEN '电子工业出版社'
            ELSE '科学出版社'
        END;
        
        SET total_copies = 10 + (i % 20);
        SET available_copies = total_copies - (i % 5);
        
        INSERT INTO books (title, author, isbn, publisher, publish_date, description, total_copies, available_copies, category, created_at, updated_at) VALUES
        (CONCAT('图书', i, '：深入理解'),
         CONCAT('作者', i),
         CONCAT('978-7-111-', LPAD(i, 5, '0')),
         publisher_name,
         DATE_ADD('2020-01-01', INTERVAL (i % 1460) DAY),
         CONCAT('这是第', i, '本图书的详细描述。本书内容丰富，涵盖了相关领域的核心知识点，适合广大读者阅读学习。本书从基础理论出发，深入浅出地介绍了相关概念和实践方法。'),
         total_copies,
         available_copies,
         category_name,
         DATE_ADD('2024-01-01 10:00:00', INTERVAL (i % 365) DAY),
         DATE_ADD('2024-01-01 10:00:00', INTERVAL (i % 365) DAY));
        
        SET i = i + 1;
    END WHILE;
END //
DELIMITER ;

-- 执行存储过程生成图书
CALL generate_books();

-- 删除存储过程
DROP PROCEDURE generate_books;

-- ============================================
-- 3. 借阅记录表 (borrow_records) - 500条数据
-- ============================================
-- 注意：假设users表ID从1开始，books表ID从1开始

-- 创建存储过程生成500条借阅记录
DELIMITER //
CREATE PROCEDURE generate_borrow_records()
BEGIN
    DECLARE i INT DEFAULT 1;
    DECLARE user_id INT;
    DECLARE book_id INT;
    DECLARE borrow_date DATETIME;
    DECLARE return_date DATETIME;
    DECLARE due_date DATETIME;
    DECLARE record_status VARCHAR(20);
    DECLARE user_count INT DEFAULT 92;  -- 2个管理员 + 90个普通用户
    DECLARE book_count INT DEFAULT 500;
    
    WHILE i <= 500 DO
        -- 用户ID：从1开始，循环使用（1-92）
        SET user_id = 1 + ((i - 1) % user_count);
        -- 图书ID：从1开始，循环使用（1-500）
        SET book_id = 1 + ((i - 1) % book_count);
        -- 借阅日期：分布在2024年全年
        SET borrow_date = DATE_ADD('2024-01-01', INTERVAL (i % 365) DAY);
        -- 应还日期：借阅日期+30天
        SET due_date = DATE_ADD(borrow_date, INTERVAL 30 DAY);
        
        -- 根据记录序号决定状态：0=已归还，1=借阅中，2=逾期
        IF (i % 3) = 0 THEN
            -- 已归还
            SET record_status = 'RETURNED';
            SET return_date = DATE_ADD(borrow_date, INTERVAL 15 DAY);
        ELSEIF (i % 3) = 1 THEN
            -- 借阅中
            SET record_status = 'BORROWED';
            SET return_date = NULL;
        ELSE
            -- 逾期（借阅日期较早，已超过应还日期）
            SET record_status = 'OVERDUE';
            SET return_date = NULL;
            SET borrow_date = DATE_SUB('2024-12-31', INTERVAL (i % 100 + 50) DAY);
            SET due_date = DATE_ADD(borrow_date, INTERVAL 30 DAY);
        END IF;
        
        INSERT INTO borrow_records (user_id, book_id, borrow_date, return_date, due_date, status, created_at, updated_at) VALUES
        (user_id,
         book_id,
         borrow_date,
         return_date,
         due_date,
         record_status,
         borrow_date,
         COALESCE(return_date, borrow_date));
        
        SET i = i + 1;
    END WHILE;
END //
DELIMITER ;

-- 执行存储过程生成借阅记录
CALL generate_borrow_records();

-- 删除存储过程
DROP PROCEDURE generate_borrow_records;

-- ============================================
-- 数据说明
-- ============================================
-- 1. 用户表：包含2个管理员、90个普通用户、8个访客，共100个用户
--    所有用户密码统一为：123456（已使用BCrypt加密）
-- 
-- 2. 图书表：包含500本图书，涵盖20个不同分类
--    每本书都有合理的总册数（10-30本）和可借册数
-- 
-- 3. 借阅记录表：包含500条借阅记录
--    - 约167条已归还记录
--    - 约167条借阅中记录
--    - 约166条逾期记录
--    记录分布在不同的用户和图书之间
-- 
-- 4. 时间说明：
--    - 借阅期限为30天
--    - 借阅日期分布在2024年全年
--    - 已归还记录：借阅后15天归还
--    - 借阅中记录：当前正在借阅
--    - 逾期记录：已超过应还日期
-- 
-- ============================================

