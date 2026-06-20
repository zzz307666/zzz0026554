package com.hhjt.dto;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class StudentDTO {
    private Long id;
    private Long userId;
    private String username;
    private String realName;
    private String phone;
    private String email;
    private String studentNo;
    private Long classId; // 班级ID
    private String className; // 班级名称
    private String grade; // 年级
    private Integer gender;
    private LocalDate birthDate;
    private Integer status;
    private LocalDateTime createTime;
}