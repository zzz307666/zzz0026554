package com.hhjt.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hhjt.dto.StudentDTO;
import com.hhjt.dto.TeacherDTO;
import com.hhjt.dto.UserQueryDTO;
import com.hhjt.entity.Student;
import com.hhjt.entity.Teacher;
import com.hhjt.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface UserMapper extends BaseMapper<User> {

    // 分页查询用户（修改：按创建时间升序，从早到晚）
    @Select("SELECT u.* FROM sys_user u LEFT JOIN sys_role r ON u.role_id = r.id " +
            "WHERE 1=1 " +
            "AND (#{query.username} IS NULL OR u.username LIKE CONCAT('%', #{query.username}, '%')) " +
            "AND (#{query.realName} IS NULL OR u.real_name LIKE CONCAT('%', #{query.realName}, '%')) " +
            "AND (#{query.roleId} IS NULL OR u.role_id = #{query.roleId}) " +
            "AND (#{query.status} IS NULL OR u.status = #{query.status}) " +
            "ORDER BY u.create_time ASC")
    IPage<User> selectUserPage(Page<User> page, @Param("query") UserQueryDTO query);

    // 按用户名查询用户
    @Select("SELECT * FROM sys_user WHERE username = #{username}")
    User selectByUsername(@Param("username") String username);

    // 【核心修改：教师列表新增年级/班级查询条件】
    @Select("SELECT u.id as userId, u.username, u.real_name as realName, u.phone, u.email, u.status, u.create_time, " +
            "t.id, t.teacher_no as teacherNo, t.subject, t.gender " +
            "FROM sys_user u " +
            "INNER JOIN sys_teacher t ON u.id = t.user_id " +
            "LEFT JOIN sys_teacher_class tc ON t.id = tc.teacher_id " +
            "LEFT JOIN sys_class c ON tc.class_id = c.id " +
            "WHERE u.role_id = 2 AND u.status = 1 " +
            "AND (#{realName} IS NULL OR u.real_name LIKE CONCAT('%', #{realName}, '%')) " +
            "AND (#{teacherNo} IS NULL OR t.teacher_no LIKE CONCAT('%', #{teacherNo}, '%')) " +
            "AND (#{grade} IS NULL OR c.grade = #{grade}) " +
            "AND (#{className} IS NULL OR c.class_name LIKE CONCAT('%', #{className}, '%')) " +
            "GROUP BY u.id, t.id " +
            "ORDER BY u.create_time ASC")
    List<TeacherDTO> selectTeacherList(@Param("realName") String realName,
                                       @Param("teacherNo") String teacherNo,
                                       @Param("grade") String grade,
                                       @Param("className") String className);

    // 【核心修改：学生列表新增年级查询条件】
    @Select("SELECT u.*, s.*, c.class_name, c.grade FROM sys_user u " +
            "INNER JOIN sys_student s ON u.id = s.user_id " +
            "LEFT JOIN sys_class c ON s.class_id = c.id " +
            "WHERE u.role_id = 3 AND u.status = 1 " +
            "AND (#{realName} IS NULL OR u.real_name LIKE CONCAT('%', #{realName}, '%')) " +
            "AND (#{studentNo} IS NULL OR s.student_no LIKE CONCAT('%', #{studentNo}, '%')) " +
            "AND (#{className} IS NULL OR c.class_name LIKE CONCAT('%', #{className}, '%')) " +
            "AND (#{grade} IS NULL OR c.grade = #{grade}) " +
            "ORDER BY u.create_time ASC")
    List<StudentDTO> selectStudentList(@Param("realName") String realName,
                                       @Param("studentNo") String studentNo,
                                       @Param("className") String className,
                                       @Param("grade") String grade);

    // 查询所有教师列表（包括禁用的，修改：按创建时间升序，从早到晚）
    @Select("SELECT u.id as userId, u.username, u.real_name as realName, u.phone, u.email, u.status, u.create_time, " +
            "t.id, t.teacher_no as teacherNo, t.subject, t.gender " +
            "FROM sys_user u " +
            "INNER JOIN sys_teacher t ON u.id = t.user_id " +
            "WHERE u.role_id = 2 " +
            "AND (#{realName} IS NULL OR u.real_name LIKE CONCAT('%', #{realName}, '%')) " +
            "AND (#{teacherNo} IS NULL OR t.teacher_no LIKE CONCAT('%', #{teacherNo}, '%')) " +
            "ORDER BY u.create_time ASC")
    List<TeacherDTO> selectAllTeacherList(@Param("realName") String realName,
                                          @Param("teacherNo") String teacherNo);

    // 查询所有学生列表（包括禁用的，修改：按创建时间升序，从早到晚）
    @Select("SELECT u.*, s.*, c.class_name, c.grade FROM sys_user u " +
            "INNER JOIN sys_student s ON u.id = s.user_id " +
            "LEFT JOIN sys_class c ON s.class_id = c.id " +
            "WHERE u.role_id = 3 " +
            "AND (#{realName} IS NULL OR u.real_name LIKE CONCAT('%', #{realName}, '%')) " +
            "AND (#{studentNo} IS NULL OR s.student_no LIKE CONCAT('%', #{studentNo}, '%')) " +
            "AND (#{className} IS NULL OR c.class_name LIKE CONCAT('%', #{className}, '%')) " +
            "ORDER BY u.create_time ASC")
    List<StudentDTO> selectAllStudentList(@Param("realName") String realName,
                                          @Param("studentNo") String studentNo,
                                          @Param("className") String className);

    // 查询管理员（判断是否已存在）
    @Select("SELECT * FROM sys_user WHERE role_id = 1 LIMIT 1")
    User selectAdmin();

    // 新增：根据用户ID查询教师信息
    @Select("SELECT * FROM sys_teacher WHERE user_id = #{userId}")
    Teacher selectTeacherByUserId(@Param("userId") Long userId);

    // 新增：根据用户ID查询学生信息
    @Select("SELECT * FROM sys_student WHERE user_id = #{userId}")
    Student selectStudentByUserId(@Param("userId") Long userId);
}