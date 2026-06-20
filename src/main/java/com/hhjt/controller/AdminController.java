package com.hhjt.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.hhjt.dto.UserQueryDTO;
import com.hhjt.entity.*;
import com.hhjt.mapper.StudentMapper;
import com.hhjt.mapper.TeacherMapper;
import com.hhjt.mapper.UserMapper;
import com.hhjt.service.AdminService;
import com.hhjt.service.ClassService;
import com.hhjt.service.TeacherClassService;
import com.hhjt.utils.ErrorCountUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Controller
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    private AdminService adminService;

    @Autowired
    private TeacherMapper teacherMapper;

    @Autowired
    private StudentMapper studentMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private ClassService classService;

    @Autowired
    private TeacherClassService teacherClassService;
    @Autowired
    private ErrorCountUtil errorCountUtil;

    // 管理员首页
    @GetMapping("/index")
    public String adminIndex(Model model) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User user = (User) authentication.getPrincipal();
        model.addAttribute("realName", user.getRealName());
        return "admin/index";
    }

    // 获取统计数据
    @GetMapping("/stats")
    @ResponseBody
    public Map<String, Object> getStats() {
        Map<String, Object> result = new HashMap<>();
        
        try {
            Map<String, Object> data = new HashMap<>();
            // 总用户数
            data.put("userCount", userMapper.selectCount(null));
            // 教师数
            data.put("teacherCount", teacherMapper.selectCount(null));
            // 学生数
            data.put("studentCount", studentMapper.selectCount(null));
            // 活跃用户数（当前启用状态的用户）
            data.put("activeCount", userMapper.selectCount(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<User>()
                    .eq(User::getStatus, 1)
            ));
            
            result.put("success", true);
            result.put("data", data);
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", e.getMessage());
            log.error("获取统计数据失败", e);
        }
        
        return result;
    }

    // 用户管理列表
    @GetMapping("/user/manage")
    public String userManage(Model model,
                             @RequestParam(value = "page", defaultValue = "1") Integer page,
                             @RequestParam(value = "size", defaultValue = "10") Integer size,
                             UserQueryDTO query) {
        IPage<User> userPage = adminService.getUserPage(page, size, query);
        model.addAttribute("page", userPage);
        model.addAttribute("query", query);
        return "admin/user_manage";
    }

    // 新增用户页面
    @GetMapping("/user/add")
    public String addUserPage(Model model) {
        // 传递所有班级列表
        model.addAttribute("classList", classService.getAllClass());
        return "admin/user_add";
    }

    // 新增用户提交（联动师生信息+班级）
    // ==================== 修复1：管理员端新增用户 ====================
    @PostMapping("/user/add")
    @ResponseBody
    public Map<String, Object> addUser(@RequestBody Map<String, Object> requestData) {
        Map<String, Object> result = new HashMap<>();
        try {
            Map<String, Object> userData = (Map<String, Object>) requestData.get("user");
            User user = new User();
            user.setUsername((String) userData.get("username"));
            user.setRealName((String) userData.get("realName"));
            user.setPhone((String) Optional.ofNullable(userData.get("phone")).orElse(""));
            user.setEmail((String) Optional.ofNullable(userData.get("email")).orElse(""));
            user.setRoleId(toLong(userData.get("roleId")));
            // 修复：直接传递前端密码（可能为null），服务层兜底加密
            user.setPassword((String) userData.get("password"));


            Teacher teacher = null;
            Student student = null;
            List<Long> classIdList = null;

            if (user.getRoleId() == 2L) { // 教师
                Map<String, Object> teacherData = (Map<String, Object>) requestData.get("teacher");
                teacher = new Teacher();
                teacher.setSubject((String) teacherData.get("subject"));
                Integer gender = toInteger(teacherData.get("gender"));
                teacher.setGender(gender == null ? 0 : gender);
                // 获取选中的班级ID列表
                classIdList = toLongList(teacherData.get("classIdList"));
            } else if (user.getRoleId() == 3L) { // 学生
                Map<String, Object> studentData = (Map<String, Object>) requestData.get("student");
                student = new Student();
                student.setClassId(toLong(studentData.get("classId")));
                Integer gender = toInteger(studentData.get("gender"));
                student.setGender(gender == null ? 0 : gender);
                String birthDateStr = (String) studentData.get("birthDate");
                if (birthDateStr != null && !birthDateStr.isEmpty()) {
                    student.setBirthDate(LocalDate.parse(birthDateStr));
                }
            }

            boolean success = adminService.addUserWithRelate(user, teacher, student, classIdList);
            result.put("success", success);
            result.put("message", success ? "添加成功" : "添加失败");
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "添加失败：" + e.getMessage());
            log.error("新增用户异常", e);
        }
        return result;
    }

    // 教师管理列表【核心修改：新增年级/班级搜索参数】
    @GetMapping("/teacher/manage")
    public String teacherManage(Model model,
                                @RequestParam(value = "realName", required = false) String realName,
                                @RequestParam(value = "teacherNo", required = false) String teacherNo,
                                @RequestParam(value = "grade", required = false) String grade,
                                @RequestParam(value = "className", required = false) String className) {
        model.addAttribute("teachers", adminService.getTeacherList(realName, teacherNo, grade, className));
        // 搜索条件回显
        model.addAttribute("realName", realName);
        model.addAttribute("teacherNo", teacherNo);
        model.addAttribute("grade", grade);
        model.addAttribute("className", className);
        return "admin/teacher_manage";
    }

    // 学生管理列表【核心修改：新增年级搜索参数】
    @GetMapping("/student/manage")
    public String studentManage(Model model,
                                @RequestParam(value = "realName", required = false) String realName,
                                @RequestParam(value = "studentNo", required = false) String studentNo,
                                @RequestParam(value = "className", required = false) String className,
                                @RequestParam(value = "grade", required = false) String grade) {
        model.addAttribute("students", adminService.getStudentList(realName, studentNo, className, grade));
        // 搜索条件回显
        model.addAttribute("realName", realName);
        model.addAttribute("studentNo", studentNo);
        model.addAttribute("className", className);
        model.addAttribute("grade", grade);
        return "admin/student_manage";
    }

    // 查看所有学生（包括禁用的）- 已废弃，使用 /admin/student/manage 替代
    // @GetMapping("/student/all")
    // public String allStudentManage(Model model,
    //                                @RequestParam(value = "realName", required = false) String realName,
    //                                @RequestParam(value = "studentNo", required = false) String studentNo,
    //                                @RequestParam(value = "className", required = false) String className) {
    //     model.addAttribute("students", adminService.getAllStudentList(realName, studentNo, className));
    //     // 搜索条件回显
    //     model.addAttribute("realName", realName);
    //     model.addAttribute("studentNo", studentNo);
    //     model.addAttribute("className", className);
    //     return "admin/student_all";
    // }

    // 新增教师页面
    @GetMapping("/teacher/add")
    public String addTeacherPage(Model model) {
        // 传递所有班级列表
        model.addAttribute("classList", classService.getAllClass());
        return "admin/teacher_add";
    }

    // 新增教师提交（含班级关联）
    // ==================== 修复2：管理员端新增教师 ====================
    @PostMapping("/teacher/add")
    @ResponseBody
    public Map<String, Object> addTeacher(@RequestBody Map<String, Object> requestData) {
        Map<String, Object> result = new HashMap<>();
        try {
            Map<String, Object> userData = (Map<String, Object>) requestData.get("user");
            Map<String, Object> teacherData = (Map<String, Object>) requestData.get("teacher");
            List<Long> classIdList = toLongList(teacherData.get("classIdList"));

            User user = new User();
            user.setUsername((String) userData.get("username"));
            user.setRealName((String) userData.get("realName"));
            user.setPhone((String) userData.get("phone"));
            user.setEmail((String) userData.get("email"));
            // 修复：删除硬编码明文密码，服务层兜底

            Teacher teacher = new Teacher();
            teacher.setSubject((String) teacherData.get("subject"));
            Integer gender = toInteger(teacherData.get("gender"));
            teacher.setGender(gender == null ? 0 : gender);

            boolean success = adminService.addTeacher(user, teacher, classIdList);
            result.put("success", success);
            result.put("message", success ? "添加成功" : "添加失败");
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "添加失败：" + e.getMessage());
            log.error("新增教师异常", e);
        }
        return result;
    }

    // 新增学生页面
    @GetMapping("/student/add")
    public String addStudentPage(Model model) {
        // 传递所有班级列表
        model.addAttribute("classList", classService.getAllClass());
        return "admin/student_add";
    }

    // 新增学生提交（含班级关联）
    // ==================== 修复3：管理员端新增学生 ====================
    @PostMapping("/student/add")
    @ResponseBody
    public Map<String, Object> addStudent(@RequestBody Map<String, Object> requestData) {
        Map<String, Object> result = new HashMap<>();
        try {
            Map<String, Object> userData = (Map<String, Object>) requestData.get("user");
            Map<String, Object> studentData = (Map<String, Object>) requestData.get("student");

            User user = new User();
            user.setUsername((String) userData.get("username"));
            user.setRealName((String) userData.get("realName"));
            user.setPhone((String) userData.get("phone"));
            user.setEmail((String) userData.get("email"));
            // 修复：删除硬编码明文密码，服务层兜底

            Student student = new Student();
            // 班级ID必选
            Long classId = toLong(studentData.get("classId"));
            if (classId == null) {
                throw new RuntimeException("请选择学生所属班级");
            }
            student.setClassId(classId);
            Integer gender = toInteger(studentData.get("gender"));
            student.setGender(gender == null ? 0 : gender);
            Object birthDateObj = studentData.get("birthDate");
            if (birthDateObj != null && !"".equals(birthDateObj)) {
                if (birthDateObj instanceof String) {
                    String dateStr = (String) birthDateObj;
                    if (!dateStr.isEmpty()) {
                        student.setBirthDate(LocalDate.parse(dateStr));
                    }
                } else if (birthDateObj instanceof LocalDate) {
                    student.setBirthDate((LocalDate) birthDateObj);
                }
            }

            boolean success = adminService.addStudent(user, student);
            result.put("success", success);
            result.put("message", success ? "添加成功" : "添加失败");
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "添加失败：" + e.getMessage());
            log.error("新增学生异常", e);
        }
        return result;
    }

    // 编辑教师页面
    @GetMapping("/teacher/edit/{userId}")
    public String editTeacherPage(@PathVariable("userId") Long userId, Model model) {
        Teacher teacher = teacherMapper.selectByUserId(userId);
        User user = userMapper.selectById(userId);
        // 所有班级列表
        model.addAttribute("classList", classService.getAllClass());
        // 教师已选班级ID列表
        model.addAttribute("selectedClassIds", teacherClassService.getClassIdByTeacherId(teacher.getId()));
        model.addAttribute("teacher", teacher);
        model.addAttribute("user", user);
        return "admin/teacher_edit";
    }

    // 编辑教师提交（含班级关联）
    @PostMapping("/teacher/edit")
    @ResponseBody
    public Map<String, Object> editTeacher(@RequestBody Map<String, Object> requestData) {
        Map<String, Object> result = new HashMap<>();
        try {
            Map<String, Object> userData = (Map<String, Object>) requestData.get("user");
            Map<String, Object> teacherData = (Map<String, Object>) requestData.get("teacher");
            // 安全转换班级ID列表，核心修复泛型擦除异常
            List<Long> classIdList = toLongList(teacherData.get("classIdList"));

            // 安全转换ID，避免空指针和类型转换异常
            Long userId = toLong(userData.get("id"));
            if (userId == null) {
                throw new RuntimeException("用户ID不能为空");
            }
            Long teacherId = toLong(teacherData.get("id"));
            if (teacherId == null) {
                throw new RuntimeException("教师ID不能为空");
            }
            Integer status = toInteger(userData.get("status"));
            if (status == null) {
                throw new RuntimeException("账号状态不能为空");
            }
            Integer gender = toInteger(teacherData.get("gender"));
            gender = gender == null ? 0 : gender;

            User user = new User();
            user.setId(userId);
            user.setUsername((String) userData.get("username"));
            user.setRealName((String) userData.get("realName"));
            user.setPhone((String) Optional.ofNullable(userData.get("phone")).orElse(""));
            user.setEmail((String) Optional.ofNullable(userData.get("email")).orElse(""));
            user.setStatus(status);
            user.setRoleId(2L);

            Teacher teacher = new Teacher();
            teacher.setId(teacherId);
            teacher.setUserId(userId);
            teacher.setTeacherNo((String) teacherData.get("teacherNo"));
            teacher.setSubject((String) teacherData.get("subject"));
            teacher.setGender(gender);

            boolean success = adminService.updateTeacher(user, teacher, classIdList);
            result.put("success", success);
            result.put("message", success ? "更新成功" : "更新失败");
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "更新失败：" + e.getMessage());
            log.error("编辑教师异常", e);
        }
        return result;
    }

    // 编辑学生页面
    @GetMapping("/student/edit/{userId}")
    public String editStudentPage(@PathVariable("userId") Long userId, Model model) {
        Student student = studentMapper.selectByUserId(userId);
        User user = userMapper.selectById(userId);
        // 所有班级列表
        model.addAttribute("classList", classService.getAllClass());
        model.addAttribute("student", student);
        model.addAttribute("user", user);
        return "admin/student_edit";
    }

    // 编辑学生提交（含班级关联）
    @PostMapping("/student/edit")
    @ResponseBody
    public Map<String, Object> editStudent(@RequestBody Map<String, Object> requestData) {
        Map<String, Object> result = new HashMap<>();
        try {
            Map<String, Object> userData = (Map<String, Object>) requestData.get("user");
            Map<String, Object> studentData = (Map<String, Object>) requestData.get("student");

            // 安全转换ID
            Long userId = toLong(userData.get("id"));
            if (userId == null) {
                throw new RuntimeException("用户ID不能为空");
            }
            Long studentId = toLong(studentData.get("id"));
            if (studentId == null) {
                throw new RuntimeException("学生ID不能为空");
            }
            Integer status = toInteger(userData.get("status"));
            if (status == null) {
                throw new RuntimeException("账号状态不能为空");
            }
            Long classId = toLong(studentData.get("classId"));
            if (classId == null) {
                throw new RuntimeException("请选择学生所属班级");
            }
            Integer gender = toInteger(studentData.get("gender"));
            gender = gender == null ? 0 : gender;

            User user = new User();
            user.setId(userId);
            user.setUsername((String) userData.get("username"));
            user.setRealName((String) userData.get("realName"));
            user.setPhone((String) Optional.ofNullable(userData.get("phone")).orElse(""));
            user.setEmail((String) Optional.ofNullable(userData.get("email")).orElse(""));
            user.setStatus(status);
            user.setRoleId(3L);

            Student student = new Student();
            student.setId(studentId);
            student.setUserId(userId);
            student.setStudentNo((String) studentData.get("studentNo"));
            student.setClassId(classId);
            student.setGender(gender);
            String birthDateStr = (String) Optional.ofNullable(studentData.get("birthDate")).orElse("");
            if (!birthDateStr.isEmpty()) {
                student.setBirthDate(LocalDate.parse(birthDateStr));
            }

            boolean success = adminService.updateStudent(user, student);
            result.put("success", success);
            result.put("message", success ? "更新成功" : "更新失败");
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "更新失败：" + e.getMessage());
            log.error("编辑学生异常", e);
        }
        return result;
    }

    // 用户管理删除
    @PostMapping("/user/delete/{id}")
    @ResponseBody
    public Map<String, Object> deleteUser(@PathVariable("id") Long id) {
        Map<String, Object> result = new HashMap<>();
        try {
            boolean success = adminService.deleteUser(id);
            result.put("success", success);
            result.put("message", success ? "删除成功" : "删除失败");
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "删除失败：" + e.getMessage());
        }
        return result;
    }

    // 更新用户状态
    // 改造【更新用户状态】接口
    @PostMapping("/user/status/{id}")
    @ResponseBody
    public Map<String, Object> updateUserStatus(@PathVariable("id") Long id,
                                                @RequestParam("status") Integer status) {
        Map<String, Object> result = new HashMap<>();
        try {
            User user = userMapper.selectById(id);
            if (user == null) {
                result.put("success", false);
                result.put("message", "用户不存在");
                return result;
            }
            if (user.isAdmin()) {
                throw new RuntimeException("管理员账户不可禁用/解锁");
            }
            user.setStatus(status);
            user.setUpdateTime(LocalDateTime.now());
            // 解锁时（status=1）重置错误计数
            if (status == 1) {
                errorCountUtil.resetErrorCount(user);
            }
            boolean success = userMapper.updateById(user) > 0;
            result.put("success", success);
            result.put("message", success ? "操作成功" : "操作失败");
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "操作失败：" + e.getMessage());
        }
        return result;
    }

    // 编辑用户页面
    @GetMapping("/user/edit/{id}")
    public String editUserPage(@PathVariable("id") Long id, Model model) {
        User user = userMapper.selectById(id);
        model.addAttribute("user", user);
        model.addAttribute("classList", classService.getAllClass());
        // 教师/学生关联信息
        if (user.getRoleId() == 2L) {
            Teacher teacher = teacherMapper.selectByUserId(user.getId());
            model.addAttribute("teacher", teacher);
            model.addAttribute("selectedClassIds", teacherClassService.getClassIdByTeacherId(teacher.getId()));
        } else if (user.getRoleId() == 3L) {
            Student student = studentMapper.selectByUserId(user.getId());
            model.addAttribute("student", student);
        }
        return "admin/user_edit";
    }

    // 编辑用户提交
    @PostMapping("/user/edit")
    @ResponseBody
    public Map<String, Object> editUser(@RequestBody User user) {
        Map<String, Object> result = new HashMap<>();
        try {
            boolean success = adminService.updateUser(user);
            result.put("success", success);
            result.put("message", success ? "更新成功" : "更新失败");
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "更新失败：" + e.getMessage());
        }
        return result;
    }

    // 重置密码
    @PostMapping("/user/reset-password/{id}")
    @ResponseBody
    public Map<String, Object> resetPassword(@PathVariable("id") Long id) {
        Map<String, Object> result = new HashMap<>();
        try {
            boolean success = adminService.resetPassword(id);
            result.put("success", success);
            result.put("message", success ? "密码重置成功" : "密码重置失败");
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "密码重置失败：" + e.getMessage());
        }
        return result;
    }

    // 编辑用户联动更新师生信息
    @PostMapping("/user/edit/relate")
    @ResponseBody
    public Map<String, Object> editUserWithRelate(@RequestBody Map<String, Object> requestData) {
        Map<String, Object> result = new HashMap<>();
        try {
            Map<String, Object> userData = (Map<String, Object>) requestData.get("user");
            User user = new User();
            // 安全转换用户ID
            Long userId = toLong(userData.get("id"));
            if (userId == null) {
                throw new RuntimeException("用户ID不能为空");
            }
            Long roleId = toLong(userData.get("roleId"));
            if (roleId == null) {
                throw new RuntimeException("用户角色不能为空");
            }
            Integer status = toInteger(userData.get("status"));
            status = status == null ? 1 : status;

            user.setId(userId);
            user.setUsername((String) userData.get("username"));
            user.setRealName((String) userData.get("realName"));
            user.setPhone((String) Optional.ofNullable(userData.get("phone")).orElse(""));
            user.setEmail((String) Optional.ofNullable(userData.get("email")).orElse(""));
            user.setRoleId(roleId);
            user.setStatus(status);
            user.setPassword((String) Optional.ofNullable(userData.get("password")).orElse(""));

            Teacher teacher = null;
            Student student = null;
            List<Long> classIdList = null;

            if (user.getRoleId() == 2L) {
                Map<String, Object> teacherData = (Map<String, Object>) requestData.get("teacher");
                teacher = new Teacher();
                // 安全转换教师ID
                Long teacherId = toLong(teacherData.get("id"));
                if (teacherId != null) {
                    teacher.setId(teacherId);
                }
                teacher.setTeacherNo((String) teacherData.get("teacherNo"));
                teacher.setSubject((String) teacherData.get("subject"));
                Integer gender = toInteger(teacherData.get("gender"));
                teacher.setGender(gender == null ? 0 : gender);
                // 安全转换班级列表
                classIdList = toLongList(teacherData.get("classIdList"));
            } else if (user.getRoleId() == 3L) {
                Map<String, Object> studentData = (Map<String, Object>) requestData.get("student");
                student = new Student();
                // 安全转换学生ID
                Long studentId = toLong(studentData.get("id"));
                if (studentId != null) {
                    student.setId(studentId);
                }
                student.setStudentNo((String) studentData.get("studentNo"));
                // 安全转换班级ID
                Long classId = toLong(studentData.get("classId"));
                student.setClassId(classId);
                Integer gender = toInteger(studentData.get("gender"));
                student.setGender(gender == null ? 0 : gender);
                String birthDateStr = (String) Optional.ofNullable(studentData.get("birthDate")).orElse("");
                if (!birthDateStr.isEmpty()) {
                    student.setBirthDate(LocalDate.parse(birthDateStr));
                }
            }

            boolean success = adminService.updateUserWithRelate(user, teacher, student, classIdList);
            result.put("success", success);
            result.put("message", success ? "更新成功" : "更新失败");
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "更新失败：" + e.getMessage());
            log.error("编辑用户关联信息异常", e);
        }
        return result;
    }

    // 教师管理删除
    @PostMapping("/teacher/delete/{userId}")
    @ResponseBody
    public Map<String, Object> deleteTeacher(@PathVariable("userId") Long userId) {
        Map<String, Object> result = new HashMap<>();
        try {
            boolean success = adminService.deleteTeacher(userId);
            result.put("success", success);
            result.put("message", success ? "删除成功" : "删除失败");
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "删除失败：" + e.getMessage());
        }
        return result;
    }

    // 学生管理删除
    @PostMapping("/student/delete/{userId}")
    @ResponseBody
    public Map<String, Object> deleteStudent(@PathVariable("userId") Long userId) {
        Map<String, Object> result = new HashMap<>();
        try {
            boolean success = adminService.deleteStudent(userId);
            result.put("success", success);
            result.put("message", success ? "删除成功" : "删除失败");
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "删除失败：" + e.getMessage());
        }
        return result;
    }

    // ==================== 安全类型转换工具方法（核心解决类型转换异常）====================
    // 安全转换Object为Long，处理null、字符串数字，转换失败返回null
    private Long toLong(Object obj) {
        if (obj == null) {
            return null;
        }
        if (obj instanceof Long) {
            return (Long) obj;
        }
        if (obj instanceof Integer) {
            return ((Integer) obj).longValue();
        }
        if (obj instanceof String) {
            String str = ((String) obj).trim();
            if (str.isEmpty()) {
                return null;
            }
            try {
                return Long.parseLong(str);
            } catch (NumberFormatException e) {
                log.warn("字符串转Long失败，值：{}", str);
                return null;
            }
        }
        return null;
    }

    // 安全转换Object为Integer，处理null、字符串数字，转换失败返回null
    private Integer toInteger(Object obj) {
        if (obj == null) {
            return null;
        }
        if (obj instanceof Integer) {
            return (Integer) obj;
        }
        if (obj instanceof Long) {
            return ((Long) obj).intValue();
        }
        if (obj instanceof String) {
            String str = ((String) obj).trim();
            if (str.isEmpty()) {
                return null;
            }
            try {
                return Integer.parseInt(str);
            } catch (NumberFormatException e) {
                log.warn("字符串转Integer失败，值：{}", str);
                return null;
            }
        }
        return null;
    }

    // 安全转换List<Object>为List<Long>，过滤无效值
    private List<Long> toLongList(Object obj) {
        if (obj == null) {
            return Collections.emptyList();
        }
        if (obj instanceof List<?>) {
            return ((List<?>) obj).stream()
                    .map(this::toLong)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
        }
        return Collections.emptyList();
    }

    // ==================== 快捷路径重定向 ====================
    
    /**
     * 公告管理页面快捷访问 /admin/announcements
     */
    @GetMapping("/announcements")
    public String announcementsRedirect() {
        return "redirect:/admin/announcement/announcements";
    }

    /**
     * 数据导入导出页面快捷访问 /admin/data-import-export
     */
    @GetMapping("/data-import-export")
    public String dataImportExportRedirect() {
        return "redirect:/admin/data/data-import-export";
    }
}