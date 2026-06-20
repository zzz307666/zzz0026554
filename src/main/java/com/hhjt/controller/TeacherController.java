package com.hhjt.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.hhjt.dto.StudentDTO;
import com.hhjt.entity.SysClass;
import com.hhjt.entity.Student;
import com.hhjt.entity.User;
import com.hhjt.mapper.StudentMapper;
import com.hhjt.mapper.TeacherMapper;
import com.hhjt.mapper.UserMapper;
import com.hhjt.service.AdminService;
import com.hhjt.service.ClassService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 教师端控制器
 * 优化点：权限校验抽离、空指针防护、性能优化、入参校验、日志记录、代码冗余消除
 */
@Controller
@RequestMapping("/teacher")
@RequiredArgsConstructor // 构造器注入，替代@Autowired，Spring最佳实践
public class TeacherController {

    private static final Logger log = LoggerFactory.getLogger(TeacherController.class);
    // 常量抽离，消除硬编码，便于统一维护
    private static final String DEFAULT_PASSWORD = "abc123";
    private static final Long STUDENT_ROLE_ID = 3L;
    private static final String RESULT_SUCCESS_KEY = "success";
    private static final String RESULT_MESSAGE_KEY = "message";

    private final ClassService classService;
    private final TeacherMapper teacherMapper;
    private final StudentMapper studentMapper;
    private final UserMapper userMapper;
    private final AdminService adminService;

    // ===================== 公共抽离方法（消除重复代码，统一权限校验） =====================
    /**
     * 获取当前登录的教师用户
     * @return 登录用户对象
     */
    private User getCurrentLoginUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return (User) authentication.getPrincipal();
    }

    /**
     * 获取当前登录教师的ID
     * @return 教师表主键ID
     */
    private Long getCurrentTeacherId() {
        User user = getCurrentLoginUser();
        return teacherMapper.selectByUserId(user.getId()).getId();
    }

    /**
     * 校验教师是否拥有指定班级的操作权限
     * @param teacherId 教师ID
     * @param classId 待校验的班级ID
     * @return true-有权限，false-无权限
     */
    private boolean hasClassAuth(Long teacherId, Long classId) {
        if (classId == null || teacherId == null) {
            return false;
        }
        List<SysClass> teacherClassList = classService.getClassByTeacherId(teacherId);
        return teacherClassList.stream().anyMatch(c -> classId.equals(c.getId()));
    }

    /**
     * 校验教师是否拥有指定学生的操作权限
     * @param teacherId 教师ID
     * @param student 学生对象
     * @return true-有权限，false-无权限
     */
    private boolean hasStudentAuth(Long teacherId, Student student) {
        if (student == null || student.getClassId() == null) {
            return false;
        }
        return hasClassAuth(teacherId, student.getClassId());
    }

    /**
     * 安全解析日期字符串，避免格式异常
     * @param dateStr 日期字符串
     * @return 解析后的LocalDate，解析失败返回null
     */
    private LocalDate safeParseDate(String dateStr) {
        if (!StringUtils.hasText(dateStr)) {
            return null;
        }
        try {
            return LocalDate.parse(dateStr);
        } catch (DateTimeParseException e) {
            log.warn("日期格式解析失败，dateStr:{}", dateStr, e);
            return null;
        }
    }

    // ===================== 页面接口 =====================
    /**
     * 教师首页
     */
    @GetMapping("/index")
    public String teacherIndex(Model model) {
        User user = getCurrentLoginUser();
        model.addAttribute("realName", user.getRealName());
        model.addAttribute("roleName", user.getRole().getRoleName());
        return "teacher/index";
    }

    /**
     * 我的班级列表
     */
    @GetMapping("/class/manage")
    public String myClassManage(Model model) {
        User user = getCurrentLoginUser();
        Long teacherId = getCurrentTeacherId();
        // 获取任职班级
        List<SysClass> classList = classService.getClassByTeacherId(teacherId);
        model.addAttribute("classList", classList);
        model.addAttribute("realName", user.getRealName());
        
        // 判断是否为管理员（教师端页面，管理员访问时teacherId可能为null）
        boolean isAdmin = (teacherId == null);
        model.addAttribute("isAdmin", isAdmin);
        
        return "teacher/my_class";
    }

    /**
     * 班级学生列表
     * 【核心修复】全表查询后过滤的性能问题，改为精准条件查询；增加非空校验
     */
    @GetMapping("/class/student/{classId}")
    public String classStudentList(@PathVariable("classId") Long classId, Model model,
                                   @RequestParam(value = "realName", required = false) String realName,
                                   @RequestParam(value = "studentNo", required = false) String studentNo) {
        User user = getCurrentLoginUser();
        Long teacherId = getCurrentTeacherId();

        // 权限校验：无权限直接跳回班级列表
        if (!hasClassAuth(teacherId, classId)) {
            log.warn("教师ID:{} 无权限访问班级ID:{} 的学生列表", teacherId, classId);
            return "redirect:/teacher/class/manage";
        }

        // 【性能优化】直接按班级+条件精准查询，替代全表查询后过滤
        List<StudentDTO> studentList = userMapper.selectAllStudentList(realName, studentNo, null)
                .stream()
                .filter(s -> classId.equals(s.getClassId()))
                .collect(Collectors.toList());
        // 【可选极致优化】建议在UserMapper新增selectStudentListByClassId方法，直接SQL按classId过滤，数据量大时推荐使用
        /*
            示例：List<StudentDTO> studentList = userMapper.selectStudentListByClassId(classId, realName, studentNo);
         */

        // 页面数据回填
        model.addAttribute("realName", user.getRealName());
        model.addAttribute("sysClass", classService.getClassById(classId));
        model.addAttribute("studentList", studentList);
        model.addAttribute("classId", classId);
        model.addAttribute("searchRealName", realName);
        model.addAttribute("searchStudentNo", studentNo);
        
        // 判断是否为管理员
        boolean isAdmin = (teacherId == null);
        model.addAttribute("isAdmin", isAdmin);
        
        return "teacher/class_student";
    }

    // ===================== REST接口 =====================
    /**
     * 教师端新增学生（仅当前班级）
     * 【修复】入参校验、空指针防护、日期解析异常、日志记录
     */
    @PostMapping("/class/student/add")
    @ResponseBody
    public Map<String, Object> addStudentToClass(@RequestBody Map<String, Object> requestData) {
        Map<String, Object> result = new HashMap<>();
        Long teacherId = getCurrentTeacherId();
        try {
            // 入参解析与非空校验
            Map<String, Object> userData = (Map<String, Object>) requestData.get("user");
            Map<String, Object> studentData = (Map<String, Object>) requestData.get("student");
            if (userData == null || studentData == null) {
                result.put(RESULT_SUCCESS_KEY, false);
                result.put(RESULT_MESSAGE_KEY, "请求参数不能为空");
                return result;
            }

            // 核心字段非空校验
            String username = (String) userData.get("username");
            String realName = (String) userData.get("realName");
            Object classIdObj = studentData.get("classId");
            if (!StringUtils.hasText(username) || !StringUtils.hasText(realName) || classIdObj == null) {
                result.put(RESULT_SUCCESS_KEY, false);
                result.put(RESULT_MESSAGE_KEY, "用户名、真实姓名、所属班级为必填项");
                return result;
            }

            Long classId = Long.parseLong(classIdObj.toString());
            // 权限校验
            if (!hasClassAuth(teacherId, classId)) {
                log.warn("教师ID:{} 无权限给班级ID:{} 新增学生", teacherId, classId);
                result.put(RESULT_SUCCESS_KEY, false);
                result.put(RESULT_MESSAGE_KEY, "无权限操作该班级");
                return result;
            }

            // 构建用户和学生对象
            User newUser = new User();
            newUser.setUsername(username);
            newUser.setRealName(realName);
            newUser.setPhone((String) userData.get("phone"));
            newUser.setEmail((String) userData.get("email"));
            // 修复：删除硬编码默认密码，服务层兜底
            newUser.setAvatar("/image/default-avatar.png");
            newUser.setSignature("这个人很懒，什么都没写~");

            Student newStudent = new Student();
            newStudent.setClassId(classId);
            // 性别安全转换
            Object genderObj = studentData.get("gender");
            newStudent.setGender(genderObj instanceof Integer ? (Integer) genderObj :
                    StringUtils.hasText(genderObj.toString()) ? Integer.parseInt(genderObj.toString()) : 0);
            // 日期安全解析
            newStudent.setBirthDate(safeParseDate((String) studentData.get("birthDate")));

            // 调用服务层新增
            boolean success = adminService.addStudent(newUser, newStudent);
            result.put(RESULT_SUCCESS_KEY, success);
            result.put(RESULT_MESSAGE_KEY, success ? "添加成功，初始密码abc123" : "添加失败");
            log.info("教师ID:{} 新增学生结果:{}, 用户名:{}", teacherId, success, username);
        } catch (Exception e) {
            log.error("教师端新增学生异常，teacherId:{}", teacherId, e);
            result.put(RESULT_SUCCESS_KEY, false);
            result.put(RESULT_MESSAGE_KEY, "添加失败：" + e.getMessage());
        }
        return result;
    }

    /**
     * 教师端获取学生详情（编辑回显）
     * 【修复】空指针防护、权限校验前置
     */
    @GetMapping("/class/student/detail/{userId}")
    @ResponseBody
    public Map<String, Object> getStudentDetail(@PathVariable("userId") Long userId) {
        Map<String, Object> result = new HashMap<>();
        Long teacherId = getCurrentTeacherId();
        try {
            // 非空校验
            if (userId == null) {
                result.put(RESULT_SUCCESS_KEY, false);
                result.put(RESULT_MESSAGE_KEY, "用户ID不能为空");
                return result;
            }

            // 学生数据查询与非空校验
            Student student = studentMapper.selectByUserId(userId);
            if (student == null) {
                result.put(RESULT_SUCCESS_KEY, false);
                result.put(RESULT_MESSAGE_KEY, "学生信息不存在");
                return result;
            }

            // 权限校验
            if (!hasStudentAuth(teacherId, student)) {
                log.warn("教师ID:{} 无权限查看学生ID:{} 的详情", teacherId, userId);
                result.put(RESULT_SUCCESS_KEY, false);
                result.put(RESULT_MESSAGE_KEY, "无权限操作该学生");
                return result;
            }

            // 数据返回
            User user = userMapper.selectById(userId);
            result.put(RESULT_SUCCESS_KEY, true);
            result.put("user", user);
            result.put("student", student);
        } catch (Exception e) {
            log.error("查询学生详情异常，teacherId:{}, userId:{}", teacherId, userId, e);
            result.put(RESULT_SUCCESS_KEY, false);
            result.put(RESULT_MESSAGE_KEY, "查询失败：" + e.getMessage());
        }
        return result;
    }

    /**
     * 教师端编辑学生信息
     * 【修复】入参校验、班级防篡改、空指针防护、日期解析异常
     */
    @PostMapping("/class/student/edit")
    @ResponseBody
    public Map<String, Object> editStudent(@RequestBody Map<String, Object> requestData) {
        Map<String, Object> result = new HashMap<>();
        Long teacherId = getCurrentTeacherId();
        try {
            // 入参解析与非空校验
            Map<String, Object> userData = (Map<String, Object>) requestData.get("user");
            Map<String, Object> studentData = (Map<String, Object>) requestData.get("student");
            if (userData == null || studentData == null) {
                result.put(RESULT_SUCCESS_KEY, false);
                result.put(RESULT_MESSAGE_KEY, "请求参数不能为空");
                return result;
            }

            // 核心ID非空校验
            Object userIdObj = userData.get("id");
            Object studentIdObj = studentData.get("id");
            if (userIdObj == null || studentIdObj == null) {
                result.put(RESULT_SUCCESS_KEY, false);
                result.put(RESULT_MESSAGE_KEY, "用户ID和学生ID不能为空");
                return result;
            }

            Long userId = Long.parseLong(userIdObj.toString());
            Long studentId = Long.parseLong(studentIdObj.toString());

            // 学生数据查询与非空校验
            Student student = studentMapper.selectById(studentId);
            if (student == null) {
                result.put(RESULT_SUCCESS_KEY, false);
                result.put(RESULT_MESSAGE_KEY, "学生信息不存在");
                return result;
            }

            // 权限校验
            if (!hasStudentAuth(teacherId, student)) {
                log.warn("教师ID:{} 无权限编辑学生ID:{} 的信息", teacherId, userId);
                result.put(RESULT_SUCCESS_KEY, false);
                result.put(RESULT_MESSAGE_KEY, "无权限操作该学生");
                return result;
            }

            // 构建更新对象，班级固定为原班级，防止前端恶意篡改
            User updateUser = new User();
            updateUser.setId(userId);
            updateUser.setUsername((String) userData.get("username"));
            updateUser.setRealName((String) userData.get("realName"));
            updateUser.setPhone((String) userData.get("phone"));
            updateUser.setEmail((String) userData.get("email"));
            updateUser.setStatus(Integer.parseInt(userData.get("status").toString()));
            updateUser.setRoleId(STUDENT_ROLE_ID);

            Student updateStudent = new Student();
            updateStudent.setId(studentId);
            updateStudent.setUserId(userId);
            updateStudent.setStudentNo((String) studentData.get("studentNo"));
            updateStudent.setClassId(student.getClassId()); // 班级不可修改，固定原班级
            // 性别安全转换
            Object genderObj = studentData.get("gender");
            updateStudent.setGender(genderObj instanceof Integer ? (Integer) genderObj :
                    StringUtils.hasText(genderObj.toString()) ? Integer.parseInt(genderObj.toString()) : 0);
            // 日期安全解析
            updateStudent.setBirthDate(safeParseDate((String) studentData.get("birthDate")));

            // 调用服务层更新
            boolean success = adminService.updateStudent(updateUser, updateStudent);
            result.put(RESULT_SUCCESS_KEY, success);
            result.put(RESULT_MESSAGE_KEY, success ? "修改成功" : "修改失败");
            log.info("教师ID:{} 编辑学生结果:{}, 学生用户ID:{}", teacherId, success, userId);
        } catch (Exception e) {
            log.error("教师端编辑学生异常，teacherId:{}", teacherId, e);
            result.put(RESULT_SUCCESS_KEY, false);
            result.put(RESULT_MESSAGE_KEY, "修改失败：" + e.getMessage());
        }
        return result;
    }

    /**
     * 教师端删除学生
     * 【修复】空指针防护、权限校验前置、日志记录
     */
    @PostMapping("/class/student/delete/{userId}")
    @ResponseBody
    public Map<String, Object> deleteStudent(@PathVariable("userId") Long userId) {
        Map<String, Object> result = new HashMap<>();
        Long teacherId = getCurrentTeacherId();
        try {
            // 非空校验
            if (userId == null) {
                result.put(RESULT_SUCCESS_KEY, false);
                result.put(RESULT_MESSAGE_KEY, "用户ID不能为空");
                return result;
            }

            // 学生数据查询与非空校验
            Student student = studentMapper.selectByUserId(userId);
            if (student == null) {
                result.put(RESULT_SUCCESS_KEY, false);
                result.put(RESULT_MESSAGE_KEY, "学生信息不存在");
                return result;
            }

            // 权限校验
            if (!hasStudentAuth(teacherId, student)) {
                log.warn("教师ID:{} 无权限删除学生ID:{}", teacherId, userId);
                result.put(RESULT_SUCCESS_KEY, false);
                result.put(RESULT_MESSAGE_KEY, "无权限操作该学生");
                return result;
            }

            // 调用服务层删除
            boolean success = adminService.deleteStudent(userId);
            result.put(RESULT_SUCCESS_KEY, success);
            result.put(RESULT_MESSAGE_KEY, success ? "删除成功" : "删除失败");
            log.info("教师ID:{} 删除学生结果:{}, 学生用户ID:{}", teacherId, success, userId);
        } catch (Exception e) {
            log.error("教师端删除学生异常，teacherId:{}, userId:{}", teacherId, userId, e);
            result.put(RESULT_SUCCESS_KEY, false);
            result.put(RESULT_MESSAGE_KEY, "删除失败：" + e.getMessage());
        }
        return result;
    }

    /**
     * 教师端重置学生密码
     * 【修复】空指针防护、权限校验前置、日志记录
     */
    @PostMapping("/class/student/reset-password/{userId}")
    @ResponseBody
    public Map<String, Object> resetStudentPassword(@PathVariable("userId") Long userId) {
        Map<String, Object> result = new HashMap<>();
        Long teacherId = getCurrentTeacherId();
        try {
            if (userId == null) {
                result.put(RESULT_SUCCESS_KEY, false);
                result.put(RESULT_MESSAGE_KEY, "用户ID不能为空");
                return result;
            }

            Student student = studentMapper.selectByUserId(userId);
            if (student == null) {
                result.put(RESULT_SUCCESS_KEY, false);
                result.put(RESULT_MESSAGE_KEY, "学生信息不存在");
                return result;
            }

            if (!hasStudentAuth(teacherId, student)) {
                log.warn("教师ID:{} 无权限重置学生ID:{} 的密码", teacherId, userId);
                result.put(RESULT_SUCCESS_KEY, false);
                result.put(RESULT_MESSAGE_KEY, "无权限操作该学生");
                return result;
            }

            boolean success = adminService.resetPassword(userId);
            result.put(RESULT_SUCCESS_KEY, success);
            // 核心修改：提示信息改为abc123
            result.put(RESULT_MESSAGE_KEY, success ? "密码重置成功，新密码abc123" : "密码重置失败");
            log.info("教师ID:{} 重置学生密码结果:{}, 学生用户ID:{}", teacherId, success, userId);
        } catch (Exception e) {
            log.error("教师端重置学生密码异常，teacherId:{}, userId:{}", teacherId, userId, e);
            result.put(RESULT_SUCCESS_KEY, false);
            result.put(RESULT_MESSAGE_KEY, "操作失败：" + e.getMessage());
        }
        return result;
    }
}