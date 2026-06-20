// UserQueryDTO.java
package com.hhjt.dto;
import lombok.Data;

@Data
public class UserQueryDTO {
    private String username;
    private String realName;
    private Integer roleId;
    private String phone;
    private Integer status;
}