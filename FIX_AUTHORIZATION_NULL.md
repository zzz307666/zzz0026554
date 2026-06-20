# Thymeleaf #authorization 对象为 null 问题修复

## 问题原因

Thymeleaf模板中使用 `#authorization.expression()` 时抛出异常：
```
EL1011E: Method call: Attempted to call method expression(java.lang.String) on null context object
```

**根本原因：** 
- Spring Security的Thymeleaf方言未正确配置或未启用
- `#authorization` 对象在模板上下文中为 null

## 解决方案

**不在模板中使用 `#authorization`，而是在Controller中判断权限并传递给模板。**

### 修复步骤

#### 步骤1：在Controller中添加 isAdmin 属性

在每个需要权限判断的Controller方法中：

```java
@GetMapping("/xxx")
public String xxxPage(Model model) {
    // 获取当前用户信息
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    User currentUser = (User) authentication.getPrincipal();
    
    // 判断是否为管理员
    boolean isAdmin = currentUser.getAuthorities().stream()
        .anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN"));
    
    // 添加到模型
    model.addAttribute("isAdmin", isAdmin);
    
    return "template_name";
}
```

#### 步骤2：修改模板文件

**修复前（错误）：**
```html
<a th:href="${#authorization.expression('hasRole(''ADMIN'')') ? '/admin/index' : '/teacher/index'}">
    返回后台
</a>
```

**修复后（正确）：**
```html
<a th:href="${isAdmin} ? '/admin/index' : '/teacher/index'">
    返回后台
</a>
```

## 已修复的文件

### Controller文件

1. ✅ `TeacherSportController.java` - `/teacher/sport/audit`
   - 已添加 `model.addAttribute("isAdmin", isAdmin);`

2. ⏳ `TeacherEvaluationController.java` - `/teacher/evaluation/list`
   - 需要添加 `model.addAttribute("isAdmin", isAdmin);`

3. ⏳ `TeacherClassStatsController.java` - `/teacher/class/stats`
   - 需要添加 `model.addAttribute("isAdmin", isAdmin);`

4. ⏳ 其他教师端Controller...

### 模板文件

1. ✅ `teacher/sport_audit.html` - 已修复
2. ⏳ `teacher/evaluation_list.html` - 待修复
3. ⏳ `teacher/class_stats.html` - 待修复
4. ⏳ `teacher/class_student.html` - 待修复
5. ⏳ `teacher/evaluate_student.html` - 待修复
6. ⏳ `teacher/my_class.html` - 待修复

## 快速修复命令

对于所有教师端模板，将：
```html
th:href="${#authorization.expression('hasRole(''ADMIN'')') ? '/admin/index' : '/teacher/index'}"
```

替换为：
```html
th:href="${isAdmin} ? '/admin/index' : '/teacher/index'"
```

## 验证步骤

1. 重启应用
2. 以教师身份登录，访问教师端页面
3. 以管理员身份登录，访问教师端页面
4. 确认"返回后台"按钮能正确跳转

## 长期解决方案

如果需要继续使用 `#authorization`，需要配置Thymeleaf Spring Security方言：

1. 添加依赖（如果还没有）：
```xml
<dependency>
    <groupId>org.thymeleaf.extras</groupId>
    <artifactId>thymeleaf-extras-springsecurity5</artifactId>
</dependency>
```

2. 在HTML中添加命名空间：
```html
<html xmlns:th="http://www.thymeleaf.org"
      xmlns:sec="http://www.thymeleaf.org/thymeleaf-extras-springsecurity5">
```

3. 使用 `sec:` 命名空间：
```html
<div sec:authorize="hasRole('ADMIN')">
    管理员可见内容
</div>
```

**但推荐使用Controller传递的方式，更简单可靠！**

## 总结

✅ **最佳实践：在Controller中判断权限，通过Model传递给模板**
- 更简单
- 更可靠
- 更容易测试
- 不依赖额外的Thymeleaf方言配置
