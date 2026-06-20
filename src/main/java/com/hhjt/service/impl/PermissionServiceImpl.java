package com.hhjt.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.hhjt.entity.Permission;
import com.hhjt.entity.Role;
import com.hhjt.entity.RolePermission;
import com.hhjt.entity.Teacher;
import com.hhjt.entity.User;
import com.hhjt.mapper.PermissionMapper;
import com.hhjt.mapper.RoleMapper;
import com.hhjt.mapper.RolePermissionMapper;
import com.hhjt.mapper.TeacherClassMapper;
import com.hhjt.mapper.TeacherMapper;
import com.hhjt.mapper.UserMapper;
import com.hhjt.service.PermissionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 权限管理服务实现类 - RBAC模型（真实数据版本）
 */
@Slf4j
@Service
public class PermissionServiceImpl implements PermissionService {

    @Autowired
    private PermissionMapper permissionMapper;

    @Autowired
    private RolePermissionMapper rolePermissionMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private RoleMapper roleMapper;

    @Autowired
    private TeacherMapper teacherMapper;

    @Autowired
    private TeacherClassMapper teacherClassMapper;

    @Override
    public List<Map<String, Object>> getAllRoles() {
        List<Map<String, Object>> result = new ArrayList<>();
        
        try {
            // 从sys_role表查询真实角色数据
            List<Role> roles = roleMapper.selectList(null);
            
            for (Role role : roles) {
                Map<String, Object> roleData = new HashMap<>();
                roleData.put("roleId", role.getId());
                roleData.put("roleName", role.getRoleName());
                roleData.put("roleCode", role.getRoleCode());
                roleData.put("description", role.getRoleDesc());
                
                // 统计每个角色的用户数
                long userCount = userMapper.selectCount(
                    new QueryWrapper<User>().eq("role_id", role.getId())
                );
                roleData.put("userCount", userCount);
                
                result.add(roleData);
            }
            
        } catch (Exception e) {
            log.error("获取角色列表失败", e);
        }
        
        return result;
    }

    @Override
    public List<Map<String, Object>> getRoleMenus(Long roleId) {
        List<Map<String, Object>> result = new ArrayList<>();
        
        try {
            // 查询该角色的所有权限
            List<Permission> permissions = permissionMapper.selectByRoleId(roleId);
            
            for (Permission perm : permissions) {
                if ("MENU".equals(perm.getPermissionType())) {
                    Map<String, Object> menu = new HashMap<>();
                    menu.put("menuId", perm.getId());
                    menu.put("menuName", perm.getPermissionName());
                    menu.put("url", perm.getPath());
                    menu.put("checked", true);
                    result.add(menu);
                }
            }
        } catch (Exception e) {
            log.error("获取角色菜单失败", e);
        }
        
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean saveRoleMenus(Long roleId, List<Long> menuIds) {
        try {
            // 删除该角色的所有权限
            rolePermissionMapper.deleteByRoleId(roleId);
            
            // 添加新的权限关联
            if (menuIds != null && !menuIds.isEmpty()) {
                for (Long permissionId : menuIds) {
                    RolePermission rp = new RolePermission();
                    rp.setRoleId(roleId);
                    rp.setPermissionId(permissionId);
                    rolePermissionMapper.insert(rp);
                }
            }
            
            log.info("保存角色菜单权限成功：roleId={}, menuCount={}", roleId, menuIds != null ? menuIds.size() : 0);
            return true;
        } catch (Exception e) {
            log.error("保存角色菜单权限失败", e);
            return false;
        }
    }

    @Override
    public Map<String, Object> getUserDataPermission(Long userId) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            User user = userMapper.selectById(userId);
            if (user == null) {
                return result;
            }
            
            result.put("userId", userId);
            result.put("roleId", user.getRoleId());
            
            // 根据角色设置数据权限
            Long roleId = user.getRoleId();
            if (roleId != null && roleId == 1) {
                // 管理员：查看所有数据
                result.put("permissionType", "all");
                result.put("classIds", new ArrayList<>());
            } else if (roleId != null && roleId == 2) {
                // 教师：查看所教班级数据
                result.put("permissionType", "class");
                // 从teacher_class表查询教师所教班级
                List<Long> classIds = teacherClassMapper.selectClassIdByTeacherId(userId);
                result.put("classIds", classIds != null ? classIds : new ArrayList<>());
            } else {
                // 学生：只查看自己的数据
                result.put("permissionType", "self");
                result.put("classIds", new ArrayList<>());
            }
            
        } catch (Exception e) {
            log.error("获取用户数据权限失败", e);
        }
        
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean saveUserDataPermission(Long userId, String permissionType, List<Long> classIds) {
        try {
            // 更新用户的数据权限配置
            // 这里可以创建一个单独的用户权限配置表来存储
            // 目前只是记录到日志
            log.info("保存用户数据权限：userId={}, type={}, classIds={}", userId, permissionType, classIds);
            
            // 如果是教师角色，同步更新teacher_class表
            if ("class".equals(permissionType) && classIds != null) {
                User user = userMapper.selectById(userId);
                if (user != null && user.getRoleId() == 2) {
                    // 先删除旧的关联
                    teacherClassMapper.deleteByTeacherId(userId);
                    
                    // 添加新的关联
                    Teacher teacher = teacherMapper.selectByUserId(userId);
                    if (teacher != null) {
                        for (Long classId : classIds) {
                            com.hhjt.entity.TeacherClass tc = new com.hhjt.entity.TeacherClass();
                            tc.setTeacherId(teacher.getId());
                            tc.setClassId(classId);
                            teacherClassMapper.insert(tc);
                        }
                    }
                }
            }
            
            return true;
        } catch (Exception e) {
            log.error("保存用户数据权限失败", e);
            return false;
        }
    }

    @Override
    public List<Map<String, Object>> getAllMenus() {
        List<Map<String, Object>> result = new ArrayList<>();
        
        try {
            // 查询所有启用的菜单权限
            List<Permission> allMenus = permissionMapper.selectAllMenus();
            
            // 构建树形结构
            Map<Long, Map<String, Object>> menuMap = new LinkedHashMap<>();
            
            // 第一遍：创建所有节点
            for (Permission perm : allMenus) {
                Map<String, Object> menuNode = new HashMap<>();
                menuNode.put("menuId", perm.getId());
                menuNode.put("menuName", perm.getPermissionName());
                menuNode.put("parentId", perm.getParentId());
                menuNode.put("url", perm.getPath());
                menuNode.put("icon", perm.getIcon());
                menuNode.put("sortOrder", perm.getSortOrder());
                menuNode.put("checked", false);
                menuNode.put("children", new ArrayList<Map<String, Object>>());
                
                menuMap.put(perm.getId(), menuNode);
            }
            
            // 第二遍：构建父子关系
            for (Map<String, Object> node : menuMap.values()) {
                Long parentId = (Long) node.get("parentId");
                if (parentId != null && parentId > 0 && menuMap.containsKey(parentId)) {
                    @SuppressWarnings("unchecked")
                    List<Map<String, Object>> children = 
                        (List<Map<String, Object>>) menuMap.get(parentId).get("children");
                    children.add(node);
                } else {
                    // 根节点
                    result.add(node);
                }
            }
            
            // 按sortOrder排序
            result.sort((a, b) -> Integer.compare(
                (Integer) a.getOrDefault("sortOrder", 0),
                (Integer) b.getOrDefault("sortOrder", 0)
            ));
            
        } catch (Exception e) {
            log.error("获取所有菜单失败", e);
        }
        
        return result;
    }
}
