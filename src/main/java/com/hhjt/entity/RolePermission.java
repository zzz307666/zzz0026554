package com.hhjt.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 角色权限关联实体类
 */
@Data
@TableName("sys_role_permission")
public class RolePermission {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private Long roleId;
    
    private Long permissionId;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
