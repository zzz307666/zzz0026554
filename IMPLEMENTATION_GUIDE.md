# 运动评估系统 - 完整实施方案

## 📋 项目概述

本系统是一个高校学生运动评估管理平台，实现运动打卡、数据审核、5维评价、积分排名等功能。

---

## 🗄️ 一、数据库设计

### 1.1 已完成的数据库表

✅ **基础表**（已存在）
- `sys_user` - 用户表
- `sys_role` - 角色表  
- `sys_admin` - 管理员表
- `sys_teacher` - 教师表
- `sys_student` - 学生表
- `sys_class` - 班级表
- `sys_teacher_class` - 教师班级关联表

✅ **扩展表**（新增）
- `sport_type` - 运动类型表
- `sport_record` - 运动打卡记录表
- `evaluation_dimension` - 评价维度表
- `student_evaluation` - 学生评价记录表
- `student_points` - 学生积分表
- `points_rule` - 积分规则配置表
- `sys_operation_log` - 系统操作日志表
- `sys_notice` - 系统公告表
- `sys_semester` - 学期配置表

### 1.2 执行SQL脚本

```bash
# 在MySQL中执行扩展脚本
mysql -u root -p sport_evaluation < src/main/resources/sql/sport_evaluation_extend.sql
```

---

## 💻 二、后端实现

### 2.1 实体类（Entity）✅ 已完成

已创建以下实体类：
- `SportType.java` - 运动类型
- `SportRecord.java` - 运动打卡记录
- `EvaluationDimension.java` - 评价维度
- `StudentEvaluation.java` - 学生评价
- `StudentPoints.java` - 学生积分

### 2.2 Mapper接口 ✅ 已完成

已创建：
- `SportTypeMapper.java`
- `SportRecordMapper.java` - 含统计查询
- `EvaluationDimensionMapper.java`
- `StudentEvaluationMapper.java`
- `StudentPointsMapper.java` - 含积分汇总

### 2.3 Service层（需要创建）

#### 2.3.1 运动打卡服务

```java
// SportRecordService.java
public interface SportRecordService {
    // 学生提交运动打卡
    boolean submitRecord(SportRecord record, Long studentId);
    
    // 分页查询打卡记录
    IPage<SportRecord> getRecordPage(Integer page, Integer size, Long studentId, Integer status);
    
    // 教师审核打卡记录
    boolean auditRecord(Long recordId, Integer status, String remark, Long teacherId);
    
    // 批量审核
    boolean batchAudit(List<Long> recordIds, Integer status, Long teacherId);
    
    // 计算积分
    BigDecimal calculatePoints(SportRecord record);
}
```

#### 2.3.2 评价服务

```java
// EvaluationService.java
public interface EvaluationService {
    // 保存评价
    boolean saveEvaluation(StudentEvaluation evaluation);
    
    // 发布评价
    boolean publishEvaluation(Long evaluationId);
    
    // 查询学生评价列表
    List<StudentEvaluation> getStudentEvaluations(Long studentId);
    
    // 计算综合得分
    BigDecimal calculateTotalScore(StudentEvaluation evaluation);
    
    // 确定等级
    String determineGradeLevel(BigDecimal totalScore);
}
```

#### 2.3.3 积分服务

```java
// PointsService.java
public interface PointsService {
    // 增加积分
    void addPoints(Long studentId, String pointsType, BigDecimal value, String description, Long relatedId);
    
    // 查询学生总积分
    BigDecimal getStudentTotalPoints(Long studentId);
    
    // 查询积分明细
    IPage<StudentPoints> getPointsPage(Integer page, Integer size, Long studentId);
    
    // 应用积分规则
    void applyPointsRules(SportRecord record);
}
```

### 2.4 Controller层（需要创建）

#### 2.4.1 学生端控制器

```java
// StudentSportController.java
@Controller
@RequestMapping("/student/sport")
public class StudentSportController {
    
    // 运动打卡页面
    @GetMapping("/checkin")
    public String checkinPage(Model model) {
        model.addAttribute("sportTypes", sportTypeService.listEnabled());
        return "student/sport_checkin";
    }
    
    // 提交打卡
    @PostMapping("/submit")
    @ResponseBody
    public Map<String, Object> submitRecord(@RequestBody SportRecord record) {
        // 实现逻辑
    }
    
    // 我的打卡记录
    @GetMapping("/records")
    public String myRecords(Model model, 
                           @RequestParam(defaultValue = "1") Integer page) {
        // 实现逻辑
        return "student/sport_records";
    }
    
    // 我的积分
    @GetMapping("/points")
    public String myPoints(Model model) {
        // 实现逻辑
        return "student/points";
    }
    
    // 排名查看
    @GetMapping("/ranking")
    public String ranking(Model model) {
        // 实现逻辑
        return "student/ranking";
    }
    
    // 评价结果
    @GetMapping("/evaluation")
    public String evaluation(Model model) {
        // 实现逻辑
        return "student/evaluation";
    }
    
    // 数据可视化
    @GetMapping("/visualization")
    public String visualization(Model model) {
        // 实现逻辑
        return "student/visualization";
    }
}
```

#### 2.4.2 教师端控制器

```java
// TeacherSportController.java
@Controller
@RequestMapping("/teacher/sport")
public class TeacherSportController {
    
    // 待审核列表
    @GetMapping("/audit")
    public String auditList(Model model) {
        // 实现逻辑
        return "teacher/sport_audit";
    }
    
    // 审核操作
    @PostMapping("/audit")
    @ResponseBody
    public Map<String, Object> audit(@RequestBody Map<String, Object> params) {
        // 实现逻辑
    }
    
    // 评价学生
    @GetMapping("/evaluate/{studentId}")
    public String evaluatePage(@PathVariable Long studentId, Model model) {
        // 实现逻辑
        return "teacher/evaluate_student";
    }
    
    // 保存评价
    @PostMapping("/evaluate")
    @ResponseBody
    public Map<String, Object> saveEvaluation(@RequestBody StudentEvaluation evaluation) {
        // 实现逻辑
    }
    
    // 班级数据统计
    @GetMapping("/class/stats")
    public String classStats(Model model) {
        // 实现逻辑
        return "teacher/class_stats";
    }
}
```

#### 2.4.3 管理员端控制器

```java
// AdminSportController.java
@Controller
@RequestMapping("/admin/sport")
public class AdminSportController {
    
    // 运动类型管理
    @GetMapping("/type/manage")
    public String typeManage(Model model) {
        model.addAttribute("sportTypes", sportTypeService.list());
        return "admin/sport_type_manage";
    }
    
    // 添加运动类型
    @PostMapping("/type/add")
    @ResponseBody
    public Map<String, Object> addType(@RequestBody SportType type) {
        // 实现逻辑
    }
    
    // 评价维度配置
    @GetMapping("/dimension/config")
    public String dimensionConfig(Model model) {
        model.addAttribute("dimensions", dimensionService.list());
        return "admin/dimension_config";
    }
    
    // 更新维度权重
    @PostMapping("/dimension/update")
    @ResponseBody
    public Map<String, Object> updateDimension(@RequestBody EvaluationDimension dimension) {
        // 实现逻辑
    }
    
    // 积分规则配置
    @GetMapping("/points/rule")
    public String pointsRule(Model model) {
        // 实现逻辑
        return "admin/points_rule";
    }
    
    // 操作日志
    @GetMapping("/log")
    public String operationLog(Model model,
                              @RequestParam(defaultValue = "1") Integer page) {
        // 实现逻辑
        return "admin/operation_log";
    }
}
```

---

## 🎨 三、前端页面实现

### 3.1 学生端页面

#### 3.1.1 运动打卡页面 (`student/sport_checkin.html`)

功能：
- 选择运动类型（下拉框）
- 输入运动数据（时长、距离、卡路里等）
- 提交打卡
- 显示今日已打卡记录

#### 3.1.2 打卡记录页面 (`student/sport_records.html`)

功能：
- 历史记录列表（分页）
- 筛选（按日期、状态、运动类型）
- 查看详情
- 导出Excel

#### 3.1.3 积分页面 (`student/points.html`)

功能：
- 当前总积分（大数字展示）
- 积分明细列表
- 积分趋势图（Chart.js）

#### 3.1.4 排名页面 (`student/ranking.html`)

功能：
- 班级排名表格
- 年级排名表格
- 全校TOP10
- 排名变化趋势

#### 3.1.5 评价结果页面 (`student/evaluation.html`)

功能：
- 历次评价列表
- 5维雷达图
- 教师评语
- 综合等级

#### 3.1.6 数据可视化页面 (`student/visualization.html`)

功能：
- 运动趋势折线图
- 运动类型分布饼图
- 5维指标雷达图
- 月度统计卡片

### 3.2 教师端页面

#### 3.2.1 审核页面 (`teacher/sport_audit.html`)

功能：
- 待审核列表
- 批量选择
- 一键通过/驳回
- 查看学生历史数据

#### 3.2.2 评价页面 (`teacher/evaluate_student.html`)

功能：
- 5个维度评分滑块
- 自动计算总分
- 等级自动判定
- 评语输入框
- 保存/发布按钮

#### 3.2.3 班级统计页面 (`teacher/class_stats.html`)

功能：
- 班级平均分对比
- 学生分布直方图
- 运动活跃度排行
- 导出数据报表

### 3.3 管理员端页面

#### 3.3.1 运动类型管理 (`admin/sport_type_manage.html`)

功能：
- 类型列表（表格）
- 添加/编辑/删除
- 启用/禁用
- 排序调整

#### 3.3.2 维度配置 (`admin/dimension_config.html`)

功能：
- 5维指标列表
- 权重调整（滑块）
- 满分设置
- 实时预览权重分配

#### 3.3.3 积分规则 (`admin/points_rule.html`)

功能：
- 规则列表
- 添加/编辑规则
- 条件配置（JSON编辑器）
- 启用/禁用

#### 3.3.4 操作日志 (`admin/operation_log.html`)

功能：
- 日志列表（分页）
- 筛选（模块、用户、时间）
- 查看详情
- 导出日志

---

## 🔧 四、关键技术实现

### 4.1 积分计算逻辑

```java
@Service
public class PointsServiceImpl implements PointsService {
    
    @Autowired
    private SportTypeMapper sportTypeMapper;
    
    @Autowired
    private StudentPointsMapper pointsMapper;
    
    @Override
    public BigDecimal calculatePoints(SportRecord record) {
        // 获取运动类型
        SportType sportType = sportTypeMapper.selectById(record.getSportTypeId());
        
        // 基础积分 = 基础分 × 系数 × (时长/30)
        BigDecimal basePoints = sportType.getBasePoints();
        BigDecimal coefficient = sportType.getCoefficient();
        BigDecimal durationFactor = new BigDecimal(record.getDuration()).divide(new BigDecimal(30), 2, RoundingMode.HALF_UP);
        
        BigDecimal points = basePoints.multiply(coefficient).multiply(durationFactor);
        
        // 连续打卡奖励
        if (isContinuousCheckin(record.getStudentId(), 7)) {
            points = points.add(new BigDecimal(5));
        }
        
        return points.setScale(2, RoundingMode.HALF_UP);
    }
}
```

### 4.2 5维评价计算

```java
@Service
public class EvaluationServiceImpl implements EvaluationService {
    
    @Override
    public BigDecimal calculateTotalScore(StudentEvaluation evaluation) {
        // 获取各维度权重
        List<EvaluationDimension> dimensions = dimensionMapper.selectList(
            new LambdaQueryWrapper<EvaluationDimension>().eq(EvaluationDimension::getStatus, 1)
        );
        
        BigDecimal totalScore = BigDecimal.ZERO;
        
        for (EvaluationDimension dim : dimensions) {
            BigDecimal score = getDimensionScore(dim.getDimensionCode(), evaluation);
            totalScore = totalScore.add(score.multiply(dim.getWeight()));
        }
        
        return totalScore.setScale(2, RoundingMode.HALF_UP);
    }
    
    @Override
    public String determineGradeLevel(BigDecimal totalScore) {
        if (totalScore.compareTo(new BigDecimal(90)) >= 0) {
            return "优秀";
        } else if (totalScore.compareTo(new BigDecimal(80)) >= 0) {
            return "良好";
        } else if (totalScore.compareTo(new BigDecimal(70)) >= 0) {
            return "中等";
        } else if (totalScore.compareTo(new BigDecimal(60)) >= 0) {
            return "及格";
        } else {
            return "不及格";
        }
    }
}
```

### 4.3 数据可视化（前端Chart.js）

```javascript
// 5维雷达图
const radarCtx = document.getElementById('radarChart').getContext('2d');
new Chart(radarCtx, {
    type: 'radar',
    data: {
        labels: ['耐力', '力量', '速度', '柔韧', '协调'],
        datasets: [{
            label: '我的评分',
            data: [85, 78, 92, 70, 88],
            backgroundColor: 'rgba(54, 162, 235, 0.2)',
            borderColor: 'rgba(54, 162, 235, 1)',
            borderWidth: 2
        }]
    },
    options: {
        scales: {
            r: {
                beginAtZero: true,
                max: 100
            }
        }
    }
});
```

---

## 📊 五、数据流转路径

### 5.1 运动打卡流程

```
学生提交打卡 
  → 数据校验（时长≥30分钟）
  → 存入数据库（status=0待审核）
  → 教师端显示待审核
  → 教师审核（通过/驳回）
  → 审核通过：计算积分 → 写入积分表 → 更新排名
  → 审核驳回：返回原因给学生
```

### 5.2 评价流程

```
教师进入评价页面 
  → 查看学生运动数据
  → 填写5维评分
  → 系统自动计算总分
  → 系统自动判定等级
  → 教师填写评语
  → 保存为草稿 / 直接发布
  → 发布后学生可见
  → 生成评价积分
```

### 5.3 积分计算流程

```
触发事件（打卡通过/评价发布）
  → 查询积分规则
  → 匹配适用规则
  → 计算积分值
  → 写入积分明细表
  → 更新学生总积分
  → 更新排名缓存
  → 通知学生（可选）
```

---

## 🚀 六、项目实施步骤

### 阶段1：数据库准备（已完成✅）
- [x] 创建扩展SQL脚本
- [x] 执行SQL创建表
- [x] 初始化基础数据

### 阶段2：后端核心功能
- [ ] 创建Service实现类
- [ ] 创建Controller控制器
- [ ] 实现积分计算逻辑
- [ ] 实现评价计算逻辑
- [ ] 添加数据校验

### 阶段3：前端页面开发
- [ ] 学生端6个页面
- [ ] 教师端3个页面
- [ ] 管理员端4个页面
- [ ] 集成Chart.js图表
- [ ] 响应式布局优化

### 阶段4：测试与优化
- [ ] 单元测试
- [ ] 接口测试
- [ ] 性能优化
- [ ] 安全加固

### 阶段5：部署上线
- [ ] 打包应用
- [ ] 配置生产环境
- [ ] 数据迁移
- [ ] 正式上线

---

## 📝 七、注意事项

1. **权限控制**：使用Spring Security确保各角色只能访问授权功能
2. **数据校验**：前后端双重校验，防止非法数据
3. **事务管理**：积分计算、评价发布等操作使用@Transactional
4. **异常处理**：统一异常处理，友好错误提示
5. **日志记录**：关键操作记录日志，便于追溯
6. **性能优化**：排名数据可考虑Redis缓存
7. **数据安全**：敏感数据加密，SQL注入防护

---

## 🎯 八、预期成果

完成后的系统将实现：
- ✅ 完整的运动打卡流程
- ✅ 智能化的积分计算
- ✅ 科学的5维评价体系
- ✅ 实时的排名更新
- ✅ 丰富的数据可视化
- ✅ 完善的后台管理

---

**下一步建议**：按照实施步骤，先完成后端Service层，再开发前端页面。我可以继续帮你实现具体的Service和Controller代码。
