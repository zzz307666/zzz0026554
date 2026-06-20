# 积分兑换与排名功能说明

## 🎯 核心设计原则

### 积分排名不变原则
**兑换奖品后，积分排名中的积分不会减少！**

- **积分排名** = 累计获得的总积分（只统计正向积分，不扣除兑换消耗）
- **实际可用积分** = 累计获得积分 - 已兑换消耗的积分

这样设计的目的是：
1. ✅ 积分排名反映学生的运动表现和评价成绩
2. ✅ 鼓励学生积极参与运动和评价
3. ✅ 兑换奖品不会影响排名，避免学生不敢兑换

---

## 📊 两种排名模式

### 1. 积分排名（默认）
- **数据来源**：`student_points` 表中所有正向积分（points_value > 0）
- **包含内容**：
  - 运动打卡获得的积分
  - 五维评价获得的积分（优秀/良好/中等/及格）
  - 其他奖励积分
- **不包含**：兑换奖品消耗的负向积分

**SQL查询逻辑**：
```sql
SELECT COALESCE(SUM(CASE WHEN sp.points_value > 0 THEN sp.points_value ELSE 0 END), 0) as totalPoints
FROM student_points sp
WHERE sp.student_id = #{studentId}
```

### 2. 运动排名
- **数据来源**：`sport_record` 表中已通过的运动记录
- **计算方式**：SUM(earned_points)
- **仅统计**：运动打卡获得的积分

---

## 🔄 兑换流程

### 学生端操作流程

1. **浏览积分商城**
   ```
   http://localhost:8080/student/exchange/shop
   ```
   - 查看当前可用积分（实际可用积分）
   - 浏览可兑换的奖品列表

2. **兑换奖品**
   - 点击"立即兑换"按钮
   - 系统检查：
     - ✅ 奖品是否上架且有库存
     - ✅ 学生积分是否足够
   - 如果通过检查：
     - 扣除相应积分（插入负数积分记录）
     - 减少奖品库存
     - 创建兑换记录（状态：待领取）

3. **查看兑换记录**
   ```
   http://localhost:8080/student/exchange/my-orders
   ```
   - 查看所有兑换历史
   - 对待领取的奖品点击"确认领取"

4. **查看积分排名**
   ```
   http://localhost:8080/sport/ranking?type=points
   ```
   - **排名积分不变**（即使刚兑换了奖品）
   - 显示累计获得的总积分

---

## 💡 示例场景

### 场景：学生A的积分变化

#### 初始状态
- 运动打卡获得：100分
- 评价获得（优秀）：30分
- **累计获得积分**：130分
- **实际可用积分**：130分
- **积分排名**：第5名（130分）

#### 兑换奖品后（兑换50分的运动水杯）
- 系统插入一条 `-50` 分的积分记录
- **累计获得积分**：130分（不变！）
- **实际可用积分**：80分（130 - 50）
- **积分排名**：仍然是第5名（130分）✅

#### 再次兑换后（兑换30分的运动毛巾）
- 系统插入一条 `-30` 分的积分记录
- **累计获得积分**：130分（不变！）
- **实际可用积分**：50分（130 - 50 - 30）
- **积分排名**：仍然是第5名（130分）✅

---

## 🔧 技术实现

### 1. 数据库层

#### student_points 表结构
```sql
CREATE TABLE `student_points` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `student_id` bigint NOT NULL,
  `points_type` varchar(20) NOT NULL,  -- MOTION/EVALUATION/EXCHANGE_GIFT等
  `points_value` decimal(8,2) NOT NULL, -- 正数为增加，负数为减少
  `description` varchar(500),
  ...
);
```

#### 积分排名查询
```sql
-- 只统计正向积分（points_value > 0）
SELECT s.id as studentId, 
       u.real_name as studentName, 
       COALESCE(SUM(CASE WHEN sp.points_value > 0 THEN sp.points_value ELSE 0 END), 0) as totalPoints
FROM sys_student s
LEFT JOIN student_points sp ON s.id = sp.student_id
GROUP BY s.id
ORDER BY totalPoints DESC
```

### 2. Service层

#### PointsServiceImpl
```java
// 获取学生总积分（包含所有变动，用于显示可用积分）
public BigDecimal getStudentTotalPoints(Long studentId) {
    return pointsMapper.getStudentTotalPoints(studentId);
}

// 添加积分（正数或负数）
@Transactional
public void addPoints(Long studentId, String pointsType, BigDecimal value, 
                      String description, Long relatedId) {
    StudentPoints points = new StudentPoints();
    points.setStudentId(studentId);
    points.setPointsType(pointsType);
    points.setPointsValue(value); // 可以是负数
    points.setDescription(description);
    pointsMapper.insert(points);
}
```

#### PointsExchangeServiceImpl
```java
@Transactional
public boolean exchangeGift(Long studentId, Long giftId) {
    // 1. 检查奖品和库存
    PointsGift gift = pointsGiftMapper.selectById(giftId);
    if (gift.getStock() <= 0) {
        throw new RuntimeException("奖品库存不足");
    }
    
    // 2. 检查学生积分（使用实际可用积分）
    BigDecimal currentPoints = pointsService.getStudentTotalPoints(studentId);
    if (currentPoints.compareTo(new BigDecimal(gift.getPointsCost())) < 0) {
        throw new RuntimeException("积分不足");
    }
    
    // 3. 扣除积分（插入负数记录）
    pointsService.addPoints(studentId, "EXCHANGE_GIFT", 
        new BigDecimal(-gift.getPointsCost()), 
        "兑换奖品：" + gift.getGiftName(), 
        giftId);
    
    // 4. 减少库存
    gift.setStock(gift.getStock() - 1);
    pointsGiftMapper.updateById(gift);
    
    // 5. 创建兑换记录
    PointsExchange exchange = new PointsExchange();
    exchange.setStudentId(studentId);
    exchange.setGiftId(giftId);
    exchange.setGiftName(gift.getGiftName());
    exchange.setPointsCost(gift.getPointsCost());
    exchange.setStatus(0); // 待领取
    pointsExchangeMapper.insert(exchange);
    
    return true;
}
```

### 3. Controller层

#### SportRankingController
```java
@GetMapping("/data")
@ResponseBody
public Map<String, Object> getRankingData(
    @RequestParam(required = false) Long classId,
    @RequestParam(defaultValue = "points") String type) {
    
    List<Map<String, Object>> ranking;
    
    if ("sport".equals(type)) {
        // 运动排名（基于运动记录）
        ranking = classId != null ? 
            sportRecordMapper.getClassRanking(classId) : 
            sportRecordMapper.getSchoolRanking();
    } else {
        // 积分排名（基于累计获得积分，不扣除兑换）
        ranking = classId != null ? 
            sportRecordMapper.getClassPointsRanking(classId) : 
            sportRecordMapper.getSchoolPointsRanking();
    }
    
    result.put("success", true);
    result.put("data", ranking);
    result.put("type", type);
    return result;
}
```

---

## 📋 积分规则配置

### 评价等级积分规则

| 规则名称 | 规则编码 | 类型 | 积分值 | 触发条件 |
|---------|---------|------|--------|---------|
| 评价优秀奖励 | EVALUATION_EXCELLENT | BONUS | 30.00 | grade_level = "优秀" |
| 评价良好奖励 | EVALUATION_GOOD | BONUS | 20.00 | grade_level = "良好" |
| 评价中等奖励 | EVALUATION_MEDIUM | BONUS | 10.00 | grade_level = "中等" |
| 评价及格奖励 | EVALUATION_PASS | BONUS | 5.00 | grade_level = "及格" |

### 自动加分逻辑

当教师发布五维评价时：
```java
@Transactional
public boolean publishEvaluation(Long evaluationId) {
    StudentEvaluation evaluation = evaluationMapper.selectById(evaluationId);
    
    // 1. 更新评价状态为已发布
    evaluation.setStatus(1);
    evaluationMapper.updateById(evaluation);
    
    // 2. 根据等级自动加分
    if (evaluation.getGradeLevel() != null) {
        addPointsByGradeLevel(
            evaluation.getStudentId(), 
            evaluation.getGradeLevel(), 
            evaluation.getId()
        );
    }
    
    return true;
}

private void addPointsByGradeLevel(Long studentId, String gradeLevel, Long evaluationId) {
    String ruleCode = null;
    switch (gradeLevel) {
        case "优秀": ruleCode = "EVALUATION_EXCELLENT"; break;
        case "良好": ruleCode = "EVALUATION_GOOD"; break;
        case "中等": ruleCode = "EVALUATION_MEDIUM"; break;
        case "及格": ruleCode = "EVALUATION_PASS"; break;
        default: return; // 不及格不加分
    }
    
    // 查询积分规则
    PointsRule rule = pointsRuleMapper.selectOne(
        new LambdaQueryWrapper<PointsRule>()
            .eq(PointsRule::getRuleCode, ruleCode)
            .eq(PointsRule::getStatus, 1)
    );
    
    if (rule != null && rule.getPointsValue().compareTo(BigDecimal.ZERO) > 0) {
        // 添加正向积分记录
        pointsService.addPoints(studentId, "EVALUATION_REWARD", 
            rule.getPointsValue(), 
            "评价获得" + gradeLevel + "等级，奖励" + rule.getPointsValue() + "积分", 
            evaluationId);
    }
}
```

---

## 🎨 前端页面

### 排名页面切换
```html
<!-- 排名类型选择 -->
<select id="rankingType" class="form-select">
    <option value="points">积分排名</option>
    <option value="sport">运动排名</option>
</select>

<!-- 班级筛选 -->
<select id="classFilter" class="form-select">
    <option value="">全校排名</option>
    <!-- 动态加载班级列表 -->
</select>
```

### JavaScript逻辑
```javascript
function loadRanking() {
    const classId = document.getElementById('classFilter').value;
    const rankingType = document.getElementById('rankingType').value;
    
    let url = '/sport/ranking/data?type=' + rankingType;
    if (classId) {
        url += '&classId=' + classId;
    }
    
    fetch(url)
        .then(response => response.json())
        .then(result => {
            if (result.success) {
                displayRanking(result.data, classId, result.type);
            }
        });
}

function displayRanking(data, classId, type) {
    const typeText = type === 'points' ? '积分' : '运动';
    title.textContent = classId ? `班级${typeText}排名` : `全校${typeText}排名`;
    // ... 渲染表格
}
```

---

## ✅ 功能验证清单

### 积分排名验证
- [ ] 访问 `/sport/ranking?type=points`
- [ ] 选择"积分排名"模式
- [ ] 查看学生排名（显示累计获得积分）
- [ ] 记录某个学生的排名和积分

### 兑换验证
- [ ] 学生登录，访问积分商城
- [ ] 查看当前可用积分
- [ ] 兑换一个奖品（如50积分的运动水杯）
- [ ] 兑换成功，提示"兑换成功"

### 兑换后验证
- [ ] 学生查看"我的积分"页面
  - 可用积分应该减少50分
  - 积分明细中有两条记录：
    - 正向积分（如+100分，运动获得）
    - 负向积分（-50分，兑换奖品）
- [ ] 学生访问积分排名页面
  - **排名积分不变**（仍然是兑换前的积分）
  - 排名位置不变
- [ ] 管理员查看兑换记录
  - 看到新的兑换记录
  - 状态为"待领取"

### 评价加分验证
- [ ] 教师审核通过一个运动记录
- [ ] 教师对该学生进行五维评价
- [ ] 评分达到"优秀"（≥90分）
- [ ] 发布评价
- [ ] 学生查看积分明细
  - 新增一条记录："评价获得优秀等级，奖励30.00积分"
- [ ] 学生查看积分排名
  - 排名积分增加30分

---

## 🔍 常见问题

### Q1: 为什么兑换后排名积分不变？
**A**: 这是设计如此。积分排名反映的是学生的运动表现和评价成绩，不应该因为兑换奖品而降低排名。这样可以鼓励学生积极兑换，不用担心影响排名。

### Q2: 如何查看学生当前的可用积分？
**A**: 学生访问"我的积分"页面（`/student/points`），显示的是实际可用积分（累计获得 - 已兑换）。

### Q3: 如果学生积分不足，能兑换吗？
**A**: 不能。系统会在兑换前检查可用积分，如果不足会提示"积分不足，当前积分：X，需要积分：Y"。

### Q4: 兑换后可以取消吗？
**A**: 目前版本不支持取消兑换。后续可以添加"取消兑换"功能，在待领取状态下允许取消并退还积分。

### Q5: 管理员如何修改积分规则？
**A**: 访问 `/admin/points/rule` 页面，可以编辑积分规则的积分值、启用/禁用状态等。

---

## 📝 总结

✅ **已完成功能**：
1. 积分排名和运动排名两种模式
2. 兑换奖品自动扣除积分（插入负数记录）
3. 积分排名使用累计获得积分（不扣除兑换）
4. 评价发布时自动根据等级加分
5. 完整的兑换记录和领取流程

✅ **核心特性**：
- 兑换后排名积分不变
- 积分不足时拒绝兑换
- 库存为0时无法兑换
- 事务保证数据一致性

🎉 **功能完整可用，可以开始测试！**
