package com.hhjt.service;

import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.util.Map;

/**
 * 数据导入导出服务接口
 */
public interface DataImportExportService {
    
    /**
     * 导入学生信息
     * @param file Excel文件
     * @return 导入结果
     */
    Map<String, Object> importStudents(MultipartFile file);
    
    /**
     * 导入教师信息
     * @param file Excel文件
     * @return 导入结果
     */
    Map<String, Object> importTeachers(MultipartFile file);
    
    /**
     * 导出运动记录报表
     * @param response HTTP响应
     * @param classId 班级ID（可选）
     * @param startDate 开始日期
     * @param endDate 结束日期
     */
    void exportSportRecords(HttpServletResponse response, Long classId, String startDate, String endDate);
    
    /**
     * 导出积分明细报表
     * @param response HTTP响应
     * @param studentId 学生ID（可选）
     */
    void exportPointsDetails(HttpServletResponse response, Long studentId);
    
    /**
     * 导出评价结果报表
     * @param response HTTP响应
     * @param classId 班级ID（可选）
     * @param semester 学期
     */
    void exportEvaluationResults(HttpServletResponse response, Long classId, String semester);
    
    /**
     * 下载学生导入模板
     * @param response HTTP响应
     */
    void downloadStudentTemplate(HttpServletResponse response);
    
    /**
     * 下载教师导入模板
     * @param response HTTP响应
     */
    void downloadTeacherTemplate(HttpServletResponse response);
}
