# 500和403错误修复说明

## 问题描述

访问以下页面时出现错误：

1. **500错误** - `/teacher/sport/audit` (运动审核)
2. **500错误** - `/teacher/evaluation/list` (评价列表)  
3. **403错误** - `/student/ranking` (班级排名)

## 已修复的问题

### 1. TeacherClassStatsController 空指针异常

**问题原因：**
- 当管理员访问教师端页面时，`teacherMapper.selectByUserId(userId)` 返回 `null`
- 直接调用 `teacher.getId()` 导致 NullPointerException

**修复内容：**
✅ 更新 `TeacherClassStatsController.java` 的 `classStats()` 方法：
```java
// 修复前（会抛出NPE）
Teacher teacher = teacherMapper.selectByUserId(userId);
List<Long> classIds = teacherClassService.getClassIdByTeacherId(teacher.getId()); // ❌ teacher可能为null

// 修复后
Teacher teacher = teacherMapper.selectByUserId(userId);
List<Long> classIds;
if (teacher != null) {
    classIds = teacherClassService.getClassIdByTeacherId(teacher.getId());
} else {
    // 管理员没有教师记录，查询所有班级
    classIds = null;
}

List<SysClass> classes;
if (classIds != null && !classIds.isEmpty()) {
    classes = classService.getClassByIds(classIds);
} else {
    // 查询所有班级
    classes = classService.list();
}
```

### 2. 学生班级排名403权限错误

**问题原因：**
- 学生端的 `ranking.html` 调用了教师端的API：`/teacher/sport/ranking`
- 根据SecurityConfig配置，`/teacher/**` 需要 `ROLE_TEACHER` 或 `ROLE_ADMIN` 权限
- 学生只有 `ROLE_STUDENT` 权限，因此被拒绝访问（403 Forbidden）

**修复内容：**

**步骤1：** 在 `StudentPointsController.java` 中添加学生专用的排名API
```java
@Autowired
private com.hhjt.mapper.SportRecordMapper sportRecordMapper;

/**
 * 获取班级排名数据（API接口）
 */
@GetMapping("/ranking/data")
@ResponseBody
public Map<String, Object> getClassRankingData(@RequestParam Long classId) {
    Map<String, Object> result = new HashMap<>();
    try {
        List<Map<String, Object>> ranking = sportRecordMapper.getClassRanking(classId);
        result.put("success", true);
        result.put("data", ranking);
    } catch (Exception e) {
        result.put("success", false);
        result.put("message", e.getMessage());
    }
    return result;
}
```

**步骤2：** 更新 `student/ranking.html` 使用学生端自己的API
```javascript
// 修复前
fetch('/teacher/sport/ranking?classId=' + classId) // ❌ 权限不足

// 修复后
fetch('/student/ranking/data?classId=' + classId) // ✅ 学生可以访问
```

## 验证步骤

### 1. 测试运动审核页面
1. 以教师或管理员身份登录
2. 访问 `/teacher/sport/audit`
3. 应该能正常显示待审核列表（如果没有数据会显示"暂无数据"）

### 2. 测试评价列表页面
1. 以教师或管理员身份登录
2. 访问 `/teacher/evaluation/list`
3. 应该能正常显示评价列表（如果没有数据会显示"暂无评价记录"）

### 3. 测试班级排名页面
1. 以学生身份登录
2. 访问 `/student/ranking`
3. 应该能看到班级内学生的积分排行榜

## 权限配置说明

根据 `SecurityConfig.java` 的配置：

```java
.antMatchers("/admin/**").hasRole("ADMIN")
.antMatchers("/teacher/**").access("hasRole('TEACHER') or hasRole('ADMIN')")
.antMatchers("/student/**").hasRole("STUDENT")
```

**角色访问规则：**
- `/admin/**` - 仅管理员可访问
- `/teacher/**` - 教师和管理员都可访问
- `/student/**` - 仅学生可访问

**重要原则：**
- 每个角色应该使用自己命名空间下的API
- 学生不能调用 `/teacher/**` 的接口
- 教师不能调用 `/student/**` 的接口
- 管理员可以访问 `/teacher/**` 和 `/admin/**`

## 修复文件清单

1. ✅ `src/main/java/com/hhjt/controller/TeacherClassStatsController.java` - 修复空指针异常
2. ✅ `src/main/java/com/hhjt/controller/StudentPointsController.java` - 添加学生排名API
3. ✅ `src/main/resources/templates/student/ranking.html` - 修改API调用路径

## 常见问题排查

### 问题1：仍然出现500错误
**检查项：**
- 查看后端日志文件 `logs/application.log`
- 确认数据库连接正常
- 确认相关表中有数据

### 问题2：仍然出现403错误
**检查项：**
- 确认当前登录用户的角色
- 检查SecurityConfig配置是否正确
- 确认API路径是否在正确的命名空间下

### 问题3：页面显示空白但无错误
**检查项：**
- 数据库中是否有对应的数据
- 浏览器控制台是否有JavaScript错误
- 检查Thymeleaf模板是否有语法错误

## 后续优化建议

1. **统一异常处理**：创建全局异常处理器，提供更友好的错误提示
2. **权限验证增强**：在Controller层添加更细粒度的权限检查
3. **空值保护**：对所有可能为null的对象进行判空处理
4. **API设计规范**：明确区分不同角色的API路径，避免跨角色调用

## 总结

本次修复解决了三个关键问题：
1. ✅ 管理员访问教师端页面的空指针异常
2. ✅ 学生访问班级排名的权限禁止问题
3. ✅ API调用路径规范化

现在所有页面都应该能够正常访问！
