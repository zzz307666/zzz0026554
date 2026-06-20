# 🔧 功能修复计划 - 替换模拟数据为真实数据

## 📋 需要修复的功能模块

### 1. 班级统计 (ClassStatsServiceImpl) ⚠️
**文件**: `src/main/java/com/hhjt/service/impl/ClassStatsServiceImpl.java`

**问题**: 5个方法都使用模拟数据
- getClassParticipationRate() - 班级参与率
- getClassPointsRanking() - 班级积分排名
- getSportTypeDistribution() - 运动类型分布
- getTimeTrendAnalysis() - 时间趋势分析
- getClassDetailStats() - 班级详细统计

**修复方案**: 
- 查询`sport_record`表获取真实运动记录
- 查询`student_points`表获取真实积分
- 使用SQL聚合函数统计

---

### 2. 权限管理 (PermissionServiceImpl) ⚠️
**文件**: `src/main/java/com/hhjt/service/impl/PermissionServiceImpl.java`

**问题**: 4个方法使用模拟数据
- getAllRoles() - 获取所有角色
- getAllPermissions() - 获取所有权限
- getRolePermissions() - 获取角色权限
- getMenuTree() - 获取菜单树

**修复方案**:
- 查询`sys_role`表
- 创建`sys_permission`表（如果不存在）
- 创建`sys_role_permission`关联表

---

### 3. 数据备份 (DataBackupServiceImpl) ⚠️
**文件**: `src/main/java/com/hhjt/service/impl/DataBackupServiceImpl.java`

**问题**: 备份和恢复都是模拟的
- backupDatabase() - 模拟备份
- restoreDatabase() - 模拟恢复

**修复方案**:
- 使用mysqldump命令进行真实备份
- 使用mysql命令进行真实恢复

---

### 4. 其他模拟数据功能
- ClassRankingServiceImpl - 班级排行
- SocialServiceImpl - 社交功能
- SportReminderServiceImpl - 打卡提醒
- StudentProfileServiceImpl - 学生档案

---

## 🎯 修复优先级

### P0 - 高优先级（立即修复）
1. ✅ **班级统计** - 管理员核心功能
2. ✅ **数据备份** - 系统安全功能

### P1 - 中优先级
3. **权限管理** - 需要新建表结构
4. **班级排行** - 教师常用功能

### P2 - 低优先级
5. **社交功能** - 已有数据库表，只需实现查询
6. **打卡提醒** - 需要定时任务支持
7. **学生档案** - 需要整合多个表数据

---

## 📝 实施步骤

### 第一步：修复班级统计（今天完成）
1. 重写ClassStatsServiceImpl
2. 添加必要的Mapper方法
3. 测试API接口

### 第二步：修复数据备份（今天完成）
1. 实现真实的mysqldump调用
2. 实现恢复功能
3. 测试备份恢复流程

### 第三步：完善权限管理（明天）
1. 创建权限相关表
2. 实现RBAC权限模型
3. 前端权限配置页面

### 第四步：其他功能（后续）
根据实际需求逐步完善

---

## ✅ 已完成的功能（无需修复）

以下功能已经是真实数据，工作正常：
- ✅ 公告管理 (AnnouncementServiceImpl)
- ✅ 数据导入导出 (DataImportExportServiceImpl)
- ✅ 操作日志 (OperationLogServiceImpl)
- ✅ 运动审核 (SportAuditServiceImpl)
- ✅ 学生评价 (EvaluationServiceImpl)
- ✅ 积分兑换 (PointsExchangeServiceImpl)
- ✅ 成就徽章 (AchievementBadgeServiceImpl)
- ✅ 健康知识 (HealthKnowledgeServiceImpl)
- ✅ 运动目标 (StudentGoalServiceImpl)

---

**开始修复时间**: 2026-05-24  
**预计完成时间**: 2026-05-25
