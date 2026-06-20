package com.hhjt.config;

import com.hhjt.entity.Permission;
import com.hhjt.entity.RolePermission;
import com.hhjt.mapper.PermissionMapper;
import com.hhjt.mapper.RolePermissionMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Component
@Slf4j
public class DataInitConfig implements CommandLineRunner {

    @Autowired
    private PermissionMapper permissionMapper;

    @Autowired
    private RolePermissionMapper rolePermissionMapper;

    @Override
    public void run(String... args) throws Exception {
        initPermissions();
    }

    private void initPermissions() {
        if (permissionMapper.selectCount(null) > 0) {
            log.info("权限数据已存在，跳过初始化");
            return;
        }

        log.info("开始初始化权限数据...");

        createPermission("系统管理", "system:manage", "MENU", 0L, "/admin/system", "setting", 1, 1, "系统管理模块");
        createPermission("用户管理", "system:user", "MENU", 1L, "/admin/users", "user", 1, 1, "用户管理");
        createPermission("角色管理", "system:role", "MENU", 1L, "/admin/roles", "team", 2, 1, "角色管理");
        createPermission("权限管理", "system:permission", "MENU", 1L, "/admin/permissions", "lock", 3, 1, "权限管理");
        createPermission("班级管理", "system:class", "MENU", 1L, "/admin/classes", "bank", 4, 1, "班级管理");
        createPermission("教师管理", "system:teacher", "MENU", 1L, "/admin/teachers", "solution", 5, 1, "教师管理");
        createPermission("运动管理", "sport:manage", "MENU", 0L, "/admin/sport", "fire", 2, 1, "运动管理模块");
        createPermission("运动审核", "sport:audit", "MENU", 7L, "/admin/sport/audit", "check", 1, 1, "运动记录审核");
        createPermission("运动类型", "sport:type", "MENU", 7L, "/admin/sport/types", "tags", 2, 1, "运动类型管理");
        createPermission("运动统计", "sport:stats", "MENU", 7L, "/admin/sport/stats", "bar-chart", 3, 1, "运动统计分析");
        createPermission("评价管理", "evaluation:manage", "MENU", 0L, "/admin/evaluation", "form", 3, 1, "评价管理模块");
        createPermission("评价维度", "evaluation:dimension", "MENU", 11L, "/admin/evaluation/dimensions", "bars", 1, 1, "评价维度配置");
        createPermission("评价记录", "evaluation:record", "MENU", 11L, "/admin/evaluation/records", "file-text", 2, 1, "评价记录查询");
        createPermission("积分规则", "evaluation:rule", "MENU", 11L, "/admin/evaluation/rules", "calculator", 3, 1, "积分规则配置");
        createPermission("数据统计", "statistics:manage", "MENU", 0L, "/admin/statistics", "pie-chart", 4, 1, "数据统计模块");
        createPermission("班级统计", "statistics:class", "MENU", 15L, "/admin/class-stats", "team", 1, 1, "班级统计分析");
        createPermission("学生排名", "statistics:ranking", "MENU", 15L, "/admin/ranking", "trophy", 2, 1, "学生排名统计");
        createPermission("数据报表", "statistics:report", "MENU", 15L, "/admin/reports", "file-pdf", 3, 1, "数据报表导出");
        createPermission("系统功能", "feature:manage", "MENU", 0L, "/admin/features", "appstore", 5, 1, "系统功能模块");
        createPermission("公告管理", "feature:announcement", "MENU", 19L, "/admin/announcements", "notification", 1, 1, "系统公告管理");
        createPermission("消息中心", "feature:message", "MENU", 19L, "/admin/messages", "mail", 2, 1, "消息通知管理");
        createPermission("操作日志", "feature:log", "MENU", 19L, "/admin/logs", "history", 3, 1, "操作日志查询");
        createPermission("数据备份", "feature:backup", "MENU", 19L, "/admin/backup", "database", 4, 1, "数据备份恢复");
        createPermission("导入导出", "feature:import-export", "MENU", 19L, "/admin/import-export", "upload", 5, 1, "数据导入导出");
        createPermission("系统监控", "feature:monitor", "MENU", 19L, "/admin/monitor", "dashboard", 6, 1, "系统运行监控");

        List<String> adminPermissions = Arrays.asList(
            "system:manage", "system:user", "system:role", "system:permission", "system:class", "system:teacher",
            "sport:manage", "sport:audit", "sport:type", "sport:stats",
            "evaluation:manage", "evaluation:dimension", "evaluation:record", "evaluation:rule",
            "statistics:manage", "statistics:class", "statistics:ranking", "statistics:report",
            "feature:manage", "feature:announcement", "feature:message", "feature:log", "feature:backup", "feature:import-export", "feature:monitor"
        );

        List<String> teacherPermissions = Arrays.asList(
            "sport:audit", "sport:type", "sport:stats",
            "evaluation:dimension", "evaluation:record",
            "statistics:class", "statistics:ranking",
            "feature:message"
        );

        List<String> studentPermissions = Arrays.asList(
            "sport:stats", "statistics:ranking"
        );

        assignPermissions(1L, adminPermissions);
        assignPermissions(2L, teacherPermissions);
        assignPermissions(3L, studentPermissions);

        log.info("权限数据初始化完成");
    }

    private void createPermission(String name, String code, String type, Long parentId, String path, String icon, int sort, int status, String desc) {
        Permission perm = new Permission();
        perm.setPermissionName(name);
        perm.setPermissionCode(code);
        perm.setPermissionType(type);
        perm.setParentId(parentId);
        perm.setPath(path);
        perm.setIcon(icon);
        perm.setSortOrder(sort);
        perm.setStatus(status);
        perm.setDescription(desc);
        permissionMapper.insert(perm);
    }

    private void assignPermissions(Long roleId, List<String> permissionCodes) {
        for (String code : permissionCodes) {
            Permission perm = permissionMapper.selectOne(
                new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<Permission>()
                    .eq("permission_code", code)
            );
            if (perm != null) {
                RolePermission rp = new RolePermission();
                rp.setRoleId(roleId);
                rp.setPermissionId(perm.getId());
                rolePermissionMapper.insert(rp);
            }
        }
    }
}