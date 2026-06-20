# 运动评估系统 - 快速启动指南

## 🚀 5分钟快速启动

### 第一步：执行数据库脚本（必须）

在MySQL中执行扩展SQL脚本：

```bash
# 方法1：命令行执行
mysql -u root -p sport_evaluation < src/main/resources/sql/sport_evaluation_extend.sql

# 方法2：Navicat等工具
# 打开 src/main/resources/sql/sport_evaluation_extend.sql
# 直接运行整个脚本
```

**验证表是否创建成功：**
```sql
SHOW TABLES LIKE 'sport_%';
SHOW TABLES LIKE 'student_%';
SHOW TABLES LIKE 'evaluation_%';
```

应该看到9个新表。

---

### 第二步：启动应用

```bash
# 在项目根目录执行
mvn spring-boot:run
```

等待看到以下日志表示启动成功：
```
Started Demo01Application in X.XXX seconds
```

---

### 第三步：访问系统

浏览器访问：http://localhost:8080

**测试账号：**
- 学生账号：student01 / abc123
- 教师账号：teacher01 / abc123
- 管理员账号：admin / admin123

---

## ✅ 功能测试清单

### 学生端测试

#### 1. 运动打卡
1. 使用学生账号登录
2. 访问：http://localhost:8080/student/sport/checkin
3. 选择运动类型（如：跑步）
4. 填写运动数据：
   - 运动日期：今天
   - 时长：60分钟
   - 距离：5000米
   - 卡路里：300
5. 点击"提交打卡"
6. 应该提示"打卡成功！等待教师审核。"

#### 2. 查看打卡记录
1. 访问：http://localhost:8080/student/sport/records
2. 应该能看到刚才提交的打卡记录
3. 状态显示为"待审核"（黄色标签）
4. 顶部统计卡片应显示数据

---

### 教师端测试

#### 1. 审核打卡
1. 使用教师账号登录
2. 访问：http://localhost:8080/teacher/sport/audit
3. 应该能看到学生提交的待审核记录
4. 点击"通过"按钮
5. 在弹出的对话框中可以填写备注（可选）
6. 点击"确认"
7. 应该提示"审核成功！"

#### 2. 批量审核
1. 勾选多条待审核记录
2. 点击"批量通过"或"批量驳回"
3. 确认操作
4. 应该提示"批量审核成功！"

---

### 验证数据流转

#### 1. 学生再次查看记录
1. 切换回学生账号
2. 访问打卡记录页面
3. 刚才的记录状态应变为"已通过"（绿色标签）
4. 积分应该已计算

#### 2. 检查积分
```sql
-- 查询学生积分
SELECT * FROM student_points ORDER BY create_time DESC LIMIT 10;

-- 查询打卡记录
SELECT * FROM sport_record ORDER BY create_time DESC LIMIT 10;
```

---

## 🎯 核心功能演示流程

### 完整业务流程
```
1. 学生提交打卡 
   ↓
2. 数据存入数据库（status=0待审核）
   ↓
3. 教师查看待审核列表
   ↓
4. 教师审核通过（status=1）
   ↓
5. 积分自动计算并记录
   ↓
6. 学生查看已通过的记录和积分
```

---

## 📊 数据库验证SQL

### 查看运动类型
```sql
SELECT * FROM sport_type WHERE status = 1;
```

### 查看打卡记录
```sql
SELECT 
    sr.id,
    u.real_name as student_name,
    st.type_name,
    sr.record_date,
    sr.duration,
    sr.status,
    sr.earned_points
FROM sport_record sr
JOIN sys_student s ON sr.student_id = s.id
JOIN sys_user u ON s.user_id = u.id
JOIN sport_type st ON sr.sport_type_id = st.id
ORDER BY sr.create_time DESC;
```

### 查看评价维度
```sql
SELECT * FROM evaluation_dimension ORDER BY sort_order;
```

### 查看积分规则
```sql
SELECT * FROM points_rule WHERE status = 1;
```

---

## 🔧 常见问题

### 问题1：页面404
**原因**：模板文件不存在或路径错误

**解决**：
- 确认文件在 `src/main/resources/templates/` 目录下
- 检查Controller中的return路径是否正确
- 重启应用

### 问题2：数据库连接失败
**原因**：数据库配置错误

**解决**：
检查 `application.yml` 中的数据库配置：
```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/sport_evaluation
    username: root
    password: 你的密码
```

### 问题3：表不存在
**原因**：未执行SQL脚本

**解决**：
重新执行 `sport_evaluation_extend.sql` 脚本

### 问题4：权限不足
**原因**：Spring Security拦截

**解决**：
确保已登录且角色正确：
- 学生功能需要STUDENT角色
- 教师功能需要TEACHER角色

---

## 📝 下一步开发建议

### 优先级1：完善现有功能
- [ ] 添加数据校验（前端+后端）
- [ ] 优化用户体验（加载动画、提示美化）
- [ ] 添加导出Excel功能

### 优先级2：新增功能页面
- [ ] 学生积分查询页面
- [ ] 学生排名页面
- [ ] 学生评价结果页面
- [ ] 教师评价学生页面
- [ ] 管理员配置页面

### 优先级3：数据可视化
- [ ] 集成Chart.js
- [ ] 运动趋势图
- [ ] 5维雷达图
- [ ] 积分增长曲线

---

## 💡 开发技巧

### 1. 热重载
修改Java代码后无需重启，Spring DevTools会自动重载。

在 `pom.xml` 中添加：
```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-devtools</artifactId>
    <scope>runtime</scope>
</dependency>
```

### 2. 日志调试
在Service或Controller中添加日志：
```java
log.info("收到打卡请求: {}", record);
log.debug("计算积分: {}", points);
```

查看日志文件：`logs/application.log`

### 3. 数据库调试
使用MyBatis-Plus的日志功能，在 `application.yml` 中添加：
```yaml
mybatis-plus:
  configuration:
    log-impl: org.apache.ibatis.logging.stdout.StdOutImpl
```

---

## 🎓 学习资源

### 官方文档
- Spring Boot: https://spring.io/projects/spring-boot
- MyBatis-Plus: https://baomidou.com/
- Bootstrap: https://getbootstrap.com/
- Chart.js: https://www.chartjs.org/

### 推荐教程
- Thymeleaf模板引擎教程
- Spring Security权限控制
- RESTful API设计最佳实践

---

## ✨ 总结

你现在拥有：
- ✅ 完整的数据库结构
- ✅ 可运行的后端API
- ✅ 3个核心前端页面
- ✅ 完整的业务流程

**立即开始测试吧！**

遇到任何问题，查看：
1. 浏览器控制台（F12）
2. 应用日志文件
3. 数据库数据

祝开发顺利！🚀
