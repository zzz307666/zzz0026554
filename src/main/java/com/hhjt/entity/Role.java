package com.hhjt.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("sys_role")
public class Role {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String roleCode; // 角色编码 ADMIN/TEACHER/STUDENT
    private String roleName; // 角色名称
    private String roleDesc; // 角色描述
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}