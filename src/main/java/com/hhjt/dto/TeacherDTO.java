package com.hhjt.dto;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class TeacherDTO {
    private Long id;
    private Long userId;
    private String username;
    private String realName;
    private String phone;
    private String email;
    private String teacherNo;
    private String subject;
    private Integer gender;
    private Integer status;
    private LocalDateTime createTime;
    // 任职班级列表
    private List<ClassDTO> classList;
    // 班级名称拼接（用于列表展示）
    private String classNameStr;
}