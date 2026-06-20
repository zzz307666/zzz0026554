# 积分兑换功能完整实现说明

## 📋 功能概述

积分兑换系统允许学生使用运动获得的积分兑换奖品或特权，包含完整的奖品管理、兑换流程、记录追踪等功能。

---

## ✨ 核心特性

### 1. **双积分系统设计**
- **累计获得积分**（用于排名）：只统计正向积分，不受兑换影响
- **当前可用积分**（用于消费）：包含所有积分变动，兑换会减少

**优势：**
- ✅ 鼓励学生积极参与运动和评价以获得高排名
- ✅ 学生可以放心兑换奖品而不担心排名下降
- ✅ 排名反映学生的努力和成就，而非储蓄能力

### 2. **完整的兑换流程**
```
浏览奖品 → 选择奖品 → 确认兑换 → 扣减积分 → 生成记录 → 等待领取 → 确认收货
```

### 3. **库存管理**
- 实时显示奖品库存
- 兑换时自动扣减库存
- 库存为0时禁止兑换

### 4. **权限控制**
- 学生：浏览奖品、兑换、查看自己的兑换记录
- 管理员：管理奖品、查看所有兑换记录

---

## 🗂️ 文件结构

### 数据库层
```
src/main/resources/sql/points_exchange.sql
├── points_gift 表 - 奖品信息
└── points_exchange 表 - 兑换记录
```

### 实体类
```
src/main/java/com/hhjt/entity/
├── PointsGift.java - 奖品实体
└── PointsExchange.java - 兑换记录实体
```

### Mapper层
```
src/main/java/com/hhjt/mapper/
├── PointsGiftMapper.java - 奖品数据访问
└── PointsExchangeMapper.java - 兑换记录数据访问
```

### Service层
```
src/main/java/com/hhjt/service/
├── PointsExchangeService.java - 兑换服务接口
└── impl/PointsExchangeServiceImpl.java - 兑换服务实现
```

### Controller层
```
src/main/java/com/hhjt/controller/
├── StudentExchangeController.java - 学生端控制器
└── AdminExchangeController.java - 管理员端控制器
```

### 前端模板
```
src/main/resources/templates/
├── student/
│   ├── exchange_shop.html - 积分商城页面
│   └── my_exchanges.html - 我的兑换记录
└── admin/
    ├── gift_manage.html - 奖品管理列表
    ├── gift_add.html - 添加奖品
    └── gift_edit.html - 编辑奖品
```

---

## 🔧 部署步骤

### 1. 执行数据库脚本

```bash
mysql -u root -p sport_evaluation < src/main/resources/sql/points_exchange.sql
```

或者在MySQL客户端中执行：
```sql
source C:/Users/admin/Desktop/应用/22本科计科三班/39刘博翔/demo01/src/main/resources/sql/points_exchange.sql
```

### 2. 验证表创建成功

```sql
USE sport_evaluation;
SHOW TABLES LIKE 'points_%';
-- 应该看到 points_gift 和 points_exchange 两张表

SELECT COUNT(*) FROM points_gift;
-- 应该返回 8（初始化的8个奖品）
```

### 3. 重启Spring Boot应用

```bash
mvn spring-boot:run
```

### 4. 测试访问

#### 学生端
- 积分商城：http://localhost:8080/student/exchange/shop
- 我的兑换：http://localhost:8080/student/exchange/my-orders

#### 管理员端
- 兑换记录：http://localhost:8080/admin/exchange/manage
- 奖品管理：http://localhost:8080/admin/exchange/gifts
- 添加奖品：http://localhost:8080/admin/exchange/gift/add

---

## 📊 数据库表结构

### points_gift（奖品表）

| 字段 | 类型 | 说明 |
|------|------|------|
| id | bigint | 奖品ID（主键） |
| gift_name | varchar(100) | 奖品名称 |
| gift_image | varchar(500) | 奖品图片URL |
| points_cost | int | 所需积分 |
| stock | int | 库存数量 |
| description | varchar(500) | 奖品描述 |
| category | varchar(50) | 奖品分类 |
| status | tinyint | 状态：0-下架 1-上架 |
| sort_order | int | 排序号 |

### points_exchange（兑换记录表）

| 字段 | 类型 | 说明 |
|------|------|------|
| id | bigint | 兑换记录ID（主键） |
| student_id | bigint | 学生ID |
| gift_id | bigint | 奖品ID |
| gift_name | varchar(100) | 奖品名称（快照） |
| points_cost | int | 消耗积分（快照） |
| exchange_time | datetime | 兑换时间 |
| status | tinyint | 状态：0-待领取 1-已领取 2-已取消 |
| receive_time | datetime | 领取时间 |

---

## 🎯 功能详情

### 学生端功能

#### 1. 积分商城（exchange_shop.html）
- **展示内容**：
  - 当前可用积分余额
  - 所有上架奖品（卡片式布局）
  - 每个奖品的图片、名称、所需积分、库存、描述
  
- **操作**：
  - 点击"立即兑换"按钮
  - 弹出确认对话框显示消耗积分和剩余积分
  - 确认后调用API完成兑换

- **交互逻辑**：
  ```javascript
  // 兑换前检查
  if (currentPoints < gift.pointsCost) {
      alert('积分不足');
      return;
  }
  
  // 确认兑换
  fetch('/student/exchange/do-exchange', {
      method: 'POST',
      body: JSON.stringify({giftId: giftId})
  });
  ```

#### 2. 我的兑换记录（my_exchanges.html）
- **展示内容**：
  - 所有兑换记录列表
  - 每条记录包含：奖品名称、消耗积分、兑换时间、状态
  
- **状态标识**：
  - 🟡 待领取（黄色徽章）
  - 🟢 已领取（绿色徽章）
  - 🔴 已取消（红色徽章）

- **操作**：
  - "待领取"状态的记录可以点击"确认领取"
  - 领取后更新状态和时间

### 管理员端功能

#### 1. 奖品管理（gift_manage.html）
- **展示内容**：
  - 奖品列表表格
  - 分页显示
  - 每行显示：名称、积分、库存、分类、状态、排序
  
- **操作**：
  - **编辑**：跳转到编辑页面修改奖品信息
  - **上架/下架**：一键切换奖品状态
  - **删除**：删除奖品（有兑换记录的不能删除）

- **安全机制**：
  ```java
  // 删除前检查是否有兑换记录
  long count = pointsExchangeMapper.selectCount(
      new LambdaQueryWrapper<PointsExchange>().eq(PointsExchange::getGiftId, id)
  );
  if (count > 0) {
      return "该奖品已有兑换记录，不能删除，建议下架";
  }
  ```

#### 2. 添加奖品（gift_add.html）
- **表单字段**：
  - 奖品名称（必填）
  - 所需积分（必填，数字）
  - 库存数量（必填，数字）
  - 奖品分类（可选）
  - 排序号（可选，数字）
  - 奖品描述（可选）
  - 奖品图片URL（可选）
  - 是否上架（单选框）

- **提交逻辑**：
  ```javascript
  fetch('/admin/exchange/gift/save', {
      method: 'POST',
      headers: {'Content-Type': 'application/json'},
      body: JSON.stringify(giftData)
  });
  ```

#### 3. 编辑奖品（gift_edit.html）
- **预填充数据**：从数据库加载现有奖品信息
- **可修改字段**：同添加奖品页面
- **保存逻辑**：与添加共用同一个save接口，根据id判断是新增还是更新

#### 4. 兑换记录管理（exchange_manage.html）
- **展示内容**：
  - 所有学生的兑换记录
  - 按状态筛选（全部/待领取/已领取/已取消）
  - 分页显示
  
- **每条记录显示**：
  - 学生姓名、学号
  - 奖品名称、消耗积分
  - 兑换时间、状态、领取时间

---

## 🔐 权限配置

### SecurityConfig.java

```java
// 学生端权限
.antMatchers("/student/exchange/**").hasRole("STUDENT")

// 管理员端权限
.antMatchers("/admin/exchange/**").hasRole("ADMIN")
```

### 访问控制
- 未登录用户访问兑换功能会被重定向到登录页
- 学生尝试访问管理员接口会返回403 Forbidden
- 管理员尝试访问学生接口会返回403 Forbidden

---

## 💡 业务逻辑详解

### 兑换流程（PointsExchangeServiceImpl.exchangeGift）

```java
@Transactional(rollbackFor = Exception.class)
public boolean exchangeGift(Long studentId, Long giftId) {
    // 1. 查询奖品信息
    PointsGift gift = pointsGiftMapper.selectById(giftId);
    if (gift == null || gift.getStatus() != 1) {
        throw new RuntimeException("奖品不存在或已下架");
    }
    
    // 2. 检查库存
    if (gift.getStock() <= 0) {
        throw new RuntimeException("奖品库存不足");
    }
    
    // 3. 查询学生当前积分（可用积分）
    BigDecimal currentPoints = pointsService.getStudentTotalPoints(studentId);
    if (currentPoints.compareTo(new BigDecimal(gift.getPointsCost())) < 0) {
        throw new RuntimeException("积分不足");
    }
    
    // 4. 扣除积分（插入负数记录）
    pointsService.addPoints(studentId, "EXCHANGE_GIFT", 
        new BigDecimal(-gift.getPointsCost()), 
        "兑换奖品：" + gift.getGiftName(), 
        giftId);
    
    // 5. 减少库存
    gift.setStock(gift.getStock() - 1);
    pointsGiftMapper.updateById(gift);
    
    // 6. 创建兑换记录
    PointsExchange exchange = new PointsExchange();
    exchange.setStudentId(studentId);
    exchange.setGiftId(giftId);
    exchange.setGiftName(gift.getGiftName()); // 快照
    exchange.setPointsCost(gift.getPointsCost()); // 快照
    exchange.setExchangeTime(LocalDateTime.now());
    exchange.setStatus(0); // 待领取
    
    return pointsExchangeMapper.insert(exchange) > 0;
}
```

### 关键设计点

1. **事务保证**：使用 `@Transactional` 确保积分扣减、库存减少、记录创建要么全部成功，要么全部回滚

2. **数据快照**：兑换记录中保存奖品名称和积分的快照，即使后续修改奖品信息，历史记录也不受影响

3. **负数积分记录**：通过插入负数的 `student_points` 记录来扣减积分，保持积分流水的完整性

4. **并发控制**：在高并发场景下，可能需要添加乐观锁或数据库行锁来防止超卖

---

## 🎨 前端交互设计

### 积分商城页面

**布局特点**：
- 响应式网格布局（Bootstrap grid）
- 卡片式设计，hover效果
- 库存紧张时显示警告徽章
- 积分不足时按钮禁用并显示提示

**用户体验优化**：
- 实时显示当前积分余额
- 兑换前二次确认
- 兑换成功后刷新页面显示最新积分
- 友好的错误提示

### 奖品管理页面

**功能特点**：
- 表格展示，清晰直观
- 状态开关按钮，一键上下架
- 删除前的安全检查
- 分页导航

---

## 📈 扩展建议

### 短期优化
1. **奖品图片上传**：支持本地上传图片而不是输入URL
2. **兑换限制**：同一奖品每人限兑次数
3. **搜索功能**：按奖品名称搜索
4. **分类筛选**：按分类筛选奖品

### 长期规划
1. **奖品推荐**：根据学生积分推荐合适的奖品
2. **限时活动**：特定时间段内奖品打折
3. **兑换审核**：高价值奖品需要管理员审核
4. **物流跟踪**：实物奖品的发货和物流信息
5. **数据统计**：兑换趋势分析、热门奖品排行

---

## ❓ 常见问题

### Q1: 为什么兑换后排名积分不变？
**A**: 系统采用双积分设计：
- 排名使用"累计获得积分"（只统计正值）
- 兑换使用"当前可用积分"（正负总和）
- 这样设计的目的是鼓励学生参与，不因消费而降低成就感

### Q2: 如何添加新的奖品？
**A**: 
1. 登录管理员账号
2. 进入"奖品管理"页面
3. 点击"添加奖品"按钮
4. 填写奖品信息并提交

或者直接执行SQL：
```sql
INSERT INTO points_gift (gift_name, points_cost, stock, description, category, status)
VALUES ('新奖品', 100, 50, '奖品描述', '分类', 1);
```

### Q3: 学生积分不足怎么办？
**A**: 
- 继续参与运动打卡获得积分
- 等待教师进行五维评价获得积分
- 系统会自动阻止积分不足的兑换请求

### Q4: 兑换记录能否删除？
**A**: 
- 不建议删除兑换记录，应保持历史完整性
- 如需撤销兑换，可以手动恢复积分和库存：
```sql
-- 恢复积分
UPDATE student_points SET points_value = -points_value WHERE ...;

-- 恢复库存
UPDATE points_gift SET stock = stock + 1 WHERE id = ?;

-- 标记兑换记录为已取消
UPDATE points_exchange SET status = 2 WHERE id = ?;
```

---

## 🎉 总结

积分兑换功能的完整实现包括：
- ✅ 数据库表设计和初始化数据
- ✅ 完整的后端业务逻辑
- ✅ 学生端和管理员端界面
- ✅ 权限控制和安全性
- ✅ 积分排名不受兑换影响的设计
- ✅ 库存管理和数据一致性保证

这个功能为学生提供了激励机制，让运动获得的积分有了实际的用途，同时通过精心的设计确保了系统的公平性和可持续性。
