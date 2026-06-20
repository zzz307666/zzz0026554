package com.hhjt.service.impl;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hhjt.dto.ClassDTO;
import com.hhjt.dto.StudentDTO;
import com.hhjt.dto.TeacherDTO;
import com.hhjt.dto.UserQueryDTO;
import com.hhjt.entity.*;
import com.hhjt.mapper.*;
import com.hhjt.service.AdminService;
import com.hhjt.service.ClassService;
import com.hhjt.service.SequenceService;
import com.hhjt.service.TeacherClassService;
import com.hhjt.utils.PasswordCheckUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
public class AdminServiceImpl implements AdminService {

    // 核心修改：默认密码改为abc123（已满足6位长度，无需修改）
    private static final String DEFAULT_PASSWORD = "abc123";

    @Autowired
    private UserMapper userMapper;
    @Autowired
    private TeacherMapper teacherMapper;
    @Autowired
    private StudentMapper studentMapper;
    @Autowired
    private AdminMapper adminMapper;
    @Autowired
    private RoleMapper roleMapper;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private SequenceService sequenceService;
    @Autowired
    private TeacherClassService teacherClassService;
    @Autowired
    private ClassService classService;
    @Autowired
    private ClassMapper classMapper;

    // ==================== 核心修改：通用密码加密方法（添加长度校验） ====================
    /**
     * 通用密码加密方法（含长度校验）
     * @param rawPassword 明文密码
     * @return 加密后的密文密码
     * @throws RuntimeException 密码不满足长度要求时抛出
     */
    private String encodePassword(String rawPassword) {
        // 1. 密码长度校验（空密码由服务层兜底默认值，已满足长度）
        if (!PasswordCheckUtil.checkPasswordLength(rawPassword)) {
            throw new RuntimeException(PasswordCheckUtil.PASSWORD_TOO_SHORT_MSG);
        }
        // 2. 空密码兜底为默认密码（abc123），非空则使用传入密码
        String finalPassword = Optional.ofNullable(rawPassword)
                .filter(StringUtils::hasText)
                .orElse(DEFAULT_PASSWORD);
        // 3. BCrypt加密
        return passwordEncoder.encode(finalPassword);
    }

    // ==================== 以下为原有方法（无修改，仅保证代码完整性） ====================
    @Override
    public IPage<User> getUserPage(Integer pageNum, Integer pageSize, UserQueryDTO query) {
        Page<User> page = new Page<>(pageNum, pageSize);
        return userMapper.selectUserPage(page, query);
    }

    @Override
    public boolean addUser(User user) {
        return addUserWithRelate(user, null, null, null);
    }

    @Override
    public boolean updateUser(User user) {
        return updateUserWithRelate(user, null, null, null);
    }

    // 【核心修改：教师列表查询+年级/班级显示优化】
    @Override
    public List<TeacherDTO> getTeacherList(String realName, String teacherNo, String grade, String className) {
        // 调用Mapper新增年级/班级查询条件
        List<TeacherDTO> list = userMapper.selectTeacherList(realName, teacherNo, grade, className);
        for (TeacherDTO dto : list) {
            List<SysClass> classList = classService.getClassByTeacherId(dto.getId());
            classList.sort((c1, c2) -> c1.getCreateTime().compareTo(c2.getCreateTime()));
            List<ClassDTO> classDTOList = classList.stream().map(c -> {
                ClassDTO classDTO = new ClassDTO();
                BeanUtils.copyProperties(c, classDTO);
                return classDTO;
            }).collect(Collectors.toList());
            dto.setClassList(classDTOList);
            // 核心修改：任职班级改为 年级/班级 格式显示
            dto.setClassNameStr(classList.stream().map(c -> c.getGrade() + "/" + c.getClassName()).collect(Collectors.joining("、")));
        }
        return list;
    }

    @Override
    public List<StudentDTO> getStudentList(String realName, String studentNo, String className, String grade) {
        // 调用Mapper新增年级查询条件
        return userMapper.selectStudentList(realName, studentNo, className, grade);
    }

    @Override
    public List<TeacherDTO> getAllTeacherList(String realName, String teacherNo) {
        List<TeacherDTO> list = userMapper.selectAllTeacherList(realName, teacherNo);
        for (TeacherDTO dto : list) {
            List<SysClass> classList = classService.getClassByTeacherId(dto.getId());
            classList.sort((c1, c2) -> c1.getCreateTime().compareTo(c2.getCreateTime()));
            dto.setClassNameStr(classList.stream().map(SysClass::getClassName).collect(Collectors.joining("、")));
        }
        return list;
    }

    @Override
    public List<StudentDTO> getAllStudentList(String realName, String studentNo, String className) {
        return userMapper.selectAllStudentList(realName, studentNo, className);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean addTeacher(User user, Teacher teacher, List<Long> classIdList) {
        if (classIdList == null || classIdList.isEmpty()) {
            throw new RuntimeException("请至少选择一个任职班级");
        }
        user.setRoleId(2L);
        teacher.setTeacherNo(sequenceService.getTeacherNo());
        boolean result = addUserWithRelateEntity(user, teacher, teacherMapper);
        if (result) {
            teacherClassService.saveTeacherClass(teacher.getId(), classIdList);
        }
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean addStudent(User user, Student student) {
        if (student.getClassId() == null) {
            throw new RuntimeException("请选择学生所属班级");
        }
        user.setRoleId(3L);
        student.setStudentNo(sequenceService.getStudentNo());
        return addUserWithRelateEntity(user, student, studentMapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateTeacher(User user, Teacher teacher, List<Long> classIdList) {
        if (classIdList == null || classIdList.isEmpty()) {
            throw new RuntimeException("请至少选择一个任职班级");
        }
        if (!updateUser(user)) return false;
        teacher.setUpdateTime(LocalDateTime.now());
        teacherMapper.updateById(teacher);
        return teacherClassService.saveTeacherClass(teacher.getId(), classIdList);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateStudent(User user, Student student) {
        if (student.getClassId() == null) {
            throw new RuntimeException("请选择学生所属班级");
        }
        if (!updateUser(user)) return false;
        student.setUpdateTime(LocalDateTime.now());
        return studentMapper.updateById(student) > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteUser(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            log.error("删除失败：用户ID{}不存在", userId);
            return false;
        }
        if (user.isAdmin()) {
            throw new RuntimeException("管理员账户不可删除");
        }
        if (user.getRoleId() == 2L) {
            Teacher teacher = teacherMapper.selectByUserId(userId);
            if (teacher != null) {
                teacherClassService.deleteByTeacherId(teacher.getId());
            }
        }
        int deleteCount = userMapper.deleteById(userId);
        log.info("删除用户ID{}成功，数据库自动级联删除关联数据", userId);
        return deleteCount > 0;
    }

    @Override
    public boolean updateUserStatus(Long userId, Integer status) {
        User user = userMapper.selectById(userId);
        if (user == null) return false;
        if (user.isAdmin()) {
            throw new RuntimeException("管理员账户不可禁用");
        }
        user.setStatus(status);
        user.setUpdateTime(LocalDateTime.now());
        return userMapper.updateById(user) > 0;
    }

    @Override
    public boolean resetPassword(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) return false;
        user.setPassword(encodePassword(DEFAULT_PASSWORD)); // 复用加密方法（含校验）
        user.setUpdateTime(LocalDateTime.now());
        return userMapper.updateById(user) > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean addUserWithRelate(User user, Teacher teacher, Student student, List<Long> classIdList) {
        try {
            // 调用加密方法（自动触发长度校验）
            user.setPassword(encodePassword(user.getPassword()));
            user.setStatus(1);
            user.setCreateTime(LocalDateTime.now());
            user.setUpdateTime(LocalDateTime.now());
            user.setAvatar("/image/default-avatar.png");
            user.setSignature("这个人很懒，什么都没写~");

            if (user.isAdmin()) {
                User existAdmin = userMapper.selectAdmin();
                if (existAdmin != null) {
                    throw new RuntimeException("系统已存在管理员，不可重复添加");
                }
                userMapper.insert(user);
                Admin admin = new Admin();
                admin.setUserId(user.getId());
                admin.setCreateTime(LocalDateTime.now());
                admin.setUpdateTime(LocalDateTime.now());
                adminMapper.insert(admin);
                return true;
            }

            if (user.getRoleId() == 2L && teacher != null) {
                if (classIdList == null || classIdList.isEmpty()) {
                    throw new RuntimeException("请至少选择一个任职班级");
                }
                userMapper.insert(user);
                teacher.setUserId(user.getId());
                teacher.setTeacherNo(sequenceService.getTeacherNo());
                teacher.setCreateTime(LocalDateTime.now());
                teacher.setUpdateTime(LocalDateTime.now());
                teacherMapper.insert(teacher);
                teacherClassService.saveTeacherClass(teacher.getId(), classIdList);
                return true;
            }

            if (user.getRoleId() == 3L && student != null) {
                if (student.getClassId() == null) {
                    throw new RuntimeException("请选择学生所属班级");
                }
                userMapper.insert(user);
                student.setUserId(user.getId());
                student.setStudentNo(sequenceService.getStudentNo());
                student.setCreateTime(LocalDateTime.now());
                student.setUpdateTime(LocalDateTime.now());
                studentMapper.insert(student);
                return true;
            }

            throw new RuntimeException("角色异常，仅支持管理员/教师/学生");
        } catch (Exception e) {
            log.error("新增用户失败", e);
            throw new RuntimeException("新增用户失败：" + e.getMessage());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateUserWithRelate(User user, Teacher teacher, Student student, List<Long> classIdList) {
        try {
            User oldUser = userMapper.selectById(user.getId());
            if (oldUser == null) {
                throw new RuntimeException("用户不存在");
            }
            if (oldUser.isAdmin()) {
                user.setRoleId(1L);
                user.setStatus(1);
            }
            // 密码为空则沿用旧密码，否则加密更新（触发长度校验）
            if (user.getPassword() == null || user.getPassword().isEmpty()) {
                user.setPassword(oldUser.getPassword());
            } else {
                user.setPassword(encodePassword(user.getPassword()));
            }
            user.setUpdateTime(LocalDateTime.now());
            userMapper.updateById(user);

            if (user.getRoleId() == 2L && teacher != null) {
                if (classIdList == null || classIdList.isEmpty()) {
                    throw new RuntimeException("请至少选择一个任职班级");
                }
                teacher.setUserId(user.getId());
                teacher.setUpdateTime(LocalDateTime.now());
                teacherMapper.updateById(teacher);
                teacherClassService.saveTeacherClass(teacher.getId(), classIdList);
            }

            if (user.getRoleId() == 3L && student != null) {
                if (student.getClassId() == null) {
                    throw new RuntimeException("请选择学生所属班级");
                }
                student.setUserId(user.getId());
                student.setUpdateTime(LocalDateTime.now());
                studentMapper.updateById(student);
            }

            return true;
        } catch (Exception e) {
            log.error("编辑用户失败", e);
            throw new RuntimeException("编辑用户失败：" + e.getMessage());
        }
    }

    private <T> boolean addUserWithRelateEntity(User user, T relateEntity, BaseMapper<T> mapper) {
        // 调用加密方法（自动触发长度校验）
        user.setPassword(encodePassword(user.getPassword()));
        int userResult = userMapper.insert(user);
        if (userResult <= 0) return false;

        if (relateEntity instanceof Teacher) {
            ((Teacher) relateEntity).setUserId(user.getId());
            ((Teacher) relateEntity).setCreateTime(LocalDateTime.now());
            ((Teacher) relateEntity).setUpdateTime(LocalDateTime.now());
        } else if (relateEntity instanceof Student) {
            ((Student) relateEntity).setUserId(user.getId());
            ((Student) relateEntity).setCreateTime(LocalDateTime.now());
            ((Student) relateEntity).setUpdateTime(LocalDateTime.now());
        }

        return mapper.insert(relateEntity) > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteTeacher(Long userId) {
        return deleteUser(userId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteStudent(Long userId) {
        return deleteUser(userId);
    }
}