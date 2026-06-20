package com.hhjt.service.impl;

import com.hhjt.entity.Student;
import com.hhjt.entity.Teacher;
import com.hhjt.entity.User;
import com.hhjt.mapper.ClassMapper;
import com.hhjt.mapper.StudentMapper;
import com.hhjt.mapper.TeacherMapper;
import com.hhjt.mapper.UserMapper;
import com.hhjt.service.DataImportExportService;
import com.hhjt.utils.ExcelUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.time.LocalDate;
import java.util.*;

/**
 * 数据导入导出服务实现类
 */
@Slf4j
@Service
public class DataImportExportServiceImpl implements DataImportExportService {

    @Autowired
    private StudentMapper studentMapper;

    @Autowired
    private TeacherMapper teacherMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private ClassMapper classMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> importStudents(MultipartFile file) {
        Map<String, Object> result = new HashMap<>();
        int successCount = 0;
        int failCount = 0;
        List<String> errors = new ArrayList<>();

        try {
            // 读取Excel
            List<String[]> dataList = ExcelUtil.readExcel(file, 0, 1); // 跳过表头

            for (int i = 0; i < dataList.size(); i++) {
                String[] row = dataList.get(i);
                int rowNum = i + 2; // Excel行号（从2开始，因为第1行是表头）

                try {
                    // 验证必填字段：学号、姓名、班级
                    if (!ExcelUtil.validateRequiredFields(row, new int[]{0, 1, 2})) {
                        errors.add("第" + rowNum + "行：学号、姓名、班级不能为空");
                        failCount++;
                        continue;
                    }

                    String studentNo = row[0].trim();
                    String realName = row[1].trim();
                    String className = row[2].trim();

                    // 检查学号是否已存在（简化处理，实际应该查询）
                    // Student existingStudent = studentMapper.selectByStudentNo(studentNo);
                    // 这里暂时跳过重复检查

                    // 查找或创建用户
                    User user = findOrCreateUser(studentNo, realName, "ROLE_STUDENT");

                    // 创建学生记录
                    Student student = new Student();
                    student.setUserId(user.getId());
                    student.setStudentNo(studentNo);
                    
                    // 查找班级ID并设置
                    Long classId = getClassIdByName(className);
                    if (classId != null) {
                        student.setClassId(classId);
                    }
                    
                    // 性别转换（男=1, 女=2）
                    if (row.length > 3 && row[3] != null) {
                        String genderStr = row[3].trim();
                        if ("男".equals(genderStr)) {
                            student.setGender(1);
                        } else if ("女".equals(genderStr)) {
                            student.setGender(2);
                        } else {
                            student.setGender(0);
                        }
                    }

                    studentMapper.insert(student);
                    successCount++;

                } catch (Exception e) {
                    log.error("导入学生失败，第{}行", rowNum, e);
                    errors.add("第" + rowNum + "行：" + e.getMessage());
                    failCount++;
                }
            }

            result.put("success", true);
            result.put("message", String.format("导入完成：成功 %d 条，失败 %d 条", successCount, failCount));
            result.put("successCount", successCount);
            result.put("failCount", failCount);
            result.put("errors", errors);

        } catch (IOException e) {
            log.error("读取Excel文件失败", e);
            result.put("success", false);
            result.put("message", "读取文件失败: " + e.getMessage());
        }

        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> importTeachers(MultipartFile file) {
        Map<String, Object> result = new HashMap<>();
        int successCount = 0;
        int failCount = 0;
        List<String> errors = new ArrayList<>();

        try {
            List<String[]> dataList = ExcelUtil.readExcel(file, 0, 1);

            for (int i = 0; i < dataList.size(); i++) {
                String[] row = dataList.get(i);
                int rowNum = i + 2;

                try {
                    if (!ExcelUtil.validateRequiredFields(row, new int[]{0, 1})) {
                        errors.add("第" + rowNum + "行：工号、姓名不能为空");
                        failCount++;
                        continue;
                    }

                    String teacherNo = row[0].trim();
                    String realName = row[1].trim();

                    // 检查工号是否已存在（简化处理）
                    // Teacher existingTeacher = teacherMapper.selectByTeacherNo(teacherNo);

                    User user = findOrCreateUser(teacherNo, realName, "ROLE_TEACHER");

                    Teacher teacher = new Teacher();
                    teacher.setUserId(user.getId());
                    teacher.setTeacherNo(teacherNo);

                    teacherMapper.insert(teacher);
                    successCount++;

                } catch (Exception e) {
                    log.error("导入教师失败，第{}行", rowNum, e);
                    errors.add("第" + rowNum + "行：" + e.getMessage());
                    failCount++;
                }
            }

            result.put("success", true);
            result.put("message", String.format("导入完成：成功 %d 条，失败 %d 条", successCount, failCount));
            result.put("successCount", successCount);
            result.put("failCount", failCount);
            result.put("errors", errors);

        } catch (IOException e) {
            log.error("读取Excel文件失败", e);
            result.put("success", false);
            result.put("message", "读取文件失败: " + e.getMessage());
        }

        return result;
    }

    @Override
    public void exportSportRecords(HttpServletResponse response, Long classId, String startDate, String endDate) {
        try {
            Workbook workbook = new XSSFWorkbook();
            Sheet sheet = workbook.createSheet("运动记录");

            // 创建表头样式
            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);
            headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

            // 创建表头
            Row headerRow = sheet.createRow(0);
            String[] headers = {"学号", "姓名", "班级", "运动类型", "运动日期", "时长(分钟)", "距离(米)", "卡路里", "获得积分", "状态", "备注"};
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            // TODO: 从数据库查询数据并填充
            // 这里添加示例数据
            Row dataRow = sheet.createRow(1);
            dataRow.createCell(0).setCellValue("S2026001");
            dataRow.createCell(1).setCellValue("张三");
            dataRow.createCell(2).setCellValue("22本科计科三班");
            dataRow.createCell(3).setCellValue("跑步");
            dataRow.createCell(4).setCellValue("2026-05-24");
            dataRow.createCell(5).setCellValue(60);
            dataRow.createCell(6).setCellValue(5000);
            dataRow.createCell(7).setCellValue(300);
            dataRow.createCell(8).setCellValue(20.0);
            dataRow.createCell(9).setCellValue("已通过");
            dataRow.createCell(10).setCellValue("晨跑");

            // 自动调整列宽
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            // 输出Excel
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setHeader("Content-Disposition", "attachment;filename=" + 
                URLEncoder.encode("运动记录报表_" + LocalDate.now() + ".xlsx", "UTF-8"));

            OutputStream outputStream = response.getOutputStream();
            workbook.write(outputStream);
            outputStream.flush();
            workbook.close();

            log.info("导出运动记录成功");

        } catch (IOException e) {
            log.error("导出运动记录失败", e);
        }
    }

    @Override
    public void exportPointsDetails(HttpServletResponse response, Long studentId) {
        try {
            Workbook workbook = new XSSFWorkbook();
            Sheet sheet = workbook.createSheet("积分明细");

            // 创建表头样式
            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);
            headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

            // 创建表头
            Row headerRow = sheet.createRow(0);
            String[] headers = {"学号", "姓名", "积分类型", "积分值", "说明", "获得时间"};
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            // TODO: 从数据库查询数据并填充
            // 这里添加示例数据
            Row dataRow = sheet.createRow(1);
            dataRow.createCell(0).setCellValue("S2026001");
            dataRow.createCell(1).setCellValue("张三");
            dataRow.createCell(2).setCellValue("运动积分");
            dataRow.createCell(3).setCellValue(20.0);
            dataRow.createCell(4).setCellValue("跑步打卡60分钟");
            dataRow.createCell(5).setCellValue("2026-05-24 10:30:00");

            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setHeader("Content-Disposition", "attachment;filename=" + 
                URLEncoder.encode("积分明细_" + LocalDate.now() + ".xlsx", "UTF-8"));

            OutputStream outputStream = response.getOutputStream();
            workbook.write(outputStream);
            outputStream.flush();
            workbook.close();

            log.info("导出积分明细成功");

        } catch (IOException e) {
            log.error("导出积分明细失败", e);
        }
    }

    @Override
    public void exportEvaluationResults(HttpServletResponse response, Long classId, String semester) {
        try {
            Workbook workbook = new XSSFWorkbook();
            Sheet sheet = workbook.createSheet("评价结果");

            // 创建表头样式
            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);
            headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

            // 创建表头
            Row headerRow = sheet.createRow(0);
            String[] headers = {"学号", "姓名", "班级", "学期", "耐力", "力量", "速度", "柔韧", "协调", "总分", "等级", "教师评语"};
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            // TODO: 从数据库查询数据并填充
            // 这里添加示例数据
            Row dataRow = sheet.createRow(1);
            dataRow.createCell(0).setCellValue("S2026001");
            dataRow.createCell(1).setCellValue("张三");
            dataRow.createCell(2).setCellValue("22本科计科三班");
            dataRow.createCell(3).setCellValue("2025-2026-1");
            dataRow.createCell(4).setCellValue(85);
            dataRow.createCell(5).setCellValue(78);
            dataRow.createCell(6).setCellValue(92);
            dataRow.createCell(7).setCellValue(70);
            dataRow.createCell(8).setCellValue(88);
            dataRow.createCell(9).setCellValue(83.2);
            dataRow.createCell(10).setCellValue("良好");
            dataRow.createCell(11).setCellValue("继续保持，表现不错！");

            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setHeader("Content-Disposition", "attachment;filename=" + 
                URLEncoder.encode("评价结果_" + (semester != null ? semester : "全部") + ".xlsx", "UTF-8"));

            OutputStream outputStream = response.getOutputStream();
            workbook.write(outputStream);
            outputStream.flush();
            workbook.close();

            log.info("导出评价结果成功");

        } catch (IOException e) {
            log.error("导出评价结果失败", e);
        }
    }

    @Override
    public void downloadStudentTemplate(HttpServletResponse response) {
        try {
            Workbook workbook = new XSSFWorkbook();
            Sheet sheet = workbook.createSheet("学生信息模板");

            // 创建表头
            Row headerRow = sheet.createRow(0);
            String[] headers = {"学号*", "姓名*", "班级*", "性别", "手机号", "邮箱"};
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                
                // 设置表头样式
                CellStyle style = workbook.createCellStyle();
                Font font = workbook.createFont();
                font.setBold(true);
                style.setFont(font);
                cell.setCellStyle(style);
            }

            // 添加示例数据
            Row exampleRow = sheet.createRow(1);
            exampleRow.createCell(0).setCellValue("S2026001");
            exampleRow.createCell(1).setCellValue("张三");
            exampleRow.createCell(2).setCellValue("22本科计科三班");
            exampleRow.createCell(3).setCellValue("男");
            exampleRow.createCell(4).setCellValue("13800138000");
            exampleRow.createCell(5).setCellValue("zhangsan@example.com");

            // 自动调整列宽
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            // 输出Excel
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setHeader("Content-Disposition", "attachment;filename=" + 
                URLEncoder.encode("学生信息导入模板.xlsx", "UTF-8"));

            OutputStream outputStream = response.getOutputStream();
            workbook.write(outputStream);
            outputStream.flush();
            workbook.close();

        } catch (IOException e) {
            log.error("下载模板失败", e);
        }
    }

    @Override
    public void downloadTeacherTemplate(HttpServletResponse response) {
        try {
            Workbook workbook = new XSSFWorkbook();
            Sheet sheet = workbook.createSheet("教师信息模板");

            Row headerRow = sheet.createRow(0);
            String[] headers = {"工号*", "姓名*", "手机号", "邮箱"};
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
            }

            Row exampleRow = sheet.createRow(1);
            exampleRow.createCell(0).setCellValue("T001");
            exampleRow.createCell(1).setCellValue("李老师");
            exampleRow.createCell(2).setCellValue("13900139000");
            exampleRow.createCell(3).setCellValue("teacher@example.com");

            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setHeader("Content-Disposition", "attachment;filename=" + 
                URLEncoder.encode("教师信息导入模板.xlsx", "UTF-8"));

            OutputStream outputStream = response.getOutputStream();
            workbook.write(outputStream);
            outputStream.flush();
            workbook.close();

        } catch (IOException e) {
            log.error("下载模板失败", e);
        }
    }

    /**
     * 查找或创建用户
     */
    private User findOrCreateUser(String username, String realName, String roleStr) {
        User existingUser = userMapper.selectByUsername(username);
        if (existingUser != null) {
            return existingUser;
        }

        User newUser = new User();
        newUser.setUsername(username);
        newUser.setRealName(realName);
        newUser.setPassword(""); // 初始密码为空，需要后续设置
        
        // 根据角色字符串设置roleId
        if ("ROLE_STUDENT".equals(roleStr)) {
            newUser.setRoleId(3L);
        } else if ("ROLE_TEACHER".equals(roleStr)) {
            newUser.setRoleId(2L);
        } else if ("ROLE_ADMIN".equals(roleStr)) {
            newUser.setRoleId(1L);
        }
        
        newUser.setStatus(1); // 启用状态

        userMapper.insert(newUser);
        return newUser;
    }

    /**
     * 根据班级名称获取班级ID
     */
    private Long getClassIdByName(String className) {
        // 这里简化处理，实际应该查询数据库
        // 如果班级不存在，可以创建新班级或返回null
        return null; // TODO: 实现班级查询逻辑
    }
}
