# 学生运动测评系统

基于 Spring Boot 的学生运动测评管理系统，提供学生运动记录、积分管理、徽章奖励、班级排名等功能。

## 技术栈

- **框架**: Spring Boot 2.7.6
- **数据库**: MySQL 8.0 + H2（开发环境）
- **ORM**: MyBatis Plus 3.5.3.1
- **前端**: Thymeleaf + HTML5 + CSS3 + JavaScript
- **安全**: Spring Security
- **缓存**: Redis
- **Excel处理**: Apache POI 5.2.3
- **构建工具**: Maven

## 功能模块

### 学生模块
- 运动记录管理（添加、查看、统计）
- 积分查询与兑换
- 徽章收集
- 运动目标设置
- 运动排名查看
- 健康知识浏览

### 教师模块
- 班级学生管理
- 学生运动统计
- 班级排名查看
- 学生评价管理

### 管理员模块
- 用户管理（学生、教师）
- 班级管理
- 运动类型管理
- 积分规则配置
- 徽章管理
- 公告管理
- 数据导入导出
- 操作日志查看
- 系统监控

## 项目结构

```
src/main/java/com/hhjt/
├── annotation/          # 自定义注解
├── aspect/              # AOP切面
├── config/              # 配置类
├── controller/          # 控制器
├── dto/                 # 数据传输对象
├── entity/              # 实体类
├── mapper/              # MyBatis Mapper接口
├── service/             # 业务逻辑层
└── Demo01Application.java  # 启动类
```

## 快速开始

### 环境要求

- JDK 1.8+
- Maven 3.6+
- MySQL 8.0+（生产环境）
- Redis 6.0+（可选）

### 配置说明

1. **开发环境**：使用 H2 内存数据库，无需额外配置
2. **生产环境**：配置 MySQL 数据源

修改 `application.yml` 配置数据库连接：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/example_db?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true
    username: admin
    password: password
    driver-class-name: com.mysql.cj.jdbc.Driver
```

### 启动方式

**开发模式运行**：
```bash
mvn spring-boot:run
```

**打包构建**：
```bash
mvn clean package
```

**运行打包后的 Jar**：
```bash
java -jar target/sport-evaluation-system-0.0.1-SNAPSHOT.jar
```

### 访问地址

- 首页：http://localhost:8080
- H2 控制台（开发环境）：http://localhost:8080/h2-console

## 初始账号

| 角色 | 用户名 | 密码 | 说明 |
| :--- | :--- | :--- | :--- |
| 管理员 | admin | admin123 | 系统管理员 |
| 教师 | teacher | teacher123 | 教师账号 |
| 学生 | student | student123 | 学生账号 |

## 数据库表结构

核心数据表：

| 表名 | 说明 |
| :--- | :--- |
| `user` | 用户基础信息 |
| `student` | 学生信息 |
| `teacher` | 教师信息 |
| `sys_class` | 班级信息 |
| `sport_record` | 运动记录 |
| `student_points` | 积分记录 |
| `achievement_badge` | 成就徽章 |
| `student_badge` | 学生徽章关联 |
| `points_rule` | 积分规则 |
| `announcement` | 公告信息 |
| `operation_log` | 操作日志 |

## 主要功能接口

### 学生运动记录
- `POST /student/sport/record` - 添加运动记录
- `GET /student/sport/records` - 获取运动记录列表
- `GET /student/sport/statistics` - 获取运动统计

### 积分管理
- `GET /student/points` - 查询积分
- `POST /student/exchange` - 积分兑换
- `GET /admin/points/rule` - 获取积分规则

### 排名系统
- `GET /sport/ranking/class` - 班级排名
- `GET /sport/ranking/school` - 校级排名

### 数据管理
- `POST /admin/data/import` - 导入数据
- `GET /admin/data/export` - 导出数据
- `POST /admin/data/backup` - 数据备份

## 开发说明

### 代码规范

1. 遵循 Spring Boot 编码规范
2. 使用 Lombok 简化代码
3. Controller 层负责参数校验和响应封装
4. Service 层处理业务逻辑
5. Mapper 层使用 MyBatis Plus 注解

### 日志配置

日志输出目录：`logs/application.log`

支持按日期切割日志，保留历史日志文件。

## 许可证

MIT License

## 作者

zzz307666
