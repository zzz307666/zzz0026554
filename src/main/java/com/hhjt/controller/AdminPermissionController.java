package com.hhjt.controller;

import com.hhjt.service.PermissionService;
import com.hhjt.service.UserSessionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 管理员 - 权限管理控制器
 */
@Slf4j
@Controller
@RequestMapping("/admin/permission")
public class AdminPermissionController {

    @Autowired
    private PermissionService permissionService;

    @Autowired(required = false)
    private UserSessionService userSessionService;

    /**
     * 权限管理页面
     */
    @GetMapping("/management")
    public String permissionManagement(Model model) {
        return "admin/permission_management";
    }

    /**
     * 获取所有角色
     */
    @GetMapping("/roles")
    @ResponseBody
    public Map<String, Object> getAllRoles() {
        Map<String, Object> result = new HashMap<>();
        try {
            result.put("success", true);
            result.put("data", permissionService.getAllRoles());
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", e.getMessage());
            log.error("获取角色列表失败", e);
        }
        return result;
    }

    /**
     * 获取角色的菜单权限
     */
    @GetMapping("/role/{roleId}/menus")
    @ResponseBody
    public Map<String, Object> getRoleMenus(@PathVariable Long roleId) {
        Map<String, Object> result = new HashMap<>();
        try {
            result.put("success", true);
            result.put("data", permissionService.getRoleMenus(roleId));
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", e.getMessage());
            log.error("获取角色菜单失败", e);
        }
        return result;
    }

    /**
     * 保存角色菜单权限 - 修改后立即生效
     */
    @PostMapping("/role/{roleId}/menus")
    @ResponseBody
    public Map<String, Object> saveRoleMenus(@PathVariable Long roleId,
                                              @RequestBody List<Long> menuIds) {
        Map<String, Object> result = new HashMap<>();
        try {
            boolean success = permissionService.saveRoleMenus(roleId, menuIds);
            if (success) {
                // 使该角色所有在线用户的Session失效，强制重新登录获取新权限
                if (userSessionService != null) {
                    userSessionService.invalidateRoleSessions(roleId);
                    log.info("Invalidated sessions for role: {}", roleId);
                }
            }
            result.put("success", success);
            result.put("message", success ? "保存成功，相关用户需重新登录" : "保存失败");
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", e.getMessage());
            log.error("保存角色菜单失败", e);
        }
        return result;
    }

    /**
     * 获取用户数据权限
     */
    @GetMapping("/user/{userId}/data-permission")
    @ResponseBody
    public Map<String, Object> getUserDataPermission(@PathVariable Long userId) {
        Map<String, Object> result = new HashMap<>();
        try {
            result.put("success", true);
            result.put("data", permissionService.getUserDataPermission(userId));
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", e.getMessage());
            log.error("获取用户数据权限失败", e);
        }
        return result;
    }

    /**
     * 保存用户数据权限 - 修改后立即生效
     */
    @PostMapping("/user/{userId}/data-permission")
    @ResponseBody
    public Map<String, Object> saveUserDataPermission(@PathVariable Long userId,
                                                       @RequestParam String permissionType,
                                                       @RequestParam(required = false) List<Long> classIds) {
        Map<String, Object> result = new HashMap<>();
        try {
            boolean success = permissionService.saveUserDataPermission(userId, permissionType, classIds);
            if (success) {
                // 使该用户的Session失效，强制重新登录获取新权限
                if (userSessionService != null) {
                    userSessionService.invalidateUserSession(userId);
                    log.info("Invalidated session for user: {}", userId);
                }
            }
            result.put("success", success);
            result.put("message", success ? "保存成功，用户需重新登录" : "保存失败");
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", e.getMessage());
            log.error("保存用户数据权限失败", e);
        }
        return result;
    }

    /**
     * 获取所有菜单（树形结构）
     */
    @GetMapping("/menus")
    @ResponseBody
    public Map<String, Object> getAllMenus() {
        Map<String, Object> result = new HashMap<>();
        try {
            result.put("success", true);
            result.put("data", permissionService.getAllMenus());
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", e.getMessage());
            log.error("获取菜单列表失败", e);
        }
        return result;
    }
}