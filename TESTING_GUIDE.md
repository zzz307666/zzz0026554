# 运动评估系统 - 快速测试指南 🧪

## 📋 测试前准备

### 1. 数据库初始化
```bash
# 执行SQL脚本
mysql -u root -p sport_evaluation < src/main/resources/sql/sport_evaluation_extend.sql

# 验证表是否创建成功
mysql -u root -p -e "USE sport_evaluation; SHOW TABLES LIKE 'sport_%';"
mysql -u root -p -e "USE sport_evaluation; SHOW TABLES LIKE 'student_%';"
mysql -u root -p -e "USE sport_evaluation; SHOW TABLES LIKE 'evaluation_%';"
```

应该看到9个新表。

### 2. 启动应用
```bash
cd C:\Users\admin\Desktop\应用\22本科计科三班\39刘博翔\demo01
mvn spring-boot:run
```

等待看到：`Started Demo01Application in X.XXX seconds`

---

## ✅ 核心功能测试流程

### 测试场景1：学生打卡 → 教师审核 → 积分自动计算

#### 步骤1：学生提交打卡
1. 浏览器访问：http://localhost:8080/student/sport/checkin
2. 使用账号登录：student01 / abc123
3. 选择运动类型：点击"跑步"卡片（🏃）
4. 填写运动数据：
   - 运动日期：今天（默认已填）
   - 运动时长：60分钟
   - 运动距离：5000米
   - 消耗卡路里：300
5. 查看预估积分：应该显示约 **20.00分**
6. 点击"提交打卡"按钮
7. **预期结果**：提示"打卡成功！等待教师审核。"

#### 步骤2：教师审核通过
1. 新开浏览器窗口（或退出学生账号）
2. 访问：http://localhost:8080/teacher/sport/audit
3. 使用账号登录：teacher01 / abc123
4. 看到待审核列表，找到刚才的打卡记录
5. 点击"通过"按钮
6. 在弹出的对话框中可以填写备注（可选）
7. 点击"确认"
8. **预期结果**：提示"审核成功！"

#### 步骤3：学生查看积分增加
1. 切换回学生账号
2. 访问：http://localhost:8080/student/points
3. **预期结果**：
   - 顶部总积分卡片显示增加的积分
   - 积分明细表格有一条"运动积分 +20.00"的记录

**✅ 测试通过标准**：积分自动计算并添加到学生账户

---

### 测试场景2：教师评价学生 → 学生查看雷达图

#### 步骤1：教师评价学生
1. 使用教师账号登录
2. 访问：http://localhost:8080/teacher/evaluation/evaluate/1
   （假设学生ID为1，根据实际情况调整）
3. 拖动5个滑动条评分：
   - 耐力：85分
   - 力量：78分
   - 速度：92分
   - 柔韧：70分
   - 协调：88分
4. 观察右侧实时计算：
   - 综合总分应显示：**83.20**
   - 等级应显示：**良好**（蓝色徽章）
5. 填写教师评语："继续保持，表现不错！"
6. 点击"发布评价"按钮
7. 确认发布
8. **预期结果**：提示"发布成功！"

#### 步骤2：学生查看评价结果
1. 切换回学生账号
2. 访问：http://localhost:8080/student/evaluation
3. **预期结果**：
   - 看到评价卡片
   - 5维指标详细评分表格
   - **漂亮的Chart.js雷达图** 📊
   - 综合总分：83.20
   - 等级徽章：良好
   - 教师评语显示

**✅ 测试通过标准**：评价成功发布，学生可见，雷达图正常显示

---

### 测试场景3：批量审核功能

#### 步骤1：准备多条待审核记录
1. 使用学生账号多次提交打卡（至少3次）
2. 或使用不同学生账号提交

#### 步骤2：教师批量审核
1. 使用教师账号访问：http://localhost:8080/teacher/sport/audit
2. 勾选多条待审核记录（使用复选框）
3. 点击"批量通过"按钮
4. 确认操作
5. **预期结果**：提示"批量审核成功！"
6. 所有选中记录状态变为"已通过"

**✅ 测试通过标准**：批量操作成功，所有记录状态更新

---

### 测试场景4：班级排名查看

#### 步骤1：确保有积分数据
1. 完成测试场景1，确保有学生有积分

#### 步骤2：查看排名
1. 使用学生账号访问：http://localhost:8080/student/ranking
2. **预期结果**：
   - 看到排行榜表格
   - 第1名显示🥇金牌徽章
   - 第2名显示🥈银牌徽章
   - 第3名显示🥉铜牌徽章
   - 显示每个人的打卡次数和总积分
   - 显示平均每次积分

**✅ 测试通过标准**：排名正确显示，徽章美观

---

### 测试场景5：评价列表查看

#### 步骤1：教师查看评价历史
1. 使用教师账号访问：http://localhost:8080/teacher/evaluation/list
2. **预期结果**：
   - 看到评价列表表格
   - 显示学生姓名、学号
   - 显示5维评分
   - 显示综合总分和等级徽章
   - 显示状态（草稿/已发布）

**✅ 测试通过标准**：列表完整显示，筛选功能正常

---

## 🔍 数据库验证SQL

### 验证打卡记录
```sql
-- 查看所有打卡记录
SELECT 
    sr.id,
    u.real_name as student_name,
    st.type_name,
    sr.record_date,
    sr.duration,
    sr.status,
    sr.earned_points,
    sr.create_time
FROM sport_record sr
JOIN sys_student s ON sr.student_id = s.id
JOIN sys_user u ON s.user_id = u.id
JOIN sport_type st ON sr.sport_type_id = st.id
ORDER BY sr.create_time DESC
LIMIT 10;
```

### 验证积分记录
```sql
-- 查看学生积分明细
SELECT 
    sp.id,
    u.real_name,
    sp.points_type,
    sp.points_value,
    sp.description,
    sp.create_time
FROM student_points sp
JOIN sys_student s ON sp.student_id = s.id
JOIN sys_user u ON s.user_id = u.id
ORDER BY sp.create_time DESC
LIMIT 10;
```

### 验证评价记录
```sql
-- 查看学生评价
SELECT 
    se.id,
    u.real_name as student_name,
    se.evaluation_period,
    se.endurance_score,
    se.strength_score,
    se.speed_score,
    se.flexibility_score,
    se.coordination_score,
    se.total_score,
    se.grade_level,
    se.status
FROM student_evaluation se
JOIN sys_student s ON se.student_id = s.id
JOIN sys_user u ON s.user_id = u.id
ORDER BY se.create_time DESC;
```

### 验证统计数据
```sql
-- 查看运动类型
SELECT * FROM sport_type WHERE status = 1 ORDER BY sort_order;

-- 查看评价维度
SELECT * FROM evaluation_dimension ORDER BY sort_order;

-- 查看积分规则
SELECT * FROM points_rule WHERE status = 1;
```

---

## ⚠️ 常见问题排查

### 问题1：页面404错误
**原因**：模板文件不存在或路径错误

**解决**：
```bash
# 检查文件是否存在
ls src/main/resources/templates/student/
ls src/main/resources/templates/teacher/

# 重启应用
mvn spring-boot:run
```

### 问题2：数据库连接失败
**错误信息**：`Communications link failure`

**解决**：
1. 检查MySQL是否启动
2. 检查 `application.yml` 中的数据库配置：
   ```yaml
   spring:
     datasource:
       url: jdbc:mysql://localhost:3306/sport_evaluation
       username: root
       password: root  # 修改为你的密码
   ```
3. 确认数据库 `sport_evaluation` 已创建

### 问题3：表不存在
**错误信息**：`Table 'sport_evaluation.sport_type' doesn't exist`

**解决**：
```bash
# 重新执行SQL脚本
mysql -u root -p sport_evaluation < src/main/resources/sql/sport_evaluation_extend.sql

# 验证表是否存在
mysql -u root -p -e "USE sport_evaluation; SHOW TABLES;"
```

### 问题4：权限不足
**错误信息**：`Access Denied` 或重定向到登录页

**解决**：
1. 确认已登录
2. 确认角色正确：
   - 学生功能需要STUDENT角色
   - 教师功能需要TEACHER角色
3. 清除浏览器缓存后重试

### 问题5：积分未自动计算
**现象**：审核通过后学生积分未增加

**排查**：
```sql
-- 检查审核状态
SELECT id, status, earned_points FROM sport_record ORDER BY create_time DESC LIMIT 5;

-- 检查积分记录
SELECT * FROM student_points ORDER BY create_time DESC LIMIT 5;

-- 查看应用日志
tail -f logs/application.log | grep "添加积分"
```

**可能原因**：
- PointsService未正确注入
- 事务未提交
- 异常被捕获但未记录

---

## 📊 测试检查清单

### 学生端功能
- [ ] 运动打卡页面正常显示
- [ ] 可以选择运动类型
- [ ] 表单验证生效（时长≥30分钟）
- [ ] 预估积分实时显示
- [ ] 提交打卡成功
- [ ] 打卡记录列表显示
- [ ] 统计卡片数据正确
- [ ] 积分查询页面显示
- [ ] 总积分正确
- [ ] 积分明细完整
- [ ] 评价结果页面显示
- [ ] 雷达图正常渲染
- [ ] 班级排名显示
- [ ] 金银铜牌徽章显示

### 教师端功能
- [ ] 待审核列表显示
- [ ] 单个审核功能正常
- [ ] 批量审核功能正常
- [ ] 审核备注可以输入
- [ ] 评价学生页面显示
- [ ] 滑动条可以拖动
- [ ] 总分实时计算
- [ ] 等级自动判定
- [ ] 保存草稿成功
- [ ] 发布评价成功
- [ ] 评价列表显示
- [ ] 筛选功能正常

### 自动化流程
- [ ] 审核通过自动触发积分计算
- [ ] 积分记录正确插入
- [ ] 评价保存自动计算总分
- [ ] 评价发布后学生可见
- [ ] 等级判定正确

### 数据一致性
- [ ] 数据库记录完整
- [ ] 外键约束生效
- [ ] 索引正常工作
- [ ] 事务回滚正常

---

## 🎯 测试完成标准

**全部通过**：所有测试项打勾 ✅

**核心功能可用**：
- ✅ 学生可以打卡
- ✅ 教师可以审核
- ✅ 积分自动计算
- ✅ 评价可以发布
- ✅ 数据可视化正常

---

## 📝 测试报告模板

```
测试日期：2026-05-10
测试人员：___________
测试环境：Windows / MySQL 8.0 / JDK 8

测试结果：
- 学生端功能：____/14 通过
- 教师端功能：____/12 通过
- 自动化流程：____/5 通过
- 数据一致性：____/4 通过

发现的问题：
1. ________________
2. ________________

总体评价：□ 优秀  □ 良好  □ 一般  □ 需改进
```

---

## 🚀 开始测试

现在你可以：
1. 按照上述步骤逐一测试
2. 记录测试结果
3. 发现问题及时反馈

**祝测试顺利！** 🎉
