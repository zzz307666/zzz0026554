package com.hhjt.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.hhjt.dto.StudentDTO;
import com.hhjt.dto.TeacherDTO;
import com.hhjt.dto.UserQueryDTO;
import com.hhjt.entity.Student;
import com.hhjt.entity.Teacher;
import com.hhjt.entity.User;

import java.util.List;

public interface AdminService {

    // 分页查询用户
    IPage<User> getUserPage(Integer pageNum, Integer pageSize, UserQueryDTO query);

    // 新增普通用户（支持管理员/教师/学生角色）
    boolean addUser(User user);

    // 编辑用户信息
    boolean updateUser(User user);

    // 获取启用的教师列表【核心修改：新增年级/班级参数】
    List<TeacherDTO> getTeacherList(String realName, String teacherNo, String grade, String className);

    // 获取启用的学生列表【核心修改：新增年级参数】
    List<StudentDTO> getStudentList(String realName, String studentNo, String className, String grade);

    // 获取所有教师列表（包括禁用的）
    List<TeacherDTO> getAllTeacherList(String realName, String teacherNo);

    // 获取所有学生列表（包括禁用的）
    List<StudentDTO> getAllStudentList(String realName, String studentNo, String className);

    // 新增教师（关联用户表+班级关联）
    boolean addTeacher(User user, Teacher teacher, List<Long> classIdList);

    // 新增学生（关联用户表+班级关联）
    boolean addStudent(User user, Student student);

    // 更新教师信息+班级关联
    boolean updateTeacher(User user, Teacher teacher, List<Long> classIdList);

    // 更新学生信息+班级关联
    boolean updateStudent(User user, Student student);

    // 物理删除用户（级联删除关联数据）
    boolean deleteUser(Long userId);

    // 启用/禁用用户
    boolean updateUserStatus(Long userId, Integer status);

    // 重置密码
    boolean resetPassword(Long userId);

    // 添加用户时联动创建教师/学生关联记录+班级
    boolean addUserWithRelate(User user, Teacher teacher, Student student, List<Long> classIdList);

    // 编辑用户时联动更新教师/学生关联记录+班级
    boolean updateUserWithRelate(User user, Teacher teacher, Student student, List<Long> classIdList);

    // 删除教师（级联删除用户+班级关联）
    boolean deleteTeacher(Long userId);

    // 删除学生（级联删除用户）
    boolean deleteStudent(Long userId);
}