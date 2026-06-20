package com.hhjt.service;

import java.util.List;
import java.util.Map;

/**
 * 权限管理服务接口
 */
public interface PermissionService {
    
    /**
     * 获取所有角色列表
     * @return 角色列表
     */
    List<Map<String, Object>> getAllRoles();
    
    /**
     * 获取角色的菜单权限
     * @param roleId 角色ID
     * @return 菜单权限列表
     */
    List<Map<String, Object>> getRoleMenus(Long roleId);
    
    /**
     * 保存角色菜单权限
     * @param roleId 角色ID
     * @param menuIds 菜单ID列表
     * @return 是否成功
     */
    boolean saveRoleMenus(Long roleId, List<Long> menuIds);
    
    /**
     * 获取用户的数据权限
     * @param userId 用户ID
     * @return 数据权限配置
     */
    Map<String, Object> getUserDataPermission(Long userId);
    
    /**
     * 保存用户数据权限
     * @param userId 用户ID
     * @param permissionType 权限类型（all/self/class）
     * @param classIds 班级ID列表
     * @return 是否成功
     */
    boolean saveUserDataPermission(Long userId, String permissionType, List<Long> classIds);
    
    /**
     * 获取所有菜单列表
     * @return 菜单树形结构
     */
    List<Map<String, Object>> getAllMenus();
}
