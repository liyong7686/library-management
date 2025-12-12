# 图书管理系统

基于 Spring Boot 的图书管理系统，支持三种角色：访客、普通用户、管理员。

## 技术栈

- **后端**: Spring Boot 3.2.0
- **数据库**: MySQL 8.0
- **缓存**: Redis 3.0.504
- **前端**: Thymeleaf + HTML + CSS + JavaScript
- **JDK**: 21

## 功能特性

### 1. 用户认证
- ✅ 用户注册
- ✅ 用户登录（支持三种角色）
- ✅ 找回密码（邮箱验证码）
- ✅ 角色管理（访客、普通用户、管理员）
- ✅ 个人资料管理（查看、编辑个人信息、修改密码）

### 2. 图书管理
- ✅ 图书列表浏览（访客可访问）
- ✅ 图书列表分页显示（默认每页10条，支持10/20/50/100条选择）
- ✅ 图书详情查看（访客可访问）
- ✅ 图书搜索（按书名、作者、ISBN）
- ✅ 图书分类筛选
- ✅ 图书添加（管理员）
- ✅ 图书编辑（管理员）
- ✅ 图书删除（管理员）
- ✅ 图书管理列表分页显示（管理员）

### 3. 借阅管理
- ✅ 图书借阅（普通用户和管理员）
- ✅ 图书归还（普通用户和管理员）
- ✅ 我的借阅记录查看（支持分页，默认每页10条）
- ✅ 借阅记录管理（管理员，支持分页，默认每页10条）
- ✅ 逾期检查

### 4. 统计报表（管理员）
- ✅ 总体统计（总借阅数、当前借阅中、已归还、逾期数量、归还率）
- ✅ 借阅状态分布图表（饼图）
- ✅ 图书分类借阅统计（柱状图）
- ✅ 最近30天借阅趋势（折线图）
- ✅ 最近12个月借阅趋势（柱状图）
- ✅ 最受欢迎的图书排行（Top 10）
- ✅ 最活跃的用户排行（Top 10）

### 5. 个人资料管理
- ✅ 查看个人资料（用户名、邮箱、真实姓名、手机号、角色、注册时间）
- ✅ 编辑个人资料（邮箱、真实姓名、手机号）
- ✅ 修改密码（需要验证原密码）
- ✅ 用户名不可修改（系统唯一标识）

## 角色权限

### 访客（GUEST）
- 浏览图书列表
- 查看图书详情
- 需要注册/登录才能借阅

### 普通用户（USER）
- 所有访客权限
- 借阅图书
- 查看自己的借阅记录
- 归还图书
- 管理个人资料

### 管理员（ADMIN）
- 所有普通用户权限
- 图书管理（增删改查）
- 查看所有借阅记录
- 管理所有借阅记录
- 查看借阅统计报表
- 管理个人资料

## 数据库配置

### MySQL 配置
在 `application.yml` 中配置数据库连接：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/library_db?useUnicode=true&characterEncoding=utf8&useSSL=false&serverTimezone=Asia/Shanghai
    username: root
    password: root
```

### Redis 配置
```yaml
spring:
  redis:
    host: localhost
    port: 6379
    password: 123456  # Redis 密码
    database: 0
```

**Redis 版本**: 3.0.504

**Redis 启动方式**:
- Windows: 使用 `RedisStart.bat` 脚本启动 Redis 服务器
- 脚本位置: `D:\redis\3.0.504\Redis-x64-3.0.504\RedisStart.bat`
- Redis 配置文件: `redis.windows.conf`
- Redis 密码: 123456（在配置文件中已设置）

## 运行步骤

1. **创建数据库并初始化数据**
   ```sql
   -- 方式1：使用完整初始化脚本（推荐）
   mysql -u root -p < init_complete.sql
   
   -- 方式2：手动创建数据库
   CREATE DATABASE library_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
   ```
   
   **初始化脚本说明**:
   - `init_complete.sql`: 完整初始化脚本，包含表结构创建和数据初始化
     - 用户表：100条数据（2个管理员 + 90个普通用户 + 8个访客）
     - 图书表：500条数据
     - 借阅记录表：500条数据
   - 所有用户密码统一为：**123456**
   - 管理员账户：**admin / 123456**

2. **启动 Redis**
   ```bash
   # Windows: 使用批处理脚本启动
   D:\redis\3.0.504\Redis-x64-3.0.504\RedisStart.bat
   
   # Linux/Mac: 使用命令行启动
   redis-server
   ```
   
   **Redis 管理工具**:
   - `ClearBooksCache.bat`: 清除 books 缓存的批处理脚本
   - 位置: `D:\redis\3.0.504\Redis-x64-3.0.504\ClearBooksCache.bat`

3. **配置数据库和Redis**
   修改 `application.yml` 中的数据库和Redis连接信息

4. **运行应用**
   ```bash
   mvn spring-boot:run
   ```

5. **访问系统**
   打开浏览器访问：`http://localhost:8080/library`

## 默认配置

- 服务器端口：8080
- 上下文路径：/library
- JWT过期时间：24小时
- 最大借阅天数：30天
- 最大借阅数量：5本
- Redis 缓存过期时间：1小时
- 列表分页默认大小：10条/页
- 列表分页可选大小：10、20、50、100条/页

## 项目结构

```
library-management/
├── src/
│   ├── main/
│   │   ├── java/com/library/
│   │   │   ├── config/          # 配置类
│   │   │   ├── controller/      # 控制器
│   │   │   ├── entity/          # 实体类
│   │   │   ├── interceptor/     # 拦截器
│   │   │   ├── repository/      # 数据访问层
│   │   │   ├── service/         # 业务逻辑层
│   │   │   └── util/            # 工具类
│   │   └── resources/
│   │       ├── static/          # 静态资源
│   │       │   ├── css/
│   │       │   └── js/
│   │       ├── templates/       # 模板文件
│   │       └── application.yml # 配置文件
│   └── test/
├── init_complete.sql            # 数据库完整初始化脚本
└── pom.xml
```

## 注意事项

1. **找回密码功能**：当前版本在开发环境下会在响应中返回验证码，生产环境需要配置邮件服务
2. **数据库表**：使用 JPA 自动创建表结构（`ddl-auto: update`），或使用 `init_complete.sql` 脚本初始化
3. **缓存**：图书信息使用 Redis 缓存，提高查询性能
   - 缓存序列化方式：GenericJackson2JsonRedisSerializer（自动包含类型信息）
   - 缓存键格式：`books::<bookId>`
   - 缓存过期时间：1小时
4. **安全性**：密码使用 BCrypt 加密存储
5. **分页功能**：所有列表页面均支持分页
   - 我的借阅：`/borrow?page=0&size=10`
   - 借阅管理：`/borrow/admin?page=0&size=10`
   - 图书管理：`/books/admin?page=0&size=10`
   - 图书列表：`/books?page=0&size=10`

## 开发说明

- 使用 Spring Security 进行权限控制
- 使用 JWT 进行身份认证
- 使用 Redis 3.0.504 存储验证码和缓存数据
- 使用 Thymeleaf 作为模板引擎
- 使用 JPA/Hibernate 进行数据持久化
- 使用 Spring Data JPA 的 Pageable 实现分页功能
- 使用 GenericJackson2JsonRedisSerializer 进行缓存序列化，支持类型信息

## 分页功能说明

系统所有列表页面均支持分页功能：

### 支持的页面
1. **我的借阅** (`/borrow`)
   - 显示当前用户的借阅记录
   - 支持分页和分页大小选择

2. **借阅管理** (`/borrow/admin`)
   - 管理员查看所有借阅记录
   - 支持分页和分页大小选择

3. **图书管理** (`/books/admin`)
   - 管理员管理所有图书
   - 支持分页和分页大小选择

4. **图书列表** (`/books`)
   - 所有用户可浏览的图书列表
   - 支持分页和分页大小选择

### 分页参数
- `page`: 页码（从0开始，默认0）
- `size`: 每页显示数量（默认10，可选：10、20、50、100）

### 分页特性
- 默认每页显示10条记录
- 支持下拉选择分页大小：10、20、50、100
- 显示当前页、总页数、总记录数
- 支持上一页/下一页导航

## 后续优化建议

1. 添加邮件服务，实现真正的邮件验证码发送
2. 添加图书封面图片上传功能
3. 添加图书评价功能
4. 优化前端UI，使用现代化框架（如Vue.js或React）
5. 添加数据导出功能（Excel、PDF）
6. 添加更多统计维度（按时间段筛选、自定义报表）
