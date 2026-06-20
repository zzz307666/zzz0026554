@echo off
chcp 65001 >nul
echo ========================================
echo   运动评估系统 - 数据库初始化脚本
echo ========================================
echo.

set MYSQL_USER=root
set MYSQL_PASS=123456
set MYSQL_DB=sport_evaluation_system

echo [1/2] 执行权限管理系统SQL...
mysql -u%MYSQL_USER% -p%MYSQL_PASS% %MYSQL_DB% < src\main\resources\sql\permission_system.sql
if errorlevel 1 (
    echo ❌ 权限系统SQL执行失败！
    pause
    exit /b 1
)
echo ✅ 权限系统SQL执行成功
echo.

echo [2/2] 执行运动提醒SQL...
mysql -u%MYSQL_USER% -p%MYSQL_PASS% %MYSQL_DB% < src\main\resources\sql\sport_reminder.sql
if errorlevel 1 (
    echo ❌ 运动提醒SQL执行失败！
    pause
    exit /b 1
)
echo ✅ 运动提醒SQL执行成功
echo.

echo ========================================
echo   🎉 所有SQL脚本执行完成！
echo ========================================
echo.
echo 下一步：重启Spring Boot应用
echo.
pause
