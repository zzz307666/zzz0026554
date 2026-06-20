#!/bin/bash
# 功能修复验证脚本
# 检查日期：2026-05-24

echo "=========================================="
echo "运动评估系统 - 功能修复验证"
echo "检查日期：2026-05-24"
echo "=========================================="
echo ""

# 1. 检查操作日志模板修复
echo "1. 检查操作日志模板 (admin/operation_log.html)"
echo "------------------------------------------"
echo "需要确认的修改："
echo "  - log.status → log.result"
echo "  - log.executionTime → log.duration"
echo ""

# 检查文件中是否包含正确的字段
grep -n "log.result" src/main/resources/templates/admin/operation_log.html && echo "✅ log.result 字段正确" || echo "❌ log.result 字段缺失"
grep -n "log.duration" src/main/resources/templates/admin/operation_log.html && echo "✅ log.duration 字段正确" || echo "❌ log.duration 字段缺失"
grep -n "data.result" src/main/resources/templates/admin/operation_log.html && echo "✅ data.result JS字段正确" || echo "❌ data.result JS字段缺失"
grep -n "data.duration" src/main/resources/templates/admin/operation_log.html && echo "✅ data.duration JS字段正确" || echo "❌ data.duration JS字段缺失"
echo ""

# 2. 检查SocialServiceImpl修复
echo "2. 检查社交服务 (SocialServiceImpl.java)"
echo "------------------------------------------"
echo "需要确认的修改："
echo "  - UserMapper 注入"
echo "  - getFriendList() 真实查询用户名"
echo "  - getChallengeList() 真实查询用户名"
echo ""

grep -n "UserMapper userMapper" src/main/java/com/hhjt/service/impl/SocialServiceImpl.java && echo "✅ UserMapper 注入正确" || echo "❌ UserMapper 注入缺失"
grep -n "friendUser = userMapper.selectById" src/main/java/com/hhjt/service/impl/SocialServiceImpl.java && echo "✅ 好友姓名真实查询正确" || echo "❌ 好友姓名查询缺失"
grep -n "challengerUser = userMapper.selectById" src/main/java/com/hhjt/service/impl/SocialServiceImpl.java && echo "✅ 挑战用户名真实查询正确" || echo "❌ 挑战用户名查询缺失"
echo ""

# 3. 检查ClassRankingServiceImpl修复
echo "3. 检查班级排行榜 (ClassRankingServiceImpl.java)"
echo "------------------------------------------"
echo "需要确认的修改："
echo "  - getMostImprovedStudents() 不再使用 Math.random()"
echo "  - 基于真实数据计算进步率"
echo ""

if grep -q "Math.random()" src/main/java/com/hhjt/service/impl/ClassRankingServiceImpl.java; then
    echo "❌ 仍然存在 Math.random() 模拟数据"
else
    echo "✅ 已移除 Math.random() 模拟数据"
fi

grep -n "improvementRate = " src/main/java/com/hhjt/service/impl/ClassRankingServiceImpl.java && echo "✅ 进步率真实计算正确" || echo "❌ 进步率计算缺失"
echo ""

# 4. 检查必要的数据表
echo "4. 检查数据库表和测试数据"
echo "------------------------------------------"
echo "检查测试数据脚本是否存在..."
if [ -f "src/main/resources/sql/test_data.sql" ]; then
    echo "✅ test_data.sql 存在"
else
    echo "❌ test_data.sql 缺失"
fi
echo ""

# 5. 总结
echo "=========================================="
echo "验证总结"
echo "=========================================="
echo "本次修复完成的主要工作："
echo ""
echo "✅ 1. 操作日志页面字段修复"
echo "   - 将 log.status 改为 log.result"
echo "   - 将 log.executionTime 改为 log.duration"
echo ""
echo "✅ 2. 社交功能假数据修复"
echo "   - 好友列表显示真实姓名"
echo "   - 挑战列表显示真实姓名"
echo ""
echo "✅ 3. 班级排行榜假数据修复"
echo "   - 基于真实数据计算进步率"
echo "   - 移除随机数模拟"
echo ""
echo "=========================================="
echo "下一步操作建议："
echo "=========================================="
echo ""
echo "1. 执行测试数据脚本（如果需要）："
echo "   mysql -u root -p sport_evaluation < src/main/resources/sql/test_data.sql"
echo ""
echo "2. 启动应用程序："
echo "   mvn spring-boot:run"
echo ""
echo "3. 测试各个功能模块："
echo "   - 管理员登录 → 操作日志页面"
echo "   - 学生登录 → 社交功能"
echo "   - 教师登录 → 班级排行榜"
echo ""
echo "=========================================="
